package com.devonoff.domain.photo.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.devonoff.domain.user.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PhotoService {

  private final AmazonS3Client amazonS3Client;
  private final AuthService authService;
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.region.static}")
  private String region;
  private String urlPrefix;
  @Value("${cloud.aws.s3.default-thumbnail-image-url}")
  private String defaultThumbnailImageUrl;
  @Value("${cloud.aws.s3.default-profile-image-url}")
  private String defaultProfileImageUrl;

  @PostConstruct
  public void init() {
    urlPrefix = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
  }

  public String save(MultipartFile file) {

    Long userId = authService.getLoginUserId();
    try {
      String fileName = file.getOriginalFilename();
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(file.getContentType());
      metadata.setContentLength(file.getSize());
      if (amazonS3Client.doesObjectExist(bucket, userId + "/" + fileName)) {
        return urlPrefix + userId + "/" + fileName;
      }
      this.amazonS3Client.putObject(bucket, userId + "/" + fileName,
          file.getInputStream(),
          metadata);
      return urlPrefix + userId + "/" + fileName;
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public void delete(String fileUrl) {
    if (fileUrl.equals(defaultThumbnailImageUrl) || fileUrl.equals(defaultProfileImageUrl)) {
      return;
    }
    try {
      this.amazonS3Client.deleteObject(bucket, fileUrl.substring(urlPrefix.length()));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}