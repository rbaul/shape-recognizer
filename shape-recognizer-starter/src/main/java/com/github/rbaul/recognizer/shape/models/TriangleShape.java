package com.github.rbaul.recognizer.shape.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString
public class TriangleShape extends Shape {
    private Point p1;
    private Point p2;
    private Point p3;

    @Override
    public ShapeType getType() {
        return ShapeType.TRIANGLE;
    }
}
