package com.devonoff.infosharepost.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.infosharepost.controller.InfoSharePostController;
import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.service.InfoSharePostService;
import com.devonoff.util.JwtAuthenticationFilter;
import com.devonoff.util.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InfoSharePostController.class)
@Import(SecurityConfig.class) // SecurityConfig를 명시적으로 포함 (Optional)
@AutoConfigureMockMvc(addFilters = false)
class InfoSharePostControllerTest {

  @MockBean
  private JwtProvider jwtProvider;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private InfoSharePostService infoSharePostService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private InfoSharePostDto samplePost;

  @BeforeEach
  void setUp() {
    samplePost = new InfoSharePostDto();
    samplePost.setId(1L);
    samplePost.setTitle("Sample Title");
    samplePost.setDescription("Sample Content");
    samplePost.setUserId(1L);
  }

  @Test
  void testCreateInfoSharePost() throws Exception {
    // given
    Mockito.when(infoSharePostService.createInfoSharePost(any(InfoSharePostDto.class)))
        .thenReturn(samplePost);

    // when
    mockMvc.perform(post("/api/info-posts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(samplePost)))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.title").value("Sample Title"));
  }

  @Test
  void testGetInfoSharePosts() throws Exception {
    // given
    Page<InfoSharePostDto> page = new PageImpl<>(List.of(samplePost));
    Mockito.when(infoSharePostService.getInfoSharePosts(anyInt(), anyString()))
        .thenReturn(page);

    // when
    mockMvc.perform(get("/api/info-posts")
            .param("page", "0")
            .param("search", "Sample"))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1L));
  }

  @Test
  void testGetInfoSharePostsByUserId() throws Exception {
    // given
    Page<InfoSharePostDto> page = new PageImpl<>(List.of(samplePost));
    Mockito.when(infoSharePostService.getInfoSharePostsByUserId(anyLong(), anyInt(), anyString()))
        .thenReturn(page);

    // when
    mockMvc.perform(get("/api/info-posts/author/1")
            .param("page", "0")
            .param("search", "Sample"))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1L));
  }

  @Test
  void testGetInfoSharePostByPostId() throws Exception {
    // given
    Mockito.when(infoSharePostService.getInfoSharePostByPostId(anyLong()))
        .thenReturn(samplePost);

    // when
    mockMvc.perform(get("/api/info-posts/1"))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.title").value("Sample Title"));
  }

  @Test
  void testUpdateInfoSharePost() throws Exception {
    // given
    Mockito.when(infoSharePostService.updateInfoSharePost(anyLong(), any(InfoSharePostDto.class)))
        .thenReturn(samplePost);

    // when
    mockMvc.perform(put("/api/info-posts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(samplePost)))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.title").value("Sample Title"));
  }

  @Test
  void testDeleteInfoSharePost() throws Exception {
    // when
    mockMvc.perform(delete("/api/info-posts/1"))

        // then
        .andExpect(status().isOk());
  }
}
