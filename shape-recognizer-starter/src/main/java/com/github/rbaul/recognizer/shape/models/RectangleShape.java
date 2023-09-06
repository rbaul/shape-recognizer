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
    private int x;
    private int y;
    private int height;
    private int width;

    @Override
    public ShapeType getType() {
        return ShapeType.RECTANGLE;
    }

    @Override
    public double getArea() {
        return height * width;
    }

    public Point getCenter() {
        return new Point(x + (double) width / 2, y + (double) height / 2);
    }
}
