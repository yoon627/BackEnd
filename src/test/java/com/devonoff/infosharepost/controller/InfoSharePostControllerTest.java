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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
  void createInfoSharePost_Success() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test-image.jpg",
        "image/jpeg",
        "mock image content".getBytes()
    );

    InfoSharePostDto mockRequestDto = InfoSharePostDto.builder()
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
  void getInfoSharePosts_Success() throws Exception {
    // given
    Page<InfoSharePostDto> page = new PageImpl<>(Collections.singletonList(
        InfoSharePostDto.builder().title("Test Title").build()
    ));

    when(infoSharePostService.getInfoSharePosts(0, ""))
        .thenReturn(page);

    // when & then
    mockMvc.perform(get("/api/info-posts")
            .param("page", "0")
            .param("search", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("Test Title"));
  }

  @Test
  void getInfoSharePostByPostId_Success() throws Exception {
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
  void updateInfoSharePost_Success() throws Exception {
    // Given
    Long postId = 1L;
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "updated-image.jpg",
        "image/jpeg",
        "updated mock image content".getBytes()
    );

    InfoSharePostDto mockRequestDto = InfoSharePostDto.builder()
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
  void deleteInfoSharePost_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/info-posts/1"))
        .andExpect(status().isOk());

    Mockito.verify(infoSharePostService).deleteInfoSharePost(1L);
  }
}
