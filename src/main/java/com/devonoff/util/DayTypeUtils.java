package com.devonoff.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DayTypeUtils {

  // 요일 비트 값 매핑
  private static final Map<String, Integer> DAY_MAP = Map.of(
      "월", 1, "화", 2, "수", 4, "목", 8, "금", 16, "토", 32, "일", 64);

  private static final List<String> WEEK = List.of("월", "화", "수", "목", "금", "토", "일");

  public static int encodeDaysFromRequest(List<String> dayType) {
    if (dayType == null || dayType.isEmpty()) {
      return 0;
    }
    return dayType.stream()
        .filter(DAY_MAP::containsKey)
        .mapToInt(DAY_MAP::get)
        .reduce(0, (a, b) -> a | b);
  }

  public static List<String> decodeDays(int dayType) {
    return WEEK.stream()
        .filter(day -> (dayType & DAY_MAP.get(day)) != 0)
        .collect(Collectors.toList());
  }
}
