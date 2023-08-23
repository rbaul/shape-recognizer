package com.github.rbaul.recognizer.shape.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString
public class CircleShape extends Shape {
    private Point center;
    private double radius;

    @Override
    public ShapeType getType() {
        return ShapeType.CIRCLE;
    }
}
