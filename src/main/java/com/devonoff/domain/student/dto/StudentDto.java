package com.devonoff.domain.student.dto;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {

  private Long studentId;
  private UserDto user;
  private Boolean isLeader;

  public static StudentDto fromEntity(Student student) {
    return StudentDto.builder()
        .studentId(student.getId())
        .user(UserDto.fromEntity(student.getUser()))
        .isLeader(student.getIsLeader())
        .build();
  }
}