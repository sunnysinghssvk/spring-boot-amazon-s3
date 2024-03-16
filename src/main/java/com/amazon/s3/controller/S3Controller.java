package com.amazon.s3.controller;

import com.amazon.s3.constants.S3Constants;
import com.amazon.s3.dto.S3ApiResponse;
import com.amazon.s3.exception.FileDownloadException;
import com.amazon.s3.exception.FileEmptyException;
import com.amazon.s3.exception.FileUploadException;
import com.amazon.s3.service.S3StorageService;
import com.amazon.s3.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/s3/file")
public class S3Controller {
    private final S3StorageService s3StorageService;

    /**
     * GET Endpoint for Downloading File from S3 Bucket
     * @param fileName
     * @return
     * @throws FileDownloadException
     * @throws IOException
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("fileName") String fileName) throws FileDownloadException, IOException {
        Object response = s3StorageService.downloadFile(fileName);
        S3ApiResponse apiResponse;
        HttpStatus httpStatus;
        ResponseEntity<?> responseEntity;
        if (response != null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            apiResponse = S3ApiResponse.builder()
                    .message("File Downloaded Successfully: " + fileName)
                    .httpStatusCode(200)
                    .fileData(response)
                    .build();
            httpStatus = HttpStatus.OK;
            responseEntity = new ResponseEntity<>(apiResponse, httpHeaders, httpStatus);
        } else {
            apiResponse = S3ApiResponse.builder()
                    .message("File Not Found - Failed to Download: " + fileName)
                    .httpStatusCode(400)
                    .build();
            httpStatus = HttpStatus.NOT_FOUND;
            responseEntity = new ResponseEntity<>(apiResponse, httpStatus);
        }
        log.info("Download File Response: {}", apiResponse);
        return responseEntity;
    }

    /**
     * POST Endpoint for Uploading File to S3 Bucket
     * @param multipartFile
     * @return
     * @throws FileEmptyException
     * @throws FileUploadException
     * @throws IOException
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws FileEmptyException, FileUploadException, IOException {
        if (multipartFile.isEmpty()) {
            log.error("Failed to upload since the file is Empty");
            throw new FileEmptyException("File is Empty");
        }
        boolean isValidFile = S3Util.checkIfValidFile(multipartFile);
        List<String> allowedFileExtensions = new ArrayList<>(S3Constants.ALLOWED_FORMATS);
        S3ApiResponse apiResponse;
        HttpStatus httpStatus;
        if (isValidFile && allowedFileExtensions.contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))) {
            String fileName = s3StorageService.uploadFile(multipartFile);
             apiResponse = S3ApiResponse.builder()
                    .message("File Uploaded Successfully: " + fileName)
                    .httpStatusCode(200)
                    .build();
            httpStatus = HttpStatus.OK;
        } else {
            apiResponse = S3ApiResponse.builder()
                    .message("Invalid File - Failed to Upload")
                    .httpStatusCode(400)
                    .build();
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        log.info("Upload File Response: {}", apiResponse);
        return new ResponseEntity<>(apiResponse, httpStatus);
    }

    /**
     * DELETE Endpoint for deleting File from S3 Bucket
     * @param fileName
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam("fileName") String fileName) {
        boolean isDeleted = s3StorageService.delete(fileName);
        S3ApiResponse apiResponse;
        HttpStatus httpStatus;
        if (isDeleted) {
            apiResponse = S3ApiResponse.builder()
                    .message("File Deleted Successfully: " + fileName)
                    .httpStatusCode(200)
                    .build();
            httpStatus = HttpStatus.OK;
        } else {
            apiResponse = S3ApiResponse.builder()
                    .message("File Not Found: " + fileName)
                    .httpStatusCode(404)
                    .build();
            httpStatus = HttpStatus.NOT_FOUND;
        }
        log.info("Delete File Response: {}", apiResponse);
        return new ResponseEntity<>(apiResponse, httpStatus);
    }
}
