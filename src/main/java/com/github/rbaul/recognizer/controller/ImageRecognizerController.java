package com.github.rbaul.recognizer.controller;

import com.github.rbaul.recognizer.dtos.ImageDataDto;
import com.github.rbaul.recognizer.service.ImageRecognizerService;
import com.github.rbaul.recognizer.shape.models.Shape;
import com.github.rbaul.recognizer.shape.models.Text;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/recognizer")
@RestController
public class ImageRecognizerController {

    private final ImageRecognizerService imageRecognizerService;

    @PostMapping(path = "/shape", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<Shape> shapeRecognize(@RequestPart("data") ImageDataDto data, @RequestPart("image") MultipartFile image) {
        return imageRecognizerService.recognizeShape(data, image);
    }

    @PostMapping(path = "/shape/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<Resource> getRecognizeShapeImage(@RequestPart("data") ImageDataDto data,
                                                           @RequestPart("image") MultipartFile image,
                                                           @RequestParam(name = "onImageDraw", defaultValue = "true") boolean onImageDraw) {
        byte[] recognizeShapeImage = imageRecognizerService.getRecognizeShapeImage(data, image, onImageDraw);
        ByteArrayResource body = new ByteArrayResource(recognizeShapeImage);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(recognizeShapeImage.length)
                .body(body);
    }

    @PostMapping(path = "/shape/template", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<Resource> getRecognizeShapeImageByTemplate(@RequestPart("data") ImageDataDto data,
                                                                     @RequestPart("image") MultipartFile image,
                                                                     @RequestPart("template") List<MultipartFile> templates,
                                                                     @RequestParam(name = "onImageDraw", defaultValue = "true") boolean onImageDraw) {
        byte[] recognizeShapeImage = imageRecognizerService.getRecognizeShapeImageByTemplate(data, image, templates, onImageDraw);
        ByteArrayResource body = new ByteArrayResource(recognizeShapeImage);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(recognizeShapeImage.length)
                .body(body);
    }

    @PostMapping(path = "/text/location", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public List<Text> getRecognizeImageTextLocation(@RequestPart("data") ImageDataDto data, @RequestPart("image") MultipartFile image) {
        return imageRecognizerService.getRecognizeImageTextLocation(data, image);
    }

    @PostMapping(path = "/text", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public String getRecognizeImageText(@RequestPart("data") ImageDataDto data, @RequestPart("image") MultipartFile image) {
        return imageRecognizerService.getRecognizeImageText(data, image);
    }
}
