package org.vrajpatel.personalexpense.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.responseDto.PresignedUrlResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final Logger log= LoggerFactory.getLogger(S3Service.class);
    private final S3Presigner presigner;

    private final S3Client s3Client;

    @Value("${aws.bucket_name}")
    private String bucketName;


    public S3Service(S3Presigner presigner, S3Client s3Client) {
        this.presigner=presigner;
        this.s3Client=s3Client;
    }

    public String generateGetPresignedUrl(String key){
        GetObjectRequest getObjectRequest= GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest=GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(30)).getObjectRequest(getObjectRequest).build();

        PresignedGetObjectRequest request=presigner.presignGetObject(presignRequest);

        return request.url().toString();
    }

    public PresignedUrlResponse generatePresignedUrl(String fileName, String contentType, long fileSize)
    {
        String key = "uploads/"+UUID.randomUUID()+"/"+ fileName;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                            .bucket(bucketName)
                                            .key(key)
                                            .contentType(contentType)
                                            .contentLength(fileSize)
                                            .build();
        PutObjectPresignRequest presignRequest=PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(5)).putObjectRequest(putObjectRequest).build();
        PresignedPutObjectRequest presignedRequest=presigner.presignPutObject(presignRequest);
        return new PresignedUrlResponse(presignedRequest.url().toString(),key);
    }

    public void deleteObject(String receiptFileUrl) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(receiptFileUrl)
                .build();
        s3Client.deleteObject(deleteRequest);
    }
}
