package com.devonoff.studyPost.service;

import com.devonoff.exception.CustomException;
import com.devonoff.studyPost.dto.StudyPostCreateDto;
import com.devonoff.studyPost.dto.StudyPostCreateDto.Request;
import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.studyPost.dto.StudyPostUpdateDto;
import com.devonoff.studyPost.entity.StudyPost;
import com.devonoff.studyPost.repository.StudyPostRepository;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.user.entity.User;
import com.devonoff.user.repository.UserRepository;
import com.devonoff.util.DayTypeUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyPostService {

  private final StudyPostRepository studyPostRepository;
  private final UserRepository userRepository;

  // 상세 조회
  public StudyPostDto getStudyPostDetail(Long id) {
    StudyPost studyPost = studyPostRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    return StudyPostDto.fromEntity(studyPost);
  }

  // 조회 (검색리스트)
  public List<StudyPostDto> searchStudyPosts(
      StudyMeetingType meetingType, String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyStatus status,
      Double latitude, Double longitude, Pageable pageable) {

    return studyPostRepository.findStudyPostsByFilters(
        meetingType, title, subject, difficulty, dayType, status,
        latitude, longitude, pageable);
  }

  // 생성
  public StudyPostCreateDto.Response createStudyPost(StudyPostCreateDto.Request request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    int dayType = encodeDaysFromRequest(request.getDayType());

    StudyPost studyPost = buildStudyPost(request, user);
    studyPost.setDayType(dayType);

    studyPostRepository.save(studyPost);

    return new StudyPostCreateDto.Response("스터디 모집 글이 생성되었습니다.");
  }

  // 수정
  @Transactional
  public StudyPostUpdateDto.Response updateStudyPost(Long id, StudyPostUpdateDto.Request request) {
    StudyPost studyPost = studyPostRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    updateStudyPostFields(studyPost, request);

    return new StudyPostUpdateDto.Response("스터디 모집 글이 업데이트되었습니다.");
  }

  // 모집 취소로 변경 -> 일주일뒤 자동 삭제됨
  @Transactional
  public void cancelStudyPost(Long id) {
    StudyPost studyPost = studyPostRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    studyPost.setStatus(StudyStatus.DELETION_SCHEDULED);
  }

  // 즉시 삭제 (관리자나 특정 조건에서만 사용), 회의 후 삭제 고려
  @Transactional
  public void deleteStudyPost(Long id) {
    StudyPost studyPost = studyPostRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    studyPostRepository.delete(studyPost);
  }

  // ================================= 헬퍼 메서드 ================================= //

  // 요일 리스트를 비트 값으로 인코딩
  private int encodeDaysFromRequest(List<String> dayType) {
    return DayTypeUtils.encodeDays(
        dayType.contains("월"),
        dayType.contains("화"),
        dayType.contains("수"),
        dayType.contains("목"),
        dayType.contains("금"),
        dayType.contains("토"),
        dayType.contains("일")
    );
  }

  // TODO: 엔티티로 이동시킬지 고려
  // 엔티티 생성
  private static StudyPost buildStudyPost(Request request, User user) {
    int dayType = DayTypeUtils.encodeDays(
        request.getDayType().contains("월"),
        request.getDayType().contains("화"),
        request.getDayType().contains("수"),
        request.getDayType().contains("목"),
        request.getDayType().contains("금"),
        request.getDayType().contains("토"),
        request.getDayType().contains("일")
    );

    return StudyPost.builder()
        .title(request.getTitle())
        .studyName(request.getStudyName())
        .subject(request.getSubject())
        .difficulty(request.getDifficulty())
        .dayType(dayType)
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .startTime(request.getStartTime())
        .endTime(request.getEndTime())
        .meetingType(request.getMeetingType())
        .recruitmentPeriod(request.getRecruitmentPeriod())
        .description(request.getDescription())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .status(StudyStatus.RECRUITING) // 기본값: 모집 중
        .thumbnailImgUrl(request.getThumbnailImgUrl())
        .user(user)
        .build();
  }

  // 상품 필드 업데이트
  private void updateStudyPostFields(StudyPost studyPost, StudyPostUpdateDto.Request request) {
    if (request.getTitle() != null) {
      studyPost.setTitle(request.getTitle());
    }
    if (request.getStudyName() != null) {
      studyPost.setStudyName(request.getStudyName());
    }
    if (request.getSubject() != null) {
      studyPost.setSubject(request.getSubject());
    }
    if (request.getDifficulty() != null) {
      studyPost.setDifficulty(request.getDifficulty());
    }
    if (request.getDayType() != null) {
      int dayType = DayTypeUtils.encodeDays(
          request.getDayType().contains("월"),
          request.getDayType().contains("화"),
          request.getDayType().contains("수"),
          request.getDayType().contains("목"),
          request.getDayType().contains("금"),
          request.getDayType().contains("토"),
          request.getDayType().contains("일")
      );
      studyPost.setDayType(dayType);
    }
    if (request.getStartDate() != null) {
      studyPost.setStartDate(request.getStartDate());
    }
    if (request.getEndDate() != null) {
      studyPost.setEndDate(request.getEndDate());
    }
    if (request.getStartTime() != null) {
      studyPost.setStartTime(request.getStartTime());
    }
    if (request.getEndTime() != null) {
      studyPost.setEndTime(request.getEndTime());
    }
    if (request.getMeetingType() != null) {
      studyPost.setMeetingType(request.getMeetingType());
    }
    if (request.getRecruitmentPeriod() != null) {
      studyPost.setRecruitmentPeriod(request.getRecruitmentPeriod());
    }
    if (request.getDescription() != null) {
      studyPost.setDescription(request.getDescription());
    }
    if (request.getLatitude() != null) {
      studyPost.setLatitude(request.getLatitude());
    }
    if (request.getLongitude() != null) {
      studyPost.setLongitude(request.getLongitude());
    }
    if (request.getStatus() != null) {
      studyPost.setStatus(request.getStatus());
    }
    if (request.getThumbnailImgUrl() != null) {
      studyPost.setThumbnailImgUrl(request.getThumbnailImgUrl());
    }
  }
}
