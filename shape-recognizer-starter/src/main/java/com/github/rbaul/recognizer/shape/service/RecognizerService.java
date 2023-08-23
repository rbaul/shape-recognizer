package com.github.rbaul.recognizer.shape.service;

import com.github.rbaul.recognizer.shape.models.Shape;

import java.util.List;

public interface RecognizerService {

    List<Shape> recognizeAllShapes(byte[] image);
}
