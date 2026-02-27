package com.example.ReservationApp.service.impl.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.ReservationApp.dto.LoginRequestDTO;
import com.example.ReservationApp.dto.RegisterRequestDTO;
import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.request.ChangePasswordRequest;
import com.example.ReservationApp.dto.response.auth.LoginResponseDTO;
import com.example.ReservationApp.dto.user.CreatePasswordDTO;
import com.example.ReservationApp.dto.user.CreateUserDTO;
import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.dto.user.UserSessionDTO;
import com.example.ReservationApp.entity.user.LoginHistory;
import com.example.ReservationApp.entity.user.PasswordResetToken;
import com.example.ReservationApp.entity.user.TokenType;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.entity.user.UserSession;
import com.example.ReservationApp.enums.LoginStatus;
import com.example.ReservationApp.enums.RevokedReason;
import com.example.ReservationApp.enums.SessionStatus;
import com.example.ReservationApp.enums.UserRole;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.exception.AlreadyExistException;
import com.example.ReservationApp.exception.BadRequestException;
import com.example.ReservationApp.generator.IdGeneratorUtil;
import com.example.ReservationApp.mapper.UserMapper;
import com.example.ReservationApp.mapper.UserSessionMapper;
import com.example.ReservationApp.repository.user.LoginHistoryRepository;
import com.example.ReservationApp.repository.user.PasswordResetTokenRepository;
import com.example.ReservationApp.repository.user.UserRepository;
import com.example.ReservationApp.repository.user.UserSessionRepository;
import com.example.ReservationApp.security.AuthUser;
import com.example.ReservationApp.security.JwtUtils;
import com.example.ReservationApp.service.auth.UserService;
import com.example.ReservationApp.util.UserAgentParser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UserServiceImplクラスは、ユーザー管理に関するサービスロジックを提供。
 * 
 * 主な機能:
 * - ユーザーの新規登録
 * - ユーザーのログイン
 * - ユーザー情報の取得・更新・削除
 * - JWTトークン生成と認証関連処理
 * - ユーザーのログアウト
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final UserSessionMapper userSessionMapper;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ユーザーのログイン処理を行い、JWTトークンを生成。
     *
     * @param loginRequestDTO ログイン情報を格納したDTO
     * @return ログイン結果を含むResponseDTO
     */
    @Override
    public ResponseDTO<LoginResponseDTO> loginUser(
            LoginRequestDTO loginRequestDTO,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("メールアドレスが見つかりません"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            saveLoginHistory(user, request, LoginStatus.FAILED);
            throw new InvalidCredentialException("パスワードは間違っています");
        }

        log.info("{}", user.getRole());
        saveLoginHistory(user, request, LoginStatus.SUCCESS);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setIpAddress(request.getRemoteAddr());
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setExpiry(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        session.setRevoked(false);

        session = userSessionRepository.save(session);
        boolean remember = loginRequestDTO.isRemember();
        String accessToken = jwtUtils.generateToken(user.getEmail(), session.getId());
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);
        accessCookie.setAttribute("SameSite", "Lax");
        response.addCookie(accessCookie);

        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), session.getId());

        session.setRefreshToken(refreshToken);
        userSessionRepository.save(session);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(remember ? 7 * 24 * 60 * 60 : -1);
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        UserDTO userDTO = userMapper.toDTO(user);
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .role(user.getRole())
                .expirationTime("一時間")
                .user(userDTO)
                .build();
        return ResponseDTO.<LoginResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("ユーザーのロギングに成功しました")
                .data(loginResponseDTO)
                .build();
    }

    /**
     * ユーザーを新規登録するサービスメソッド
     *
     * 処理内容:
     * - リクエストにロールが指定されていればそのロールを使用、指定がなければデフォルトでMANAGERを設定
     * - パスワードをハッシュ化してUserエンティティを作成
     * - 作成したUserをデータベースに保存
     * - 登録成功のレスポンスを返却
     *
     * @param registerRequestDTO 登録情報を格納したDTO
     * @return 登録結果を含むResponseDTO
     */
    @Override
    public ResponseDTO<UserDTO> registerUser(RegisterRequestDTO registerRequestDTO) {

        UserRole defaultRole = UserRole.STAFF;
        if (registerRequestDTO.getRole() != null) {
            defaultRole = registerRequestDTO.getRole();
        }

        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new AlreadyExistException("メールアドレスはすでに存在しています");
        }
        try {
            User userToSave = User.builder()
                    .name(registerRequestDTO.getName())
                    .email(registerRequestDTO.getEmail())
                    .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                    .phoneNumber(registerRequestDTO.getPhoneNumber())
                    .role(defaultRole)
                    .build();

            String prefix = switch (defaultRole) {
                case STAFF -> "STA";
                case ADMIN -> "ADM";
                case WAREHOUSE -> "WAH";
            };
            String userId = IdGeneratorUtil.generateUserId(entityManager, prefix);
            userToSave.setUserId(userId);
            userRepository.save(userToSave);

            UserDTO userDTO = userMapper.toDTO(userToSave);
            return ResponseDTO.<UserDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("ユーザーの登録に成功しました")
                    .data(userDTO)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "内部サーバーエラーが発生しました");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseDTO<UserDTO> createUserByAdmin(CreateUserDTO request, AuthUser authUser) {

        User adminUser = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理者が見つかりません"));
        if (adminUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理者のみユーザーを作成できます");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("メールアドレスはすでに存在しています");
        }

        String tempPassword = UUID.randomUUID().toString();

        User userToSave = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : UserRole.STAFF)
                .password(passwordEncoder.encode(tempPassword))
                .build();

        String prefix = switch (userToSave.getRole()) {
            case STAFF -> "STA";
            case ADMIN -> "ADM";
            case WAREHOUSE -> "WAH";
        };

        userToSave.setUserId(IdGeneratorUtil.generateUserId(entityManager, prefix));
        userRepository.save(userToSave);

        sendPasswordTokenEmail(request.getEmail(), TokenType.CREATE_PASSWORD, 30);

        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message("ユーザーの登録に成功しました")
                .data(userMapper.toDTO(userToSave))
                .build();
    }

    /**
     * 全ユーザーを取得。
     *
     * @return 全ユーザー情報を含むResponseDTO
     */
    @Override
    public ResponseDTO<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOs = userMapper.toDTOList(users);

        return ResponseDTO.<List<UserDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("全てのユーザー取得に成功しました")
                .data(userDTOs)
                .build();
    }

    /**
     * 指定したIDのユーザーを取得。
     *
     * @param id 取得するユーザーのID
     * @return ユーザー情報を含むResponseDTO
     */
    @Override
    public ResponseDTO<UserDTO> getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id + "のユーザーを見つかりません"));
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message(id + "のユーザー取得に成功しました")
                .data(userDTO)
                .build();
    }

    /**
     * ユーザー情報を更新。
     *
     * @param id      更新対象のユーザーID
     * @param userDTO 更新情報を格納したDTO
     * @return 更新結果を含むResponseDTO
     */
    @Transactional
    @Override
    public ResponseDTO<UserDTO> updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id + "ユーザーを見つかりません"));

        if (userDTO.getName() != null && !userDTO.getName().isBlank()) {
            existingUser.setName(userDTO.getName());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank()) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().isBlank()) {
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getRole() != null) {
            existingUser.setRole(userDTO.getRole());
        }

        userRepository.save(existingUser);

        UserDTO updatedUserDTO = userMapper.toDTO(existingUser);
        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message("編集に成功しました")
                .data(updatedUserDTO)
                .build();
    }

    /**
     * 指定したIDのユーザーを削除。
     *
     * @param id 削除するユーザーのID
     * @return 削除結果を含むResponseDTO
     */
    @Transactional
    @Override
    public ResponseDTO<Void> deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id + "ユーザーを見つかりません"));
        passwordResetTokenRepository.deleteByUser(existingUser);
        userRepository.deleteById(id);

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("削除に成功しました")
                .build();
    }

    @Override
    public ResponseDTO<UserDTO> getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("ユーザーが見つかりません"));

            return ResponseDTO.<UserDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("のユーザー取得に成功しました")
                    .data(userMapper.toDTO(user))
                    .build();
        }
        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Not logged in")
                .data(null)
                .build();

    }

    /**
     * 現在ログイン中のユーザー情報をEntity形式で取得する。
     *
     * Spring Securityの認証情報からメールアドレスを取得し、DBからユーザーを検索。
     * 認証されていない場合やユーザーが見つからない場合はNotFoundExceptionを投げる。
     *
     * @return 現在ログイン中のUserエンティティ
     * @throws NotFoundException 認証されていない場合またはユーザーが存在しない場合
     */
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("ユーザーが見つかりません"));
        }
        throw new NotFoundException("認証されていません");
    }

    /**
     * リフレッシュトークンを使用してアクセストークンを再発行する処理
     *
     * Cookie に保存されている refreshToken を検証し、
     * 有効な場合は新しい accessToken と refreshToken を生成して
     * HttpOnly Cookie として再設定する。
     *
     * フロントエンドではアクセストークンの有効期限切れ（401発生時）に
     * 自動的に本APIを呼び出して再認証を行う。
     *
     * @param response     HttpServletResponse（Cookieの再設定に使用）
     * @param refreshToken Cookieから取得したリフレッシュトークン
     * @return 処理結果
     */
    @Override
    public ResponseDTO<Void> refresh(HttpServletResponse response, String refreshToken) {

        // リフレッシュトークンが無い場合は401返却
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseDTO.<Void>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("リフレッシュトークンがありません。再ログインしてください")
                    .build();
        }
        try {
            // トークンからメールアドレスを取得（期限内チェック）
            String email = jwtUtils.extractUsernameIfNotExpired(refreshToken);
            // 旧セッション取得 + 無効化・期限切れチェック
            UserSession oldSession = userSessionRepository.findByRefreshToken(refreshToken)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "無効なリフレッシュトークン"));
            // チェック revoked + expiry
            if (oldSession.isRevoked() || oldSession.getExpiry().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "セッションが無効です。再ログインしてください");
            }
            User user = oldSession.getUser();
            // 旧セッションを無効化
            oldSession.setRevoked(true);
            oldSession.setRevokedAt(LocalDateTime.now());
            oldSession.setRevokedReason(RevokedReason.ROTATED);
            userSessionRepository.save(oldSession);
            // 新しいセッション作成（まずrefreshToken空で保存）
            UserSession newSession = UserSession.builder()
                    .user(user)
                    .refreshToken("")
                    .ipAddress(oldSession.getIpAddress())
                    .userAgent(oldSession.getUserAgent())
                    .createdAt(LocalDateTime.now())
                    .expiry(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .build();
            userSessionRepository.save(newSession);
            // 新しいアクセストークンとリフレッシュトークンを生成
            String newAccessToken = jwtUtils.generateToken(email, newSession.getId());
            String newRefreshToken = jwtUtils.generateRefreshToken(email, newSession.getId());

            // 生成したリフレッシュトークンを新セッションに設定して更新
            newSession.setRefreshToken(newRefreshToken);
            userSessionRepository.save(newSession);
            // Cookieに設定
            Cookie accessCookie = new Cookie("accessToken", newAccessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(60 * 15);
            accessCookie.setAttribute("SameSite", "Lax");
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            refreshCookie.setAttribute("SameSite", "Lax");
            response.addCookie(refreshCookie);

            return ResponseDTO.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("アクセストークンが再発行されました")
                    .build();
        } catch (Exception e) {
            log.error("リフレッシュトークンのエラー", e);
            return ResponseDTO.<Void>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("リフレッシュトークンが無効です。再ログインしてください")
                    .build();
        }
    }

    /**
     * ログアウト処理
     *
     * ブラウザに保存されている accessToken / refreshToken の
     * Cookie を削除（MaxAge=0）し、認証情報を無効化する。
     *
     * フロントエンドではログアウトボタン押下時に呼び出され、
     * セッションを完全に終了させるために使用する。
     *
     * @param response HttpServletResponse（Cookie削除に使用）
     * @return 処理結果
     */
    @Override
    public ResponseDTO<Void> logout(HttpServletResponse response, String refreshToken) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            userSessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
                if (!session.isRevoked()) {
                    session.setRevoked(true);
                    session.setRevokedAt(LocalDateTime.now());
                    session.setRevokedReason(RevokedReason.USER_LOGOUT);
                    userSessionRepository.save(session);
                }
            });
        }
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        accessCookie.setAttribute("SameSite", "Strict");

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        accessCookie.setAttribute("SameSite", "Strict");

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("ログアウトしました")
                .build();
    }

    @Transactional
    @Override
    public ResponseDTO<UserDTO> changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("ユーザーが見つかりません"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialException("パスワードは間違っています");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("新しいパスワードが一致しません");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("以前のパスワードと同じです");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message("パスワードが変更されました")
                .data(userDTO)
                .build();
    }

    /**
     * パスワード作成／リセット処理
     *
     * クライアントから送信されたトークンとパスワードを受け取り、
     * トークンの有効性を確認後、ユーザーのパスワードを更新する。
     * 更新後、トークンは削除されるため、同じトークンは再利用不可。
     *
     * @param request CreatePasswordDTO（token と新しい password を含む）
     * @return パスワード設定結果を含む ResponseDTO
     */
    @Transactional
    @Override
    public ResponseDTO<Void> createPassword(CreatePasswordDTO request) {

        // トークンをデータベースから取得
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "無効なトークンです"));

        // トークンの有効期限をチェック
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "トークンの有効期限が切れています");
        }

        // パスワードをハッシュ化して更新
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // 使用済みトークンを削除
        passwordResetTokenRepository.delete(resetToken);
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("パスワードの設定が完了しました")
                .build();
    }

    /**
     * ログイン履歴を保存する処理
     *
     * ユーザーのログイン時に、IPアドレス・User-Agent・ステータスを
     * LoginHistoryテーブルに保存する。
     *
     * @param user    ログインしたユーザー
     * @param request HttpServletRequest（IPアドレスやUser-Agent取得に使用）
     * @param status  ログイン成功／失敗などのステータス
     */
    public void saveLoginHistory(User user, HttpServletRequest request, LoginStatus status) {

        LoginHistory history = new LoginHistory();
        history.setUserId(user.getUserId());
        history.setLoginTime(LocalDateTime.now());
        history.setIpAddress(request.getRemoteAddr());
        history.setUserAgent(request.getHeader("User-Agent"));
        history.setStatus(status);

        loginHistoryRepository.save(history);
    }

    /**
     * 現在ログイン中のユーザーのログイン履歴を取得
     *
     * データベースからユーザーIDに紐づくログイン履歴を取得し、
     * 登録日時の降順で返す。
     *
     * @return ログイン履歴リストを含む ResponseDTO
     */
    @Override
    public ResponseDTO<List<LoginHistory>> getLoginHistory() {

        String userId = getCurrentUserEntity().getUserId();
        List<LoginHistory> loginHistories = loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId);

        return ResponseDTO.<List<LoginHistory>>builder()
                .status(HttpStatus.OK.value())
                .message("ログイン履歴の取得に成功しました")
                .data(loginHistories)
                .build();
    }

    /**
     * パスワードリセット／アカウント作成用のメール送信処理
     *
     * 指定されたメールアドレスに対して、パスワードリセット用または
     * アカウント作成用のトークンを生成し、データベースに保存した後、
     * メールでリンクを送信する。
     *
     * 存在しないメールアドレスの場合でも、セキュリティ上の理由から
     * 成功メッセージを返し、メールの存在有無を判定できないようにする。
     *
     * @param email         送信対象のユーザーのメールアドレス
     * @param type          トークンの種類（CREATE_PASSWORD または RESET_PASSWORD）
     * @param expiryMinutes トークンの有効期限（分単位）
     * @return メール送信処理の結果を含む ResponseDTO
     */
    @Override
    @Transactional
    public ResponseDTO<Void> sendPasswordTokenEmail(String email, TokenType type, int expiryMinutes) {
        log.info("email: {}", email);
        // メールアドレスからユーザーを取得
        Optional<User> optionalUser = userRepository.findByEmail(email);
        // ユーザーが存在しない場合でもセキュリティ上の理由で成功メッセージを返す
        if (optionalUser.isEmpty()) {
            return ResponseDTO.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("メールアドレスが登録されている場合、リンクを送信しました")
                    .build();
        }

        User user = optionalUser.get();
        // 既存のパスワードリセットトークンを削除（古いトークンを無効化）
        passwordResetTokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();

        // PasswordResetTokenエンティティを作成し、有効期限を設定
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(expiryMinutes)) // 有効期限
                .build();
        passwordResetTokenRepository.save(resetToken);

        // 送信するリンクとメッセージを準備
        String link, message;
        if (type == TokenType.CREATE_PASSWORD) {
            link = frontendUrl + "/create-password?token=" + token;
            message = "アカウント作成用のリンクを送信しました";
        } else {
            link = frontendUrl + "/reset-password?token=" + token;
            message = "パスワード再設定用のリンクを送信しました";
        }
        sendEmail(user.getEmail(), link);
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    /**
     * パスワード再設定処理
     *
     * クライアントから送信されたトークンと新しいパスワードを受け取り、
     * トークンの有効性を確認後、ユーザーのパスワードを更新する。
     * 更新後、使用済みトークンは削除されるため再利用不可。
     *
     * @param token       パスワードリセット用トークン
     * @param newPassword 新しいパスワード
     * @return パスワード再設定結果を含む ResponseDTO
     */
    @Override
    public ResponseDTO<Void> resetPassword(String token, String newPassword) {

        // トークンをデータベースから取得
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "無効なトークンです"));

        // トークンの有効期限をチェック
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "トークンの有効期限が切れています");
        }

        // パスワードをハッシュ化して更新
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 使用済みトークンを削除
        passwordResetTokenRepository.delete(resetToken);
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("パスワードの再設定が完了しました")
                .build();
    }

    /**
     * パスワード設定用トークンの有効性検証
     *
     * トークンが存在するか、かつ有効期限内であるかを確認する。
     *
     * @param token パスワードリセット用トークン
     * @return トークン検証結果を含む ResponseDTO
     */
    @Override
    public ResponseDTO<Void> verifySetPasswordToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "無効なトークンです"));
        // トークンの有効期限をチェック
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "トークンの有効期限が切れています");
        }
        ;
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("トークンは有効です")
                .build();
    }

    /**
     * パスワード再設定メール送信処理
     *
     * 指定されたメールアドレス宛に、パスワード再設定用リンクを送信する。
     *
     * @param to   メール送信先アドレス
     * @param link パスワード再設定用リンク
     */
    private void sendEmail(String to, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("パスワード再設定のご案内");
        message.setText("以下のリンクをクリックしてパスワードを再設定してください:\n" + link);

        mailSender.send(message);
    }

    /**
     * 指定されたユーザーIDのセッション一覧を取得する。
     * 現在のセッションも判定してDTOに設定する。
     *
     * @param id          ユーザーID
     * @param accessToken 現在のアクセスToken（どのセッションか判定用）
     * @return UserSessionDTOのリストを含むResponseDTO
     */
    @Override
    public ResponseDTO<List<UserSessionDTO>> getUserSessions(Long id, String accessToken) {

        if (!userRepository.existsById(id)) {
            throw new NotFoundException(id + "のユーザーを見つかりません");
        }
        // 現在のセッションIDを取得
        Long currentSessionid = jwtUtils.extractSessionId(accessToken);
        List<UserSession> sessions = userSessionRepository.findTop10ByUserIdOrderByCreatedAtDesc(id);
        // DTOに変換し、状態とデバイス情報を設定
        List<UserSessionDTO> sessionDTOs = sessions.stream()
                .map(session -> {
                    UserSessionDTO dto = userSessionMapper.toDTO(session);
                    // セッション状態を判定（ACTIVE / REVOKED / EXPIRED
                    dto.setStatus(!session.isRevoked() && session.getExpiry().isAfter(LocalDateTime.now())
                            ? SessionStatus.ACTIVE
                            : session.isRevoked() ? SessionStatus.REVOKED : SessionStatus.EXPIRED);
                    dto.setDevice(UserAgentParser.formatDeviceInfo(session.getUserAgent()));
                    dto.setCurrentSession(session.getId().equals(currentSessionid));
                    return dto;
                })
                .toList();
        return ResponseDTO.<List<UserSessionDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("セッション一覧を取得しました")
                .data(sessionDTOs)
                .build();
    }

    /**
     * 指定されたセッションIDを無効化する。
     * 管理者操作用。
     *
     * @param sessionId 無効化するセッションID
     * @return 処理結果を示すResponseDTO
     */
    @Override
    public ResponseDTO<Void> revokeSession(Long sessionId) {

        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("このセッションを見つかりません"));
        // 未無効化なら無効化
        if (!session.isRevoked()) {
            session.setRevoked(true);
            session.setRevokedAt(LocalDateTime.now());
            session.setRevokedReason(RevokedReason.ADMIN_REVOKE);
            userSessionRepository.save(session);
        }

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("セッションを無効化しました")
                .build();
    }

    /**
     * 指定ユーザーの全セッションを無効化する。
     * 管理者操作用。
     *
     * @param userId 対象ユーザーID
     * @return 処理結果を示すResponseDTO
     */
    @Override
    public ResponseDTO<Void> revokeAllSessions(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("このユーザーを見つかりません");
        }
        List<UserSession> sessions = userSessionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        // 未無効化のセッションをすべて無効化
        for (UserSession session : sessions) {
            if (!session.isRevoked()) {
                session.setRevoked(true);
                session.setRevokedAt(LocalDateTime.now());
                session.setRevokedReason(RevokedReason.ADMIN_REVOKE);
            }
        }
        userSessionRepository.saveAll(sessions);

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("すべてのセッションを無効化しました")
                .build();
    }
}