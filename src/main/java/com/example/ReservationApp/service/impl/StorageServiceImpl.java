package com.example.ReservationApp.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ReservationApp.service.StorageService;

/**
 * StorageServiceImplクラスは、ファイルのアップロードと保存を処理。
 * アップロードされたファイルはローカルの "uploads" ディレクトリに保存。
 */
@Service
public class StorageServiceImpl implements StorageService {

    private final Path storageLocation = Paths.get("uploads");

    /**
     * コンストラクタ。保存用ディレクトリが存在しない場合は作成。
     * 
     * @throws IOException ディレクトリ作成に失敗した場合
     */
    public StorageServiceImpl() throws IOException {
        Files.createDirectories(storageLocation);
    }

    /**
     * アップロードされたファイルを保存。
     * ファイル名が同じ場合は上書き。
     * 
     * @param file 保存するMultipartFile
     * @return 保存先のファイルパス文字列。ファイルが空の場合はnullを返。
     * @throws RuntimeException ファイルの保存に失敗した場合
     */
    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return null;
            }
            Path targetLocation = storageLocation.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file" + file.getOriginalFilename());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Path filePath = storageLocation.resolve(fileUrl).normalize();
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
        }
    }
}
