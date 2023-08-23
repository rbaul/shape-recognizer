package com.github.rbaul.recognizer.shape.service;

import com.github.rbaul.recognizer.shape.models.RectangleShape;
import com.github.rbaul.recognizer.shape.models.Shape;
import com.github.rbaul.recognizer.shape.models.ShapeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OpenCvRecognizerService implements RecognizerService {

    static {
        log.info("Loaded OpenCV recognizer");
        OpenCV.loadLocally();
    }

    @Override
    public List<Shape> recognizeAllShapes(byte[] imageBytes) {

        List<Shape> shapes = new ArrayList<>();

        // Load the image
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur to reduce noise
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);

        // Perform edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 50, 150);

        // Rectangle detection

        // Find contours in the edge-detected image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Iterate through each contour and find rectangles
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

            double epsilon = 0.04 * Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);

            int hierarchyDepth = (int) hierarchy.get(0, i)[3];
            if (hierarchyDepth >= 0) {
                // This contour has a parent, it's nested in another contour
                // Handle nested rectangles here
                if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                    // You've found a rectangle, do something with it
                    // The corners of the rectangle are given by approxCurve.toArray()
                    System.out.println("Nested Rectangle Detected");
                    Point[] array = approxCurve.toArray();
                    shapes.add(RectangleShape.builder()
                            .point(new com.github.rbaul.recognizer.shape.models.Point(array[0].x, array[0].y))
                            .p2(new com.github.rbaul.recognizer.shape.models.Point(array[1].x, array[1].y))
                            .p3(new com.github.rbaul.recognizer.shape.models.Point(array[2].x, array[2].y))
                            .p4(new com.github.rbaul.recognizer.shape.models.Point(array[3].x, array[3].y))
                            .build());

//                    Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//                    Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//                    Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//                    Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);

                    System.out.println(array[0] + " - " + array[1] + " - " + array[2] + " - " + array[3]);
                }
            } else {
                // This contour is an outer rectangle
                if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                    // You've found a rectangle, do something with it
                    // The corners of the rectangle are given by approxCurve.toArray()
                    System.out.println("Rectangle Detected");
                    Point[] array = approxCurve.toArray();


                    shapes.add(RectangleShape.builder()
                            .point(new com.github.rbaul.recognizer.shape.models.Point(array[0].x, array[0].y))
                            .p2(new com.github.rbaul.recognizer.shape.models.Point(array[1].x, array[1].y))
                            .p3(new com.github.rbaul.recognizer.shape.models.Point(array[2].x, array[2].y))
                            .p4(new com.github.rbaul.recognizer.shape.models.Point(array[3].x, array[3].y))
                            .build());
//                    Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                    Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                    Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                    Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
                    System.out.println(array[0] + " - " + array[1] + " - " + array[2] + " - " + array[3]);
                }
            }
        }

        return shapes;
    }

    public byte[] getRecognizeShapeImage(byte[] imageBytes) {
        List<Shape> shapes = recognizeAllShapes(imageBytes);
        // Load the image
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

        for (Shape shape : shapes) {
            if (shape.getType() == ShapeType.RECTANGLE) {
                RectangleShape rec = (RectangleShape) shape;
                Imgproc.line(image, new Point(rec.getPoint().x(), rec.getPoint().y()), new Point(rec.getP2().x(), rec.getP2().y()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
                Imgproc.line(image, new Point(rec.getP2().x(), rec.getP2().y()), new Point(rec.getP3().x(), rec.getP3().y()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
                Imgproc.line(image, new Point(rec.getP3().x(), rec.getP3().y()), new Point(rec.getP4().x(), rec.getP4().y()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
                Imgproc.line(image, new Point(rec.getP4().x(), rec.getP4().y()), new Point(rec.getPoint().x(), rec.getPoint().y()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
            }
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", image, matOfByte);
        return matOfByte.toArray();
    }
}
