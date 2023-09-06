package com.github.rbaul.recognizer.shape.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@FieldNameConstants
@ToString
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = Shape.Fields.type)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RectangleShape.class, name = "RECTANGLE"),
        @JsonSubTypes.Type(value = CircleShape.class, name = "CIRCLE"),
        @JsonSubTypes.Type(value = TriangleShape.class, name = "TRIANGLE")
})
public abstract class Shape {
    private ShapeType type;
    private List<Shape> nested;
    private String text;

    public void addNested(Shape shape) {
        if (nested == null) {
            nested = new ArrayList<>();
        }
        nested.add(shape);
    }
    public double getArea() {
        return 0;
    }
}
