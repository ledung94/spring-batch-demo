package com.example.service.controller;

import com.example.service.model.file.request.UploadFileRequest;
import com.example.service.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("file/v1.0/")
public class FileController {
    private final FileService fileService;

    @PostMapping("upload")
    public void uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam(value = "hasHeader", required = false, defaultValue = "true") boolean hasHeader) {
        UploadFileRequest request = new UploadFileRequest();
        request.setFile(file);
        request.setHasHeader(hasHeader);
        fileService.upload(request);
    }
}
