package com.amazon.s3.service;

import com.amazon.s3.exception.FileDownloadException;
import com.amazon.s3.exception.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3StorageService {
    String uploadFile(MultipartFile multipartFile) throws FileUploadException, IOException;
    Object downloadFile(String fileName) throws FileDownloadException, IOException;
    boolean delete(String fileName);
}
