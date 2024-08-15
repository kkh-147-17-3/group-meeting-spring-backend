package com.sideprj.groupmeeting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.awscore.AwsClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${aws.s3.access_key}")
    private String AWS_S3_ACCESS_KEY;
    @Value("${aws.s3.secret_access_key}")
    private String AWS_S3_SECRET_ACCESS_KEY;

    @Bean
    public S3Client awsClient() {

        return S3Client.builder()
                       .region(Region.of("ap-northeast-2"))
                       .credentialsProvider(() -> AwsBasicCredentials.create(
                               AWS_S3_ACCESS_KEY,
                               AWS_S3_SECRET_ACCESS_KEY
                       ))
                       .build();
    }
}
