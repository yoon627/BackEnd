package com.devonoff.domain.photo.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PhotoService {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.region.static}")
  private String region;

  private String urlPrefix;

  @PostConstruct
  public void init() {
    urlPrefix = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
  }

  public String save(MultipartFile file) {

    //TODO UserService에서 로그인된 유저 ID를 주도록 추가 고려
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    Long userId = Long.parseLong(userDetails.getUsername());
    try {
      String fileName = file.getOriginalFilename();
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(file.getContentType());
      metadata.setContentLength(file.getSize());
      if (amazonS3Client.doesObjectExist(bucket, userId + "/" + fileName)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "File already exists");
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
    this.amazonS3Client.deleteObject(bucket, fileUrl.substring(urlPrefix.length()));
  }
}
