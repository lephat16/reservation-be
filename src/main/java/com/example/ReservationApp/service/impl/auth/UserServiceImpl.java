package com.example.ReservationApp.service.impl.auth;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.ReservationApp.dto.LoginRequestDTO;
import com.example.ReservationApp.dto.RegisterRequestDTO;
import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.auth.LoginResponseDTO;
import com.example.ReservationApp.dto.response.auth.RefreshTokenDTO;
import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.enums.UserRole;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.exception.AlreadyExistException;
import com.example.ReservationApp.generator.IdGeneratorUtil;
import com.example.ReservationApp.mapper.UserMapper;
import com.example.ReservationApp.repository.user.UserRepository;
import com.example.ReservationApp.security.JwtUtils;
import com.example.ReservationApp.service.auth.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ユーザーのログイン処理を行い、JWTトークンを生成。
     *
     * @param loginRequestDTO ログイン情報を格納したDTO
     * @return ログイン結果を含むResponseDTO
     */
    @Override
    public ResponseDTO<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDTO) {

        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("メールアドレスが見つかりません。"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("パスワードは間違っています。");
        }

        String token = jwtUtils.generateToken(loginRequestDTO.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(loginRequestDTO.getEmail());
        log.info("{}", user.getRole());
        UserDTO userDTO = userMapper.toDTO(user);
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .role(user.getRole())
                .token(token)
                .refreshToken(refreshToken)
                .expirationTime("一時間")
                .user(userDTO)
                .build();
        return ResponseDTO.<LoginResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("ユーザーのロギングに成功しました。")
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
            throw new AlreadyExistException("メールアドレスはすでに存在しています。");
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
                    .message("ユーザーの登録に成功しました。")
                    .data(userDTO)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "内部サーバーエラーが発生しました。");
        }

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
                .message("全てのユーザー取得に成功しました。")
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
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id + "のユーザーを見つかりません。"));
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message(id + "のユーザー取得に成功しました。")
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

        User existingUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id + "ユーザーを見つかりません。"));

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
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);
        // log.info(userDTO.toString());
        // log.info(updatedUser.toString());
        UserDTO updatedUserDTO = userMapper.toDTO(existingUser);
        return ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message("編集に成功しました。")
                .data(updatedUserDTO)
                .build();
    }

    /**
     * 指定したIDのユーザーを削除。
     *
     * @param id 削除するユーザーのID
     * @return 削除結果を含むResponseDTO
     */
    @Override
    public ResponseDTO<Void> deleteUser(Long id) {
        userRepository.deleteById(id);

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("削除に成功しました。")
                .build();
    }

    @Override
    public ResponseDTO<UserDTO> getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("ユーザーが見つかりません。"));

            return ResponseDTO.<UserDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("のユーザー取得に成功しました。")
                    .data(userMapper.toDTO(user))
                    .build();
        }
        return null;
    }

    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("ユーザーが見つかりません。"));
        }
        throw new NotFoundException("認証されていません。");
    }

    @Override
    public ResponseDTO<RefreshTokenDTO> refresh(Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseDTO.<RefreshTokenDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("リフレッシュトークンが必要です。")
                    .build();
        }

        try {
            String email = jwtUtils.extractUsername(refreshToken);

            String newAccessToken = jwtUtils.refreshAccessToken(email, refreshToken);

            String newRefreshToken = jwtUtils.generateRefreshToken(email);

            return ResponseDTO.<RefreshTokenDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("アクセストークンが再発行されました。")
                    // .token(newAccessToken)
                    // .refreshToken(newRefreshToken)
                    .data(RefreshTokenDTO.builder()
                            .token(newAccessToken)
                            .refreshToken(newRefreshToken)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Refresh token error: ", e);
            return ResponseDTO.<RefreshTokenDTO>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("リフレッシュトークンが無効です。再ログインしてください。")
                    .build();
        }
    }

}
