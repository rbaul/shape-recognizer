package com.github.rbaul.recognizer.shape;

import com.github.rbaul.recognizer.shape.config.TesseractConfig;
import com.github.rbaul.recognizer.shape.service.OpenCvRecognizerService;
import com.github.rbaul.recognizer.shape.service.TesseractTextRecognizerService;
import org.springframework.context.annotation.Import;

@Import({
        TesseractConfig.class,
        OpenCvRecognizerService.class,
        TesseractTextRecognizerService.class
})
public class ShapeRecognizerConfiguration {
}
