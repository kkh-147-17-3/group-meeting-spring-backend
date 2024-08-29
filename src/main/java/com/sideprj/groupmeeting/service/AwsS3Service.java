package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.exceptions.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class AwsS3Service {

    private final S3Client s3Client;

    public AwsS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void deleteImage(String bucketName, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String uploadImage(String fileName, String bucketName, String keyPrefix, MultipartFile file) throws IOException, BadRequestException {
        fileName = (fileName == null) ? UUID.randomUUID().toString() : fileName;
        var contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            throw new BadRequestException("프로필 이미지는 이미지 형식만 업로드 가능합니다. 업로드한 형식: %s".formatted(contentType));
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(String.format("%s/%s", keyPrefix, fileName)).contentType(file.getContentType()).build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        fileName = keyPrefix == null || keyPrefix.isEmpty() ? fileName : String.format("%s/%s", keyPrefix, fileName);

        return fileName;
    }
}