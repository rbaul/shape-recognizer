package com.github.rbaul.recognizer.shape.utils;

import lombok.experimental.UtilityClass;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

@UtilityClass
public class OpenCvUtils {

    // Remove duplicates from the list of rectangles
    // TODO: Check duplicate also by area more accurate
    public static boolean isDuplicated(List<Rect> rectangles, Rect rect) {
        List<RectData> centers = rectangles.stream()
                .map(OpenCvUtils::convert)
                .toList();
//        System.out.println("Rect: " + rect + " Area: " + rect.area());
        RectData center = convert(rect);

        boolean isDuplicate = false;
        for (RectData existingCenter : centers) {
            double distance = Math.sqrt(Math.pow(center.center.x - existingCenter.center.x, 2) +
                    Math.pow(center.center.y - existingCenter.center.y, 2));

            double areaRation = center.area/existingCenter.area;
            // You can adjust this threshold value based on your image and detection accuracy
            // Line width
            if (distance < 2) {
//                System.out.println("Area: " + center.area + " Area: " + existingCenter.area);
                isDuplicate = true;
                break;
            }
        }

        return isDuplicate;
    }
    public record RectData(Point center, double area){}

    public RectData convert(Rect rect) {
        return new RectData(new Point(rect.x + (double) rect.width / 2, rect.y + (double) rect.height / 2), rect.area());
    }

    // Check if the angles between sides of the shape are close to 90 degrees
    public static boolean hasRightAngles(MatOfPoint2f approxCurve) {
        Point[] points = approxCurve.toArray();

        for (int i = 0; i < 4; i++) {
            Point p1 = points[i];
            Point p2 = points[(i + 1) % 4];
            Point p3 = points[(i + 2) % 4];

            double angle = calculateAngle(p1, p2, p3);

            // You can adjust this threshold value based on your desired tolerance
            if (Math.abs(angle - 90) > 5) {
                return false;
            }
        }

        return true;
    }

    // Calculate the angle between three points
    private static double calculateAngle(Point p1, Point p2, Point p3) {
        double angle1 = Math.atan2(p1.y - p2.y, p1.x - p2.x);
        double angle2 = Math.atan2(p3.y - p2.y, p3.x - p2.x);

        double angle = Math.abs(Math.toDegrees(angle1 - angle2));

        return angle <= 180 ? angle : 360 - angle;
    }
}
