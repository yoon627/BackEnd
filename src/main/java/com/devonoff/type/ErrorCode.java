package com.devonoff.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // 회원가입 및 로그인
  EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST.value(), "이미 사용 중인 이메일입니다."), // 400
  NICKNAME_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST.value(), "이미 사용 중인 닉네임입니다."), // 400
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "이메일 또는 비밀번호가 잘못되었습니다."), // 401
  SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED.value(), "소셜 로그인에 실패했습니다."), // 401
  ACCOUNT_PENDING_DELETION(HttpStatus.FORBIDDEN.value(), "해당 계정은 탈퇴 예정 상태입니다."), // 403 -> 필요한가?
  EMAIL_CERTIFICATION_FAILED(HttpStatus.BAD_REQUEST.value(),
      "이메일 인증에 실패했습니다."), // 400 -> 이거쓸지 아래 두개 쓸지 고려
  EMAIL_CERTIFICATION_UNCOMPLETED(HttpStatus.BAD_REQUEST.value(),
      "이메일 인증이 완료되지 않았습니다."),
  INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST.value(), "이메일 인증 코드가 유효하지 않습니다."), // 400
  EXPIRED_EMAIL_CODE(HttpStatus.BAD_REQUEST.value(), "이메일 인증 코드가 만료되었습니다."), // 400
  EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "인증 코드 메일 발송에 실패했습니다."), // 500
  // 유저 정보 관리
  USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자를 찾을 수 없습니다."), // 404
  INVALID_PROFILE_IMAGE_FORMAT(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 프로필 이미지 형식입니다."), // 400
  PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "프로필 이미지를 업로드하는 데 실패했습니다."), // 500
  UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED.value(), "로그인 된 사용자와 일치하지 않습니다."),
  // 스터디 관련
  STUDY_POST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "스터디 모집글을 찾을 수 없습니다."), // 404
  STUDY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "스터디를 찾을 수 없습니다."), // 404
  PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "스터디 참여자를 찾을 수 없습니다."), // 404
  DUPLICATE_APPLICATION(HttpStatus.BAD_REQUEST.value(), "이미 해당 스터디에 신청했습니다."), // 400
  SIGNUP_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "스터디 신청 내역을 찾을 수 없습니다."), // 404
  SIGNUP_STATUS_ALREADY_FINALIZED(HttpStatus.BAD_REQUEST.value(), "이미 확정된 신청 상태입니다."), // 400
  NO_APPROVED_SIGNUPS(HttpStatus.BAD_REQUEST.value(), "승인된 신청자가 없습니다."), // 400
  STUDY_POST_FULL(HttpStatus.BAD_REQUEST.value(), "스터디 모집 인원이 가득 찼습니다."), // 400
  INVALID_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST.value(), "모집 인원은 최소 2명, 최대 10명까지 설정할 수 있습니다."), // 400
  APPLICATION_PERIOD_CLOSED(HttpStatus.BAD_REQUEST.value(), "스터디 신청 기간이 종료되었습니다."), // 400
  STUDY_EXTENSION_FAILED(HttpStatus.BAD_REQUEST.value(), "스터디 모집 기한 연장은 최대 1개월입니다."), // 400
  INVALID_STUDY_STATUS(HttpStatus.BAD_REQUEST.value(), "잘못된 스터디 상태값입니다."), // 400
  MAP_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "지도 API 요청에 실패했습니다."), // 500
  LOCATION_REQUIRED_FOR_HYBRID(HttpStatus.BAD_REQUEST.value(),
      "온/오프라인 병행 스터디의 경우 위치 정보가 필수입니다."), // 400
  // QnA 게시글
  INVALID_INPUT_VALUE(HttpStatus.NOT_FOUND.value(), "입력이 정상적으로 되지 않았습니다."),

  POST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "게시글을 찾을 수 없습니다."), // 404
  UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN.value(), "게시글에 접근할 권한이 없습니다."), // 403
  // 정보 공유 게시글
  SHARED_POST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "정보 공유 게시글을 찾을 수 없습니다."), // 404
  UNAUTHORIZED_SHARED_POST_ACCESS(HttpStatus.FORBIDDEN.value(), "정보 공유 게시글에 접근할 권한이 없습니다."), // 403
  // 댓글
  INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST.value(), "댓글 내용이 유효하지 않습니다"),
  UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN.value(), "댓글에 접근할 권한이 없습니다."), // 403
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "댓글을 찾을 수 없습니다."), // 404

  // 채팅
  CHAT_MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "채팅 메시지 송신에 실패했습니다."), // 500
  CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "채팅방을 찾을 수 없습니다."), // 404
  CHAT_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "채팅방에 접근할 권한이 없습니다."), // 403
  // 화상채팅
  VIDEO_CHATROOM_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "화상 채팅방 생성에 실패했습니다."), // 500
  VIDEO_CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "화상 채팅방을 찾을 수 없습니다."), // 404
  VIDEO_CHATROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "화상 채팅방에 접근할 권한이 없습니다."), // 403
  VIDEO_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "화상 채팅 연결에 실패했습니다."), // 500
  // 학습시간(타이머)
  STUDY_TIME_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "스터디 시간을 찾을 수 없습니다."), // 404
  STUDY_TOP_RANKING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "스터디 랭킹 조회에 실패했습니다."), // 500
  // 공통 예외
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요."), // 500
  BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."), // 400
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST.value(), "입력값 검증에 실패했습니다."), // 400
  // 보안 관련 예외
  UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다."), // 403
  // 토큰 관련 예외
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."), // 401
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED.value(), "토큰이 만료되었습니다."), // 401
  TOKEN_NOT_PROVIDED(HttpStatus.UNAUTHORIZED.value(), "토큰이 제공되지 않았습니다."), // 401
  REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다."), // 401
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 리프레시 토큰입니다."); // 401
  private final int status;
  private final String description;
}
