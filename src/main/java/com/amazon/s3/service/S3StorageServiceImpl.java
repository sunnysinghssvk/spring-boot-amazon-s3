package com.amazon.s3.service;

import com.amazon.s3.constants.S3Constants;
import com.amazon.s3.exception.FileDownloadException;
import com.amazon.s3.exception.FileUploadException;
import com.amazon.s3.util.S3Util;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Service
@Log4j2
public class S3StorageServiceImpl implements S3StorageService {
    @Value("${s3.bucket-name}")
    private String bucketName;
    private final AmazonS3 s3Client;

    /**
     * Upload File to S3 Bucket
     * @param multipartFile
     * @return
     * @throws FileUploadException
     * @throws IOException
     */
    @Override
    public String uploadFile(MultipartFile multipartFile) throws FileUploadException, IOException {
        File file = new File(multipartFile.getOriginalFilename());
        String fileName = S3Util.generateFileName(multipartFile);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipartFile.getBytes());
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(S3Constants.PLAIN_SLASH + FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
            metadata.addUserMetadata(S3Constants.TITLE, S3Constants.FILE_UPLOAD + fileName);
            metadata.setContentLength(file.length());
            request.setMetadata(metadata);
            s3Client.putObject(request);
            file.delete();
        } catch(Exception e) {
            log.error("Exception occurred while uploading file: {}, {}, {}", fileName, e.getMessage(), e);
            throw new FileUploadException("Failed to upload file: " + fileName + e.getMessage());
        }
        return fileName;
    }

    /**
     * Download File from S3 Bucket
     * @param fileName
     * @return
     * @throws FileDownloadException
     * @throws IOException
     */
    @Override
    public Object downloadFile(String fileName) throws FileDownloadException, IOException {
        if (S3Util.isBucketEmpty(s3Client, bucketName))
            throw new FileDownloadException("S3-Bucket doesn't exists or is empty");
        S3Object object = s3Client.getObject(bucketName, fileName);
        try {
            S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] readBuffer = new byte[1024];
            int read_len = 0;
            while ((read_len = s3ObjectInputStream.read(readBuffer)) > 0) {
                fileOutputStream.write(readBuffer, 0, read_len);
            }
            Path pathObject = Paths.get(fileName);
            Resource resource = new UrlResource(pathObject.toUri());
            if (resource.exists() || resource.isReadable()) {
                log.info("File: {} got downloaded successfully", fileName);
                return resource;
            } else {
                throw new FileDownloadException("Not able to locate the file: " + fileName);
            }
        } catch (Exception e) {
            log.error("Exception occurred while downloading file: {}, {}, {}", fileName, e.getMessage(), e);
            throw new FileDownloadException("Not able to download the file: " + fileName);
        }
    }

    /**
     * Delete File from S3 Bucket
     * @param fileName
     * @return
     */
    @Override
    public boolean delete(String fileName) {
        File file = Paths.get(fileName).toFile();
        if (file.exists()) {
            file.delete();
            log.info("File: {} got successfully deleted", fileName);
            return true;
        } else {
            log.info("File: {} doesn't exists", fileName);
        }
        return false;
    }
}
