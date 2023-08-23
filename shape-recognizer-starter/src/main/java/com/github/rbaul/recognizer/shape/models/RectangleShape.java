package com.github.rbaul.recognizer.shape.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString
public class RectangleShape extends Shape {
    private Point point;
    private int height;
    private int width;
    private Point p2;
    private Point p3;
    private Point p4;

    @Override
    public ShapeType getType() {
        return ShapeType.RECTANGLE;
    }
}
