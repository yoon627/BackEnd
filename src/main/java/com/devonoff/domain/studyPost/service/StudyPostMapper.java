package com.devonoff.domain.studyPost.service;

import com.devonoff.domain.studyPost.dto.StudyPostUpdateDto.Request;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.util.DayTypeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {DayTypeUtils.class})

public interface StudyPostMapper {

  @Mapping(target = "title", source = "request.title")
  @Mapping(target = "studyName", source = "request.studyName")
  @Mapping(target = "subject", source = "request.subject")
  @Mapping(target = "difficulty", source = "request.difficulty")
  @Mapping(target = "dayType", expression = "java(DayTypeUtils.encodeDaysFromRequest(request.getDayType()))")
  @Mapping(target = "startDate", source = "request.startDate")
  @Mapping(target = "endDate", source = "request.endDate")
  @Mapping(target = "startTime", source = "request.startTime")
  @Mapping(target = "endTime", source = "request.endTime")
  @Mapping(target = "meetingType", source = "request.meetingType")
  @Mapping(target = "recruitmentPeriod", source = "request.recruitmentPeriod")
  @Mapping(target = "description", source = "request.description")
  @Mapping(target = "latitude", source = "request.latitude")
  @Mapping(target = "longitude", source = "request.longitude")
  @Mapping(target = "status", source = "request.status")
  @Mapping(target = "thumbnailImgUrl", source = "request.thumbnailImgUrl")

  void toStudyPost(Request request, @MappingTarget StudyPost studyPost);
}
