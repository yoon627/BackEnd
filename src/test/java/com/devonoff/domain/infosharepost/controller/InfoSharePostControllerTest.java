package com.devonoff.domain.infosharepost.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.infosharepost.controller.InfoSharePostController;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentDto;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentRequest;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentResponse;
import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyRequest;
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
    InfoSharePostDto mockRequestDto = InfoSharePostDto.builder()
        .file(file)
        .title("Test Title")
        .description("Test Content")
        .build();

    InfoSharePostDto mockResponseDto = InfoSharePostDto.builder()
        .id(1L)
        .title(mockRequestDto.getTitle())
        .description(mockRequestDto.getDescription())
        .build();

    when(infoSharePostService.createInfoSharePost(mockRequestDto)).thenReturn(
        mockResponseDto);

    // When & Then
    mockMvc.perform(multipart("/api/info-posts")
            .file(file)
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
    InfoSharePostDto mockRequestDto = InfoSharePostDto.builder()
        .file(file)
        .title("Updated Title")
        .description("Updated Content")
        .build();
    Long postId = 1L;

    InfoSharePostDto mockResponseDto = InfoSharePostDto.builder()
        .id(postId)
        .title(mockRequestDto.getTitle())
        .description(mockRequestDto.getDescription())
        .build();

    when(infoSharePostService.updateInfoSharePost(postId, mockRequestDto)).thenReturn(
        mockResponseDto);

    // When & Then
    mockMvc.perform(multipart("/api/info-posts/{infoPostId}", postId)
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("특정 정보공유 게시글 삭제 - 성공")
  void testDeleteInfoSharePost_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/info-posts/1"))
        .andExpect(status().isOk());

    verify(infoSharePostService).deleteInfoSharePost(1L);
  }

  @Test
  @DisplayName("댓글 생성 - 성공")
  void testCreateInfoSharePostComment_Success() throws Exception {
    // given
    Long infoSharePostId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    InfoShareCommentDto infoShareCommentDto = InfoShareCommentDto.builder().id(1L).build();

    when(infoSharePostService.createInfoSharePostComment(eq(infoSharePostId), eq(infoShareCommentRequest)))
        .thenReturn(infoShareCommentDto);

    // when & then
    mockMvc.perform(post("/api/info-posts/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(infoShareCommentRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 조회 - 성공")
  void testGetInfoSharePostComments_Success() throws Exception {
    // given
    InfoShareCommentResponse infoShareCommentResponse = InfoShareCommentResponse.builder()
        .id(1L)
        .build();

    Page<InfoShareCommentResponse> page = new PageImpl<>(
        Collections.singletonList(infoShareCommentResponse)
    );

    when(infoSharePostService.getInfoSharePostComments(anyLong(), any()))
        .thenReturn(page);

    // when & then
    mockMvc.perform(get("/api/info-posts/1/comments")
            .param("page", "0"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 수정 - 성공")
  void testUpdateInfoSharePostComment_Success() throws Exception {
    // given
    Long infoShareCommentId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    InfoShareCommentDto infoShareCommentDto = InfoShareCommentDto.builder().id(1L).build();

    when(infoSharePostService.updateInfoSharePostComment(eq(infoShareCommentId), eq(infoShareCommentRequest)))
        .thenReturn(infoShareCommentDto);

    // when & then
    mockMvc.perform(put("/api/info-posts/comments/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(infoShareCommentRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  void testDeleteInfoSharePostComment_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/info-posts/comments/1"))
        .andExpect(status().isOk());

    verify(infoSharePostService).deleteInfoSharePostComment(1L);
  }

  @Test
  @DisplayName("대댓글 생성 - 성공")
  void testCreateInfoSharePostReply_Success() throws Exception {
    // given
    Long infoShareCommentId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    InfoShareReplyDto infoShareReplyDto = InfoShareReplyDto.builder().id(1L).build();

    when(infoSharePostService.createInfoSharePostReply(eq(infoShareCommentId), eq(infoShareReplyRequest)))
        .thenReturn(infoShareReplyDto);

    // when & then
    mockMvc.perform(post("/api/info-posts/comments/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(infoShareReplyRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("대댓글 수정 - 성공")
  void testUpdateInfoSharePostReply_Success() throws Exception {
    // given
    Long infoShareCommentId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    InfoShareReplyDto infoShareReplyDto = InfoShareReplyDto.builder().id(1L).build();

    when(infoSharePostService.updateInfoSharePostReply(eq(infoShareCommentId), eq(infoShareReplyRequest)))
        .thenReturn(infoShareReplyDto);

    // when & then
    mockMvc.perform(put("/api/info-posts/replies/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(infoShareReplyRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("대댓글 삭제 - 성공")
  void testDeleteInfoSharePostReply_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/info-posts/replies/1"))
        .andExpect(status().isOk());

    verify(infoSharePostService).deleteInfoSharePostReply(1L);
  }
}