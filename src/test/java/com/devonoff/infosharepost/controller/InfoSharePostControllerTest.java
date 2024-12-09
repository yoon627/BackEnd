package com.devonoff.infosharepost.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.infosharepost.controller.InfoSharePostController;
import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.service.InfoSharePostService;
import com.devonoff.util.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InfoSharePostController.class)
@Import(SecurityConfig.class) // SecurityConfig를 명시적으로 포함 (Optional)
@AutoConfigureMockMvc(addFilters = false)
class InfoSharePostControllerTest {

  @MockBean
  JwtProvider jwtProvider;
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private InfoSharePostService infoSharePostService;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("정보공유 게시글 생성 - 성공")
  void testCreateInfoSharePost_Success() throws Exception {
    // given
    MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        "image content".getBytes());
    InfoSharePostDto requestDto = InfoSharePostDto.builder()
        .title("Test Title")
        .description("Test Content")
        .build();

    MockMultipartFile data = new MockMultipartFile(
        "data",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(mockRequestDto)
    );

    InfoSharePostDto mockResponseDto = InfoSharePostDto.builder()
        .id(1L)
        .title(mockRequestDto.getTitle())
        .description(mockRequestDto.getDescription())
        .build();

    when(infoSharePostService.createInfoSharePost(mockRequestDto, file)).thenReturn(
        mockResponseDto);

    // When & Then
    mockMvc.perform(multipart("/api/info-posts")
            .file(file)
            .file(data)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("정보공유 게시글 페이지 조회 - 성공")
  void testGetInfoSharePosts_Success() throws Exception {
    // given
    Page<InfoSharePostDto> page = new PageImpl<>(Collections.singletonList(
        InfoSharePostDto.builder().title("Test Title").build()
    ));

    Pageable pageable = PageRequest.of(0, 12);
    Mockito.when(infoSharePostService.getInfoSharePosts(pageable, ""))
        .thenReturn(page);

    // when & then
    mockMvc.perform(get("/api/info-posts")
            .param("page", "0")
            .param("size", "12")
            .param("search", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("Test Title"));
  }

  @Test
  @DisplayName("특정 정보공유 게시글 조회 - 성공")
  void testGetInfoSharePostByPostId_Success() throws Exception {
    // given
    InfoSharePostDto responseDto = InfoSharePostDto.builder()
        .title("Test Title")
        .description("Test Description")
        .build();

    when(infoSharePostService.getInfoSharePostByPostId(anyLong()))
        .thenReturn(responseDto);

    // when & then
    mockMvc.perform(get("/api/info-posts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Test Title"));
  }

  @Test
  @DisplayName("특정 정보공유 게시글 수정 - 성공")
  void testUpdateInfoSharePost_Success() throws Exception {
    // given
    MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
        "image content".getBytes());
    InfoSharePostDto requestDto = InfoSharePostDto.builder()
        .title("Updated Title")
        .description("Updated Content")
        .build();

    MockMultipartFile data = new MockMultipartFile(
        "data",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(mockRequestDto)
    );

    InfoSharePostDto mockResponseDto = InfoSharePostDto.builder()
        .id(postId)
        .title(mockRequestDto.getTitle())
        .description(mockRequestDto.getDescription())
        .build();

    when(infoSharePostService.updateInfoSharePost(postId, mockRequestDto, file)).thenReturn(
        mockResponseDto);

    // When & Then
    mockMvc.perform(multipart("/api/info-posts/{infoPostId}", postId)
            .file(file)
            .file(data)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("특정 정보공유 게시글 삭제 - 성공")
  void testDeleteInfoSharePost_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/info-posts/1"))
        .andExpect(status().isOk());

    Mockito.verify(infoSharePostService).deleteInfoSharePost(1L);
  }
}
