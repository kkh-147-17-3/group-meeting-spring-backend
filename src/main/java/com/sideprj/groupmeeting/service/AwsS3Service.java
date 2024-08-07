package com.sideprj.groupmeeting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class AwsS3Service {

    private final S3Client s3Client;

    public AwsS3Service() {
        this.s3Client = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of("ap-northeast-2"))
                .credentialsProvider(() -> software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(
                        configService.get("AWS_S3_ACCESS_KEY"),
                        configService.get("AWS_S3_SECRET_ACCESS_KEY")))
                .build();
    }

    public void deleteImage(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(configService.get("AWS_S3_BUCKET_NAME"))
                .key("profile/" + key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String uploadImage(String fileName, MultipartFile file) throws IOException {
        fileName = (fileName == null) ? UUID.randomUUID().toString() : fileName;

        var ext = Path.of(fileName).getFileName().toString().toLowerCase();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(configService.get("AWS_S3_BUCKET_NAME"))
                .key("profile/" + fileName)
                .contentType("image/" + ext)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }
}