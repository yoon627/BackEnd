package com.devonoff.util;

import java.util.ArrayList;
import java.util.List;

public class DayTypeUtils {

  public static final int MONDAY = 1;    // 2^0
  public static final int TUESDAY = 2;   // 2^1
  public static final int WEDNESDAY = 4; // 2^2
  public static final int THURSDAY = 8;  // 2^3
  public static final int FRIDAY = 16;   // 2^4
  public static final int SATURDAY = 32; // 2^5
  public static final int SUNDAY = 64;   // 2^6

  public static int encodeDays(boolean... days) {
    int encoded = 0;
    for (int i = 0; i < days.length; i++) {
      if (days[i]) {
        encoded |= (1 << i);
      }
    }
    return encoded;
  }

  public static boolean isDayIncluded(int dayType, int day) {
    return (dayType & day) != 0;
  }

  public static List<String> decodeDays(int dayType) {
    List<String> days = new ArrayList<>();
    if ((dayType & MONDAY) != 0) {
      days.add("월");
    }
    if ((dayType & TUESDAY) != 0) {
      days.add("화");
    }
    if ((dayType & WEDNESDAY) != 0) {
      days.add("수");
    }
    if ((dayType & THURSDAY) != 0) {
      days.add("목");
    }
    if ((dayType & FRIDAY) != 0) {
      days.add("금");
    }
    if ((dayType & SATURDAY) != 0) {
      days.add("토");
    }
    if ((dayType & SUNDAY) != 0) {
      days.add("일");
    }
    return days;
  }
}
