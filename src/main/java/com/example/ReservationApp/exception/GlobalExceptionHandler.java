package com.example.ReservationApp.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import com.example.ReservationApp.dto.ResponseDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * グローバル例外ハンドラー
 *
 * コントローラーで発生した例外を統一的に処理、
 * クライアントにJSON形式でレスポンスを返却。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

        /**
         * NotFoundExceptionが発生した場合のハンドラー
         *
         * @param ex NotFoundException
         * @return HTTPステータス404とエラーメッセージを含むResponseDTO
         */
        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ResponseDTO<Void>> handleNotFoundException(NotFoundException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }

        /**
         * InvalidCredentialExceptionが発生した場合のハンドラー
         *
         * @param ex InvalidCredentialException
         * @return HTTPステータス400とエラーメッセージを含むResponseDTO
         */
        @ExceptionHandler(InvalidCredentialException.class)
        public ResponseEntity<ResponseDTO<Void>> invalidCredentialException(InvalidCredentialException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        /**
         * ResponseStatusExceptionが発生した場合のハンドラー
         *
         * @param ex ResponseStatusException
         * @return 例外に設定されたHTTPステータスとエラーメッセージを含むResponseDTO
         */
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ResponseDTO<Void>> handleResponseStatus(ResponseStatusException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(ex.getStatusCode().value())
                                .message(ex.getReason())
                                .build();
                return ResponseEntity.status(ex.getStatusCode()).body(responseDTO);
        }

        @ExceptionHandler(AlreadyExistException.class)
        public ResponseEntity<ResponseDTO<Void>> handleAlreadyExistException(AlreadyExistException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ResponseDTO<Void>> handleBadRequestException(BadRequestException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(NameValueRequiredException.class)
        public ResponseEntity<ResponseDTO<Void>> handleValueRequiredException(NameValueRequiredException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ResponseDTO<Void>> handleConstraintViolation(ConstraintViolationException ex) {
                List<String> errors = ex.getConstraintViolations().stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toList());
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(String.join(", ", errors))
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ResponseDTO<Object>> handleValidation(HandlerMethodValidationException ex) {

                List<String> errors = ex.getAllErrors().stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.toList());
                ResponseDTO<Object> responseDTO = ResponseDTO.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("入力に誤りがあります")
                                .errors(errors)
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ResponseDTO<Object>> handleArgumentValidation(MethodArgumentNotValidException ex) {

                List<String> errors = ex.getAllErrors().stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.toList());
                ResponseDTO<Object> responseDTO = ResponseDTO.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("入力に誤りがあります")
                                .errors(errors)
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ResponseDTO<Void>> handleIllegal(IllegalStateException ex) {

                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException ex) {

                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(CannotDeleteException.class)
        public ResponseEntity<ResponseDTO<Void>> handleCannotDeleteException(CannotDeleteException ex) {

                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);

        }

        @ExceptionHandler(InvalidActionException.class)
        public ResponseEntity<ResponseDTO<Void>> InvalidActionException(InvalidActionException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ResponseDTO<Void>> UnauthorizedException(UnauthorizedException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
        @ExceptionHandler(InvalidRefreshTokenException.class)
        public ResponseEntity<ResponseDTO<Void>> InvalidRefreshTokenException(InvalidRefreshTokenException ex) {
                ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
        }
}