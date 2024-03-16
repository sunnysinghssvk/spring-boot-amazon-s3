package com.amazon.s3.util;

import com.amazon.s3.constants.S3Constants;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Log4j2
@UtilityClass
public class S3Util {
    /**
     * Check if the File is Valid or Not
     * @param multipartFile
     * @return
     */
    public static boolean checkIfValidFile(MultipartFile multipartFile){
        log.info("Is File Empty: {}", multipartFile.isEmpty());
        if (Objects.isNull(multipartFile.getOriginalFilename()))
            return false;
        return !multipartFile.getOriginalFilename().trim().equals(S3Constants.EMPTY_STRING);
    }

    /**
     * Check if S3 Bucket is Empty or Not
     * @return
     */
    public static boolean isBucketEmpty(AmazonS3 s3Client, String bucketName) {
        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        if (result == null)
            return false;
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        if(objects.isEmpty())
            return true;
        else
            return false;
    }

    /**
     * Generate File Name for Uploading File
     * @param multipartFile
     * @return
     */
    public static String generateFileName(MultipartFile multipartFile) {
        String fileName = new Date().getTime()
                + S3Constants.HYPHEN
                + multipartFile.getOriginalFilename().replace(S3Constants.SPACE, S3Constants.UNDERSCORE);
        log.info("Generated file name: {}", fileName);
        return fileName;
    }
}
