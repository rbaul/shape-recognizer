package com.github.rbaul.recognizer.service;

import com.github.rbaul.recognizer.dtos.ImageDataDto;
import com.github.rbaul.recognizer.exceptions.RecognizerException;
import com.github.rbaul.recognizer.shape.models.Shape;
import com.github.rbaul.recognizer.shape.service.OpenCvRecognizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ImageRecognizerService {

    private final OpenCvRecognizerService openCvRecognizerService;
    public List<Shape> recognizeShape(ImageDataDto data, MultipartFile image) {
        try {
            return openCvRecognizerService.recognizeAllShapes(image.getBytes());
        } catch (IOException e) {
            throw new RecognizerException("Failed read image", e);
        }
    }

    public byte[] getRecognizeShapeImage(ImageDataDto data, MultipartFile image) {
        try {
            return openCvRecognizerService.getRecognizeShapeImage(image.getBytes());
        } catch (IOException e) {
            throw new RecognizerException("Failed read image", e);
        }
    }
}
