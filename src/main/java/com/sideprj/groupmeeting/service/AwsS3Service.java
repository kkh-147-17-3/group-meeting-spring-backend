package com.sideprj.groupmeeting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class AwsS3Service {

    @Value("${aws.s3.access_key}")
    private String AWS_S3_ACCESS_KEY;

    @Value("${aws.s3.secret_access_key}")
    private String AWS_S3_SECRET_ACCESS_KEY;

    private final S3Client s3Client;

    public AwsS3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.of("ap-northeast-2"))
                .credentialsProvider(() -> AwsBasicCredentials.create(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_ACCESS_KEY))
                .build();
    }

    public void deleteImage(String bucketName, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String uploadImage(String fileName, String bucketName, String keyPrefix,MultipartFile file) throws IOException {
        fileName = (fileName == null) ? UUID.randomUUID().toString() : fileName;

        var ext = Path.of(fileName).getFileName().toString().toLowerCase();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(String.format("%s/%s", keyPrefix, fileName))
                .contentType("image/" + ext)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }
}