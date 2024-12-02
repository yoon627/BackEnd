package com.devonoff.domain.totalstudytime.dto;

import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalStudyTimeDto {

  //TODO 프론트엔드로 내려보낼떄 필요한 정보들 고려해야함
  private Long studyId;
  private String studyName;
  private Long totalStudyTime;

  public static TotalStudyTimeDto fromEntityWithStudyName(TotalStudyTime totalStudyTime,
      String studyName) {
    return TotalStudyTimeDto.builder().studyId(totalStudyTime.getStudyId()).studyName(studyName)
        .totalStudyTime(
            totalStudyTime.getTotalStudyTime()).build();
  }
}
