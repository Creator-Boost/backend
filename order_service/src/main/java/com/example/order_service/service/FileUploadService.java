package com.example.order_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    @Autowired
    private Cloudinary cloudinary;

    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Generate unique public ID for Cloudinary
                    String publicId = "delivery-files/" + UUID.randomUUID().toString();

                    // Upload to Cloudinary
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "raw",        // 👈 This is important for PDFs
                            "upload_preset", "ml_default", // 👈 Use your public preset
                            "folder", "uploads"            // optional, for organization
                        ));

                    // Get the secure URL from Cloudinary response
                    String secureUrl = (String) uploadResult.get("secure_url");
                    fileUrls.add(secureUrl);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload files to Cloudinary", e);
        }

        return fileUrls;
    }
}
