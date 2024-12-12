package com.devonoff.domain.student.dto;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {

  private Long studentId;
  private UserDto user;
  private String nickname;
  private Boolean isLeader;

  public static StudentDto fromEntity(Student student) {
    return StudentDto.builder()
        .studentId(student.getId())
        .user(UserDto.fromEntity(student.getUser()))
        .nickname(student.getUser().getNickname())
        .isLeader(student.getIsLeader())
        .build();
  }
}