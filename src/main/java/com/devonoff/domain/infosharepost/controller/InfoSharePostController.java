package com.devonoff.domain.infosharepost.controller;

import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.service.InfoSharePostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/info-posts")
@RequiredArgsConstructor
public class InfoSharePostController {

  private final InfoSharePostService infoSharePostService;

  @PostMapping
  public ResponseEntity<InfoSharePostDto> createInfoSharePost(
      @RequestParam("file") MultipartFile file,
      @RequestBody InfoSharePostDto infoSharePost) {
    var result = this.infoSharePostService.createInfoSharePost(infoSharePost, file);
    return ResponseEntity.ok(result);
  }

  @GetMapping
  public ResponseEntity<Page<InfoSharePostDto>> getInfoSharePosts(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "") String search) {
    var result = this.infoSharePostService.getInfoSharePosts(page, search);
    return ResponseEntity.ok(result);
  }

  //TODO userId -> nickname으로 변경해야함, InfoSharePost 테이블에 nickname추가?
  @GetMapping("/author/{userId}")
  public ResponseEntity<Page<InfoSharePostDto>> getInfoSharePostsByUserId(@PathVariable Long userId,
      @RequestParam Integer page, @RequestParam String search) {
    var result = this.infoSharePostService.getInfoSharePostsByUserId(userId, page, search);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{infoPostId}")
  public ResponseEntity<InfoSharePostDto> getInfoSharePostByPostId(
      @PathVariable Long infoPostId) {
    var result = this.infoSharePostService.getInfoSharePostByPostId(infoPostId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{infoPostId}")
  public ResponseEntity<InfoSharePostDto> updateInfoSharePost(@PathVariable Long infoPostId,
      @RequestParam("file") MultipartFile file,
      @RequestBody InfoSharePostDto infoSharePostDto) {
    var result = this.infoSharePostService.updateInfoSharePost(infoPostId, infoSharePostDto, file);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{infoPostId}")
  public void deleteInfoSharePost(@PathVariable Long infoPostId) {
    this.infoSharePostService.deleteInfoSharePost(infoPostId);
  }
}
