package com.devonoff.domain.qnapost.controller;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.qnapost.dto.QnaCommentDto;
import com.devonoff.domain.qnapost.dto.*;
import com.devonoff.domain.qnapost.service.QnaPostService;
import com.devonoff.util.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QnaPostController.class)
@Import(SecurityConfig.class) // SecurityConfig를 명시적으로 포함 (Optional)
@AutoConfigureMockMvc(addFilters = false)
class QnaPostControllerTest {

  @MockBean
  JwtProvider jwtProvider;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private QnaPostService qnaPostService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("질의 응답 게시글 생성")
  void createQnaPost() throws Exception {
    QnaPostRequest request = new QnaPostRequest();
    request.setAuthor("test@example.com");

    when(qnaPostService.createQnaPost(any(QnaPostRequest.class), anyString()))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(multipart("/api/qna-posts")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .param("author", request.getAuthor()))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("질의 응답 게시글 수정")
  void updateQnaPost() throws Exception {
    QnaPostUpdateDto updateDto = new QnaPostUpdateDto();
    QnaPostDto response = new QnaPostDto();
    response.setId(1L);

    when(qnaPostService.updateQnaPost(anyLong(), any(QnaPostUpdateDto.class)))
        .thenReturn(response);

    mockMvc.perform(multipart("/api/qna-posts/{qnaPostId}", 1L)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .param("title", "Updated Title"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("질의 응답 게시글 목록 조회")
  void getQnaPostList() throws Exception {
    Page<QnaPostDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
    when(qnaPostService.getQnaPostList(any(Pageable.class), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/api/qna-posts")
            .param("search", "test"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("사용자별 질의 응답 게시글 목록 조회")
  void getQnaPostByUserIdList() throws Exception {
    Page<QnaPostDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
    when(qnaPostService.getQnaPostByUserIdList(anyLong(), any(Pageable.class), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/api/qna-posts/author/{userId}", 1L)
            .param("search", "test"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("질의 응답 게시글 상세 조회")
  void getQnaPost() throws Exception {
    QnaPostDto response = new QnaPostDto();
    response.setId(1L);

    when(qnaPostService.getQnaPost(anyLong()))
        .thenReturn(response);

    mockMvc.perform(get("/api/qna-posts/{qnaPostId}", 1L))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("질의 응답 게시글 삭제")
  void deleteQnaPost() throws Exception {
    doNothing().when(qnaPostService).deleteQnaPost(anyLong());

    mockMvc.perform(delete("/api/qna-posts/{qnaPostId}", 1L))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 생성 - 성공")
  void testCreateQnaPostComment_Success() throws Exception {
    // given
    Long qnaPostId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    QnaCommentDto qnaCommentDto = QnaCommentDto.builder().id(1L).build();

    when(qnaPostService.createQnaPostComment(eq(qnaPostId), eq(qnaCommentRequest)))
        .thenReturn(qnaCommentDto);

    // when & then
    mockMvc.perform(post("/api/qna-posts/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(qnaCommentRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 조회 - 성공")
  void testGetQnaPostComments_Success() throws Exception {
    // given
    QnaCommentResponse qnaCommentResponse = QnaCommentResponse.builder()
        .id(1L)
        .build();

    Page<QnaCommentResponse> page = new PageImpl<>(
        Collections.singletonList(qnaCommentResponse)
    );

    when(qnaPostService.getQnaPostComments(anyLong(), any()))
        .thenReturn(page);

    // when & then
    mockMvc.perform(get("/api/qna-posts/1/comments")
            .param("page", "0"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 수정 - 성공")
  void testUpdateQnaPostComment_Success() throws Exception {
    // given
    Long commentId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    QnaCommentDto qnaCommentDto = QnaCommentDto.builder().id(1L).build();

    when(qnaPostService.updateQnaPostComment(eq(commentId), eq(qnaCommentRequest)))
        .thenReturn(qnaCommentDto);

    // when & then
    mockMvc.perform(put("/api/qna-posts/comments/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(qnaCommentRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  void testDeleteQnaPostComment_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/qna-posts/comments/1"))
        .andExpect(status().isOk());

    verify(qnaPostService).deleteQnaPostComment(1L);
  }

  @Test
  @DisplayName("대댓글 생성 - 성공")
  void testCreateQnaPostReply_Success() throws Exception {
    // given
    Long commentId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    QnaReplyDto qnaReplyDto = QnaReplyDto.builder().id(1L).build();

    when(qnaPostService.createQnaPostReply(eq(commentId), eq(qnaReplyRequest)))
        .thenReturn(qnaReplyDto);

    // when & then
    mockMvc.perform(post("/api/qna-posts/comments/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(qnaReplyRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("대댓글 수정 - 성공")
  void testUpdateQnaPostReply_Success() throws Exception {
    // given
    Long replyId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    QnaReplyDto qnaReplyDto = QnaReplyDto.builder().id(1L).build();

    when(qnaPostService.updateQnaPostReply(eq(replyId), eq(qnaReplyRequest)))
        .thenReturn(qnaReplyDto);

    // when & then
    mockMvc.perform(put("/api/qna-posts/replies/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(qnaReplyRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("대댓글 삭제 - 성공")
  void testDeleteQnaPostReply_Success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/qna-posts/replies/1"))
        .andExpect(status().isOk());

    verify(qnaPostService).deleteQnaPostReply(1L);
  }
}