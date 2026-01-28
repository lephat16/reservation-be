package com.example.ReservationApp.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

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
            // 画像ファイルかどうかをチェック
            if (file.getContentType() == null
                    || !file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("無効な画像ファイルです");
            }

            // ファイル名の重複を防ぐためUUIDを付与
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = storageLocation.resolve(fileName);

            // ファイルを保存（同名ファイルがある場合は上書き）
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 保存された画像のパスを返す
            return "/uploads/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ファイルの保存に失敗しました: " + file.getOriginalFilename());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank())
                return;

            String fileName = Paths.get(fileUrl).getFileName().toString();
            Path filePath = storageLocation.resolve(fileName).normalize();

            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String storeBase64Image(String base64Image) throws IOException {
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }
        byte[] bytes = Base64.getDecoder().decode(base64Image);
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        Path path = Paths.get("uploads/" + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);
        return "/uploads/" + fileName;
    }
}
