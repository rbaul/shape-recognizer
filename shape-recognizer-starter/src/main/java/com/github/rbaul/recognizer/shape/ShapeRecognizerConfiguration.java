package com.github.rbaul.recognizer.shape;

import com.github.rbaul.recognizer.shape.service.OpenCvRecognizerService;
import org.springframework.context.annotation.Import;

@Import({
        OpenCvRecognizerService.class
})
public class ShapeRecognizerConfiguration {
}
