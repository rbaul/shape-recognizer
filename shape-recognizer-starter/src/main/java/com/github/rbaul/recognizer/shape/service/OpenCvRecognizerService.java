package com.github.rbaul.recognizer.shape.service;

import com.github.rbaul.recognizer.shape.models.RectangleShape;
import com.github.rbaul.recognizer.shape.models.Shape;
import com.github.rbaul.recognizer.shape.models.ShapeType;
import com.github.rbaul.recognizer.shape.models.Text;
import com.github.rbaul.recognizer.shape.utils.OpenCvUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OpenCvRecognizerService implements RecognizerService {

    public static final String PNG_EXTENSION = ".png";
    @Autowired
    private TesseractTextRecognizerService textRecognizerService;

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
        Size kernelSize = new Size(3, 3);
        Imgproc.GaussianBlur(grayImage, blurredImage, kernelSize, 0);

        // Perform edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 50, 150);

        // Rectangle detection

        // Find contours in the edge-detected image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> detectedRectangles = new ArrayList<>();
        Stack<Map.Entry<Integer, RectangleShape>> prevHierarchyDepth = new Stack<>();

        // Get all text location in image
        List<Text> texts = textRecognizerService.recognizeTextLocation(imageBytes);

        // Iterate through each contour and find rectangles
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

            double epsilon = 0.04 * Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);

            int hierarchyDepth = (int) hierarchy.get(0, i)[3];

            if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                Rect rect = Imgproc.boundingRect(contour);
//                String text = textRecognizerService.recognizeText(imageBytes, new Rectangle(rect.x, rect.y, rect.width, rect.height));
                if (!OpenCvUtils.isDuplicated(detectedRectangles, rect) && OpenCvUtils.hasRightAngles(approxCurve) && rect.area() > 200) {
                    detectedRectangles.add(rect);
                    RectangleShape rectangleShape = RectangleShape.builder()
                            .x(rect.x)
                            .y(rect.y)
                            .width(rect.width)
                            .height(rect.height)
//                            .text(text)
                            .build();

                    if (hierarchyDepth >= 0) { // Nested
                        Map.Entry<Integer, RectangleShape> parentLevel = prevHierarchyDepth.empty() ? null : prevHierarchyDepth.peek();
                        if (parentLevel == null) {
                            shapes.add(rectangleShape);
                        } else {
                            while (hierarchyDepth < parentLevel.getKey()) {
                                prevHierarchyDepth.pop();
                                parentLevel = prevHierarchyDepth.peek();
                            }
                            RectangleShape parent = parentLevel.getValue();
                            parent.addNested(rectangleShape);
                        }
                    } else {
                        prevHierarchyDepth.clear();
                        shapes.add(rectangleShape);
                    }
                    prevHierarchyDepth.add(new AbstractMap.SimpleEntry<>(hierarchyDepth, rectangleShape));

                }
            }
        }

        return shapes;
    }

    public byte[] getRecognizeShapeImage(byte[] imageBytes, boolean onImageDraw) {
        List<Shape> shapes = recognizeAllShapes(imageBytes);

        Mat drawMap;

        // Load the image
        Mat imageOriginal = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

        drawMap = onImageDraw ? imageOriginal : new Mat(imageOriginal.size(), CvType.CV_8UC3, new Scalar(0, 0, 0)); // Black background;

        drawOn(drawMap, shapes);

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(PNG_EXTENSION, drawMap, matOfByte);
        return matOfByte.toArray();
    }

    private void drawOn(Mat image, List<Shape> shapes) {
        for (Shape shape : shapes) {
            if (shape.getType() == ShapeType.RECTANGLE) {
                RectangleShape rec = (RectangleShape) shape;
                Imgproc.line(image, new Point(rec.getX(), rec.getY()), new Point(rec.getX() + rec.getWidth(), rec.getY()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
                Imgproc.line(image, new Point(rec.getX(), rec.getY()), new Point(rec.getX(), rec.getY() + rec.getHeight()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
                Imgproc.line(image, new Point(rec.getX(), rec.getY() + rec.getHeight()), new Point(rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
                Imgproc.line(image, new Point(rec.getX() + rec.getWidth(), rec.getY()), new Point(rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight()), new Scalar(0, 255, 0), 1, Imgproc.LINE_4, 0);
            }
            if (!CollectionUtils.isEmpty(shape.getNested())) {
                drawOn(image, shape.getNested());
            }
        }
    }

    public byte[] getRecognizeShapeImageByTemplate(byte[] imageBytes, List<byte[]> templateBytes, boolean onImageDraw) {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Load the image
        Mat mainImage = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
        Mat output = onImageDraw ? mainImage.clone() : new Mat(mainImage.size(), CvType.CV_8UC3, new Scalar(0, 0, 0)); // Black background;

        // Load the template images
        List<Mat> templates = templateBytes.stream().map(tb -> Imgcodecs.imdecode(new MatOfByte(tb), Imgcodecs.IMREAD_COLOR))
                .toList();

        // Create a Mat to store the result of template matching
        Mat result = new Mat();

        for (Mat template : templates) {
            // Perform template matching
            Imgproc.matchTemplate(mainImage, template, result, Imgproc.TM_CCOEFF_NORMED);

            // Define a threshold to filter matches
            double threshold = 0.7;

            // Find matches above the threshold
            Core.compare(result, new Scalar(threshold), result, Core.CMP_GT);

            // Find locations of matches
            MatOfPoint locations = new MatOfPoint();
            Core.findNonZero(result, locations);

            // Draw rectangles around matches
            for (Point point : locations.toArray()) {
                Imgproc.rectangle(output, point, new Point(point.x + template.cols(), point.y + template.rows()), new Scalar(0, 255, 0), 1);
            }
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(PNG_EXTENSION, output, matOfByte);
        return matOfByte.toArray();
    }
}
