package com.amazon.s3.controller;

import com.amazon.s3.exception.FileDownloadException;
import com.amazon.s3.exception.FileEmptyException;
import com.amazon.s3.exception.SpringBootFileUploadException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;

@Log4j2
@ControllerAdvice
public class S3ExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {FileEmptyException.class})
    protected ResponseEntity<Object> handleFileEmptyException(FileEmptyException ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        return handleExceptionInternal(ex, exceptionMessage, new HttpHeaders(), HttpStatus.NO_CONTENT, request);
    }

    @ExceptionHandler(value = {FileDownloadException.class})
    protected ResponseEntity<Object> handleFileDownloadException(FileDownloadException ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        return handleExceptionInternal(ex, exceptionMessage, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {SpringBootFileUploadException.class})
    protected ResponseEntity<Object> handleConflict(SpringBootFileUploadException ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        return handleExceptionInternal(ex, exceptionMessage, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {AmazonServiceException.class})
    protected ResponseEntity<Object> handleAmazonServiceException(RuntimeException ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        return handleExceptionInternal(ex, exceptionMessage, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {SdkClientException.class})
    protected ResponseEntity<Object> handleSdkClientException(RuntimeException ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        return handleExceptionInternal(ex, exceptionMessage, new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(value = {IOException.class, FileNotFoundException.class, MultipartException.class})
    protected ResponseEntity<Object> handleIOException(Exception ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        return handleExceptionInternal(ex, exceptionMessage, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleUnexpectedException(Exception ex, WebRequest request) {
        String exceptionMessage = ex.getMessage();
        log.info("Exception occurred: {}, {}", exceptionMessage, ex);
        return handleExceptionInternal(ex, "Internal Server Error", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
