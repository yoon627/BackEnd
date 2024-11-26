package com.devonoff.studyPost.service;

import com.devonoff.exception.CustomException;
import com.devonoff.studyPost.dto.StudyPostCreateDto;
import com.devonoff.studyPost.dto.StudyPostCreateDto.Request;
import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.studyPost.entity.StudyPost;
import com.devonoff.studyPost.repository.StudyPostRepository;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyStatus;
import com.devonoff.user.entity.User;
import com.devonoff.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyPostService {

  private final StudyPostRepository studyPostRepository;
  private final UserRepository userRepository;

  public StudyPostDto getStudyPostDetail(Long id) {
    StudyPost studyPost = studyPostRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    return StudyPostDto.fromEntity(studyPost);
  }

  public StudyPostCreateDto.Response createStudyPost(StudyPostCreateDto.Request request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    StudyPost studyPost = buildStudyPost(request, user);

    studyPostRepository.save(studyPost);

    return new StudyPostCreateDto.Response("스터디 모집 글이 생성되었습니다.");
  }

  // TODO: 엔티티로 이동시킬지 고려
  private static StudyPost buildStudyPost(Request request, User user) {
    return StudyPost.builder()
        .title(request.getTitle())
        .studyName(request.getStudyName())
        .subject(request.getSubject())
        .difficulty(request.getDifficulty())
        .dayType(request.getDayType())
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
}
