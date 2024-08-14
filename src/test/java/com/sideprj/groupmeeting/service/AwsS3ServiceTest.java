package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AwsS3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private AwsS3Service awsS3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(awsS3Service, "AWS_S3_ACCESS_KEY", "testAccessKey");
        ReflectionTestUtils.setField(awsS3Service, "AWS_S3_SECRET_ACCESS_KEY", "testSecretKey");
    }

    @Test
    void deleteImage_shouldCallS3ClientDeleteObject() {
        String bucketName = "testBucket";
        String key = "testKey";

        awsS3Service.deleteImage(bucketName, key);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void uploadImage_withValidImageFile_shouldReturnFileName() throws IOException, BadRequestException {
        String fileName = "testImage.jpg";
        String bucketName = "testBucket";
        String keyPrefix = "testPrefix";
        MultipartFile file = new MockMultipartFile("file", fileName, "image/jpeg", "test image content".getBytes());

        String result = awsS3Service.uploadImage(fileName, bucketName, keyPrefix, file);

        assertEquals("testPrefix/testImage.jpg", result);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_withNullFileName_shouldGenerateUUID() throws IOException, BadRequestException {
        String bucketName = "testBucket";
        String keyPrefix = "testPrefix";
        MultipartFile file = new MockMultipartFile("file", "testImage.jpg", "image/jpeg", "test image content".getBytes());

        String result = awsS3Service.uploadImage(null, bucketName, keyPrefix, file);

        assertTrue(result.startsWith("testPrefix/"));
        assertTrue(result.endsWith(".jpg"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_withNonImageFile_shouldThrowBadRequestException() {
        String fileName = "testFile.txt";
        String bucketName = "testBucket";
        String keyPrefix = "testPrefix";
        MultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "test content".getBytes());

        assertThrows(BadRequestException.class, () ->
                awsS3Service.uploadImage(fileName, bucketName, keyPrefix, file)
        );

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_withEmptyKeyPrefix_shouldReturnFileNameWithoutPrefix() throws IOException, BadRequestException {
        String fileName = "testImage.jpg";
        String bucketName = "testBucket";
        String keyPrefix = "";
        MultipartFile file = new MockMultipartFile("file", fileName, "image/jpeg", "test image content".getBytes());

        String result = awsS3Service.uploadImage(fileName, bucketName, keyPrefix, file);

        assertEquals(fileName, result);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}