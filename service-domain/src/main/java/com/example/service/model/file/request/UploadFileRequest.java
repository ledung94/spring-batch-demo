package com.example.service.model.file.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileRequest {
    private MultipartFile file;
    private boolean hasHeader = Boolean.TRUE;
}
