package com.example.ReservationApp.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeFile(MultipartFile file);
    void deleteFile(String fileUrl);
}
