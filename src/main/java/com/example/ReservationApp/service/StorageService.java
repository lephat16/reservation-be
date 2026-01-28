package com.example.ReservationApp.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeFile(MultipartFile file);
    void deleteFile(String fileUrl);
    String storeBase64Image(String base64Image) throws IOException;
}
