package com.github.rbaul.recognizer.service;

import com.github.rbaul.recognizer.dtos.ImageDataDto;
import com.github.rbaul.recognizer.exceptions.RecognizerException;
import com.github.rbaul.recognizer.shape.models.Shape;
import com.github.rbaul.recognizer.shape.models.Text;
import com.github.rbaul.recognizer.shape.service.OpenCvRecognizerService;
import com.github.rbaul.recognizer.shape.service.TesseractTextRecognizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ImageRecognizerService {

    private final OpenCvRecognizerService openCvRecognizerService;

    private final TesseractTextRecognizerService tesseractTextRecognizerService;

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

    public byte[] getRecognizeShapeImageByTemplate(ImageDataDto data, MultipartFile image, List<MultipartFile> templates) {
        try {
            return openCvRecognizerService.getRecognizeShapeImageByTemplate(image.getBytes(), templates.stream().map(multipartFile -> {
                try {
                    return multipartFile.getBytes();
                } catch (IOException e) {
                    log.error("Failed read template image", e);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RecognizerException("Failed read image", e);
        }
    }

    public List<Text> getRecognizeImageTextLocation(ImageDataDto data, MultipartFile image) {
        try {
            return tesseractTextRecognizerService.recognizeTextLocation(image.getBytes());
        } catch (IOException e) {
            throw new RecognizerException("Failed read image", e);
        }
    }

    public String getRecognizeImageText(ImageDataDto data, MultipartFile image) {
        try {
            return tesseractTextRecognizerService.recognizeText(image.getBytes());
        } catch (IOException e) {
            throw new RecognizerException("Failed read image", e);
        }
    }
}
