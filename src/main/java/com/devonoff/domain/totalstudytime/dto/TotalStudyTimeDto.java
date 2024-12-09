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

  public static String[] timeUnit = new String[]{"년 ", "월 ", "일 ", "시간 ", "분 "};
  public static long[] timeArr = new long[]{12 * 30 * 24 * 60 * 60, 30 * 24 * 60 * 60, 24 * 60 * 60,
      60 * 60, 60};
  private Long studyId;
  private Long ranking;
  private String studyName;
  private String totalStudyTime;
  private Double percent;

  public static TotalStudyTimeDto fromEntityWithStudyNameAndRanking(TotalStudyTime totalStudyTime,
      String studyName, Long ranking, Double percent) {
    //TODO 나중에 timeUtil이나 다른 곳을 빼주고 최적화하기
    StringBuffer sb = new StringBuffer();
    long seconds = totalStudyTime.getTotalStudyTime();
    for (int i = 0; i < timeUnit.length; i++) {
      if (seconds > timeArr[i]) {
        sb.append(seconds / timeArr[i]).append(timeUnit[i]);
        seconds %= timeArr[i];
      }
    }
    if (seconds > 0) {
      sb.append(seconds).append("초");
    }
    if (sb.isEmpty()) {
      sb.append("0초");
    }
    return TotalStudyTimeDto.builder()
        .studyId(totalStudyTime.getStudyId())
        .studyName(studyName)
        .totalStudyTime(sb.toString())
        .ranking(ranking)
        .percent(percent)
        .build();
  }
}

