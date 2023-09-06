package com.github.rbaul.recognizer.shape;

import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Disabled
public class OpenCvTests {


    @Test
    void loadOpenCV() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        Path shapesImage = Paths.get("src", "test", "resources", "shapes.png");

        Mat imread = Imgcodecs.imread(shapesImage.toString());

        System.out.println(imread);

        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "shapes_copy.png").toString(), imread);
    }

    @Test
    void detectShape() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        Path shapesImage = Paths.get("src", "test", "resources", "shapes.png");

        Mat loadedImage = Imgcodecs.imread(shapesImage.toString());

        MatOfRect facesDetected = new MatOfRect();

        CascadeClassifier cascadeClassifier = new CascadeClassifier();
        int minFaceSize = Math.round(loadedImage.rows() * 0.1f);
//        cascadeClassifier.load("./src/main/resources/haarcascades/haarcascade_frontalface_alt.xml");
        cascadeClassifier.detectMultiScale(loadedImage,
                facesDetected,
                1.1,
                3,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minFaceSize, minFaceSize),
                new Size()
        );

        Rect[] facesArray = facesDetected.toArray();
        for (Rect face : facesArray) {
            Imgproc.rectangle(loadedImage, face.tl(), face.br(), new Scalar(0, 0, 255), 3);
        }

        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "shapes_copy.png").toString(), loadedImage);
    }


    @Test
    void recognizeRectangle() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Declare the output variables
        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        Path shapesImage = Paths.get("src", "test", "resources", "rectangle-2.png");

        // Load the image
        Mat image = Imgcodecs.imread(shapesImage.toString());
//        Mat gray = Imgcodecs.imread(shapesImage.toString(), Imgcodecs.IMREAD_GRAYSCALE);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        cdstP = gray.clone();


        // Apply Canny edge detector
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 200, 3, false);

        // Apply Hough transform to find lines
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 25, 25, 10);


        // Draw the lines
//        for (int x = 0; x < lines.rows(); x++) {
//            double[] l = lines.get(x, 0);
//            Imgproc.line(cdstP, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//        }

        // Loop over the lines and check if they form a rectangle
        for (int i = 0; i < lines.rows(); i++) {
            // Get the endpoints of the first line
            double[] l1 = lines.get(i, 0);
            Point p1 = new Point(l1[0], l1[1]);
            Point p2 = new Point(l1[2], l1[3]);

            // Loop over the remaining lines
            for (int j = i + 1; j < lines.rows(); j++) {
                // Get the endpoints of the second line
                double[] l2 = lines.get(j, 0);
                Point p3 = new Point(l2[0], l2[1]);
                Point p4 = new Point(l2[2], l2[3]);

                // Check if the lines are perpendicular
                double dot = (p2.x - p1.x) * (p4.x - p3.x) + (p2.y - p1.y) * (p4.y - p3.y);
                if (Math.abs(dot) < 0.01) {
                    // Find the intersection point of the lines
                    double det = (p2.x - p1.x) * (p4.y - p3.y) - (p2.y - p1.y) * (p4.x - p3.x);
                    if (det != 0) {
                        double x = ((p4.x - p3.x) * (p1.x * p2.y - p1.y * p2.x) - (p2.x - p1.x) * (p3.x * p4.y - p3.y * p4.x)) / det;
                        double y = ((p4.y - p3.y) * (p1.x * p2.y - p1.y * p2.x) - (p2.y - p1.y) * (p3.x * p4.y - p3.y * p4.x)) / det;
                        Point p = new Point(x, y);

                        // Check if the intersection point is within the image bounds
//                        if (x >= 0 && x <= image.width() && y >= 0 && y <= image.height()) {
                        // Check if there are two other lines that form a rectangle with the first two
                        boolean found = false;
                        for (int k = 0; k < lines.rows(); k++) {
                            if (k != i && k != j) {
                                // Get the endpoints of the third line
                                double[] l3 = lines.get(k, 0);
                                Point p5 = new Point(l3[0], l3[1]);
                                Point p6 = new Point(l3[2], l3[3]);

                                // Check if the third line is perpendicular to the first line and passes through the intersection point
                                double dot1 = (p2.x - p1.x) * (p6.x - p5.x) + (p2.y - p1.y) * (p6.y - p5.y);
                                double det1 = (p6.x - p5.x) * (p.y - p5.y) - (p6.y - p5.y) * (p.x - p5.x);
                                if (Math.abs(dot1) < 0.01 && Math.abs(det1) < 0.01) {
                                    // Find the opposite vertex of the rectangle
                                    Point q = new Point(p5.x + p6.x - x, p5.y + p6.y - y);

                                    // Check if there is a fourth line that is perpendicular to the second line and passes through the opposite vertex
                                    for (int l = 0; l < lines.rows(); l++) {
                                        if (l != i && l != j && l != k) {
                                            // Get the endpoints of the fourth line
                                            double[] l4 = lines.get(l, 0);
                                            Point p7 = new Point(l4[0], l4[1]);
                                            Point p8 = new Point(l4[2], l4[3]);

                                            // Check if the fourth line is perpendicular to the second line and passes through the opposite vertex
                                            double dot2 = (p4.x - p3.x) * (p8.x - p7.x) + (p4.y - p3.y) * (p8.y - p7.y);
                                            double det2 = (p8.x - p7.x) * (q.y - p7.y) - (p8.y - p7.y) * (q.x - p7.x);
                                            if (Math.abs(dot2) < 0.01 && Math.abs(det2) < 0.01) {
                                                Imgproc.line(cdstP, p1, p, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
                                                Imgproc.line(cdstP, p, p5, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
                                                Imgproc.line(cdstP, p5, q, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
                                                Imgproc.line(cdstP, q, p3, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
                                                // Draw the rectangle on the image
//                                                    Core.line(image, p1, p, new Scalar(0, 255, 0), 2);
//                                                    Core.line(image, p, p5, new Scalar(0, 255, 0), 2);
//                                                    Core.line(image, p5, q, new Scalar(0, 255, 0), 2);
//                                                    Core.line(image, q, p3, new Scalar(0, 255, 0), 2);

                                                // Set the flag to true
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                // Break the loop if a rectangle is found
                                if (found) {
                                    break;
                                }
                            }
                        }
//                        }
                    }
                }
            }
        }


        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "shapes_copy.png").toString(), cdstP);
    }


    @Test
    void rectangleDetection() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Declare the output variables
        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        Path shapesImage = Paths.get("src", "test", "resources", "nested.png");

        // Load the image
        Mat image = Imgcodecs.imread(shapesImage.toString());
//        Mat gray = Imgcodecs.imread(shapesImage.toString(), Imgcodecs.IMREAD_GRAYSCALE);
        cdstP = image.clone();

        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "1.png").toString(), grayImage);

        // Apply Gaussian blur to reduce noise
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 100);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "2.png").toString(), blurredImage);

        // Perform edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 50, 150);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "3.png").toString(), edges);

        // Find contours in the edge-detected image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
//        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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

                    Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                    Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                    Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                    Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);

//                    System.out.println(array[0] + " - " + array[1] + " - " + array[2] + " - " + array[3]);
                }
            } else {
                // This contour is an outer rectangle
                if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                    // You've found a rectangle, do something with it
                    // The corners of the rectangle are given by approxCurve.toArray()
                    System.out.println("Rectangle Detected");
                    Point[] array = approxCurve.toArray();

                    Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
                    Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
                    Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
                    Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
//                    System.out.println(array[0] + " - " + array[1] + " - " + array[2] + " - " + array[3]);
                }
            }
        }

        // Iterate through each contour and find rectangles
//        for (MatOfPoint contour : contours) {
//            MatOfPoint2f approxCurve = new MatOfPoint2f();
//            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
//
//            double epsilon = 0.04 * Imgproc.arcLength(contour2f, true);
//            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
//
//            if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
//                // You've found a rectangle, do something with it
//                // The corners of the rectangle are given by approxCurve.toArray()
//                System.out.println("Rectangle Detected");
//                Point[] array = approxCurve.toArray();
//
//                Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//
//
//                // Nested
////                Mat cropMat = new Mat(edges, new Rect((int) array[0].x, (int) array[0].y, (int) (array[2].x - array[0].x), (int) (array[2].y - array[0].y)));
//
//                // Now, you can search for nested rectangles inside this contour
//                for (MatOfPoint nestedContour : contours) {
//                    MatOfPoint2f nestedContour2f = new MatOfPoint2f(contour.toArray());
//                    if (Imgproc.pointPolygonTest(nestedContour2f, approxCurve.toArray()[0], false) > 0) {
//                        // The nested contour is inside the current rectangle
//                        // You can treat this nested contour as a potential nested rectangle
//                        System.out.println("Nested Rectangle Detected");
////                        array = approxCurve.toArray();
////
////                        Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
////                        Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
////                        Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
////                        Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                    }
//                }
//            }
//        }

        // Save image
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "shapes_copy.png").toString(), cdstP);

    }

    @Test
    void loadOpenCVFromBytes() throws IOException {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        Path shapesImage = Paths.get("src", "test", "resources", "shapes.png");

        byte[] bytes = FileCopyUtils.copyToByteArray(shapesImage.toFile());
        Mat imread = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_GRAYSCALE);

        System.out.println(imread);

        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "shapes_copy.png").toString(), imread);
    }


    @Test
    void manipulationDetection() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Declare the output variables
        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        Path shapesImage = Paths.get("src", "test", "resources", "nested.png");

        // Load the image
        Mat image = Imgcodecs.imread(shapesImage.toString());
//        Mat gray = Imgcodecs.imread(shapesImage.toString(), Imgcodecs.IMREAD_GRAYSCALE);
        cdstP = image.clone();

        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "1.png").toString(), grayImage);


        // Apply Gaussian blur to reduce noise
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "2.png").toString(), blurredImage);

        for (int i = 0; i < 10; i++) {
            Imgproc.GaussianBlur(blurredImage, blurredImage, new Size(3, 3), 0);
        }

        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "2+.png").toString(), blurredImage);


        // Perform edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 10, 50);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "3.png").toString(), edges);

        // Flood fill
        Imgproc.floodFill(edges, new Mat(), new Point(0, 0), new Scalar(255));
        Imgproc.floodFill(edges, new Mat(), new Point(0, 0), new Scalar(0));

        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "4.png").toString(), edges);
    }


    @Test
    void manipulationDetection_1() throws TesseractException, IOException {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Declare the output variables
        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        Path shapesImage = Paths.get("src", "test", "resources", "nested_multi.png");

        // Load the image
        Mat image = Imgcodecs.imread(shapesImage.toString());
//        Mat gray = Imgcodecs.imread(shapesImage.toString(), Imgcodecs.IMREAD_GRAYSCALE);
        cdstP = image.clone();

//        Tesseract tesseract = new Tesseract();
//        tesseract.setDatapath(Paths.get("src", "test", "resources", "tessdata").toString());
//        tesseract.setLanguage("eng");
//        tesseract.setPageSegMode(1);
//        tesseract.setOcrEngineMode(1);
//
//
//        byte[] arr = new byte[]{};
//        ImageIO.read(new ByteArrayInputStream(arr));
//        // Perform OCR on the image
//        BufferedImage bufferedImage = ImageIO.read(shapesImage.toFile());
////        String recognizedText = tesseract.doOCR(bufferedImage);
//        List<Word> wordList = tesseract.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_BLOCK);
//        for (Word word : wordList) {
//            String wordText = word.getText();
//            Rectangle boundingBox = word.getBoundingBox();
//            System.out.println("Word: " + wordText);
//            System.out.println("Coordinates: x=" + boundingBox.x + ", y=" + boundingBox.y +
//                    ", width=" + boundingBox.width + ", height=" + boundingBox.height);
//        }

//        List<Rectangle> segmentedRegions = tesseract.getSegmentedRegions(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD);
//        for (Rectangle segmentedRegion : segmentedRegions) {
//            segmentedRegion.get
//        }


        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "1.png").toString(), grayImage);

        // Apply Gaussian blur to reduce noise
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(3, 3), 0);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "2.png").toString(), blurredImage);

        // Create a destination Mat to store the smoothed image
        Mat smoothedImage = new Mat();
        // Define the size of the kernel (moving window)
        Size kernelSize = new Size(3, 3); // Adjust the kernel size as needed
        // Perform the moving average (blurring) on the image
        Imgproc.blur(grayImage, smoothedImage, kernelSize);

        // adaptiveThreshold
//        Mat adaptiveImage = new Mat();
//        Imgproc.adaptiveThreshold(blurredImage, adaptiveImage, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);

        // Perform edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 100, 220);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "3.png").toString(), edges);

        // Find contours in the edge-detected image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> detectedRectangles = new ArrayList<>();

        // Iterate through each contour and find rectangles
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

            double epsilon = 0.04 * Imgproc.arcLength(contour2f, false);
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, false);

            int hierarchyDepth = (int) hierarchy.get(0, i)[3];
            double area = Imgproc.contourArea(contour);
            if (hierarchyDepth >= 0) {
                // This contour has a parent, it's nested in another contour
                // Handle nested rectangles here
                if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                    // You've found a rectangle, do something with it
                    // The corners of the rectangle are given by approxCurve.toArray()
//                    System.out.println("Nested Rectangle Detected");

                    int parentIdx = (int) hierarchy.get(0, i)[3];
                    MatOfPoint parentContour = contours.get(parentIdx);

                    Point[] array = approxCurve.toArray();
                    Rect rect = Imgproc.boundingRect(contour);
                    if (!isDuplicated(detectedRectangles, rect) && hasRightAngles(approxCurve) && area > 100) {
                        detectedRectangles.add(rect);
                        String result = "";//tesseract.doOCR(shapesImage.toFile(), new Rectangle(rect.x, rect.y, rect.width, rect.height));

                        System.out.println("Level: " + hierarchyDepth + " Points: " + Arrays.toString(array) + " Area: " + area + " Rect: " + rect + " Center: " + new Point(rect.x + (double) rect.width / 2, rect.y + (double) rect.height / 2) + " Text: " + result);

                        Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                        Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                        Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                        Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);

//                    System.out.println(array[0] + " - " + array[1] + " - " + array[2] + " - " + array[3]);
                    }
                }
//                else {
//                    Point[] array = approxCurve.toArray();
//                    for (int i1 = 0; i1 < array.length - 1; i1++) {
//                        Imgproc.line(cdstP, array[i1], array[i1+1], new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
//                    }
//                    Imgproc.line(cdstP, array[array.length-1], array[0], new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
//                }
            } else {
                // This contour is an outer rectangle
                if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                    // You've found a rectangle, do something with it
                    // The corners of the rectangle are given by approxCurve.toArray()
//                    System.out.println("Rectangle Detected");
                    Point[] array = approxCurve.toArray();
                    Rect rect = Imgproc.boundingRect(contour);
                    if (!isDuplicated(detectedRectangles, rect) && hasRightAngles(approxCurve) && area > 100) {
                        detectedRectangles.add(rect);

                        String result = "";//tesseract.doOCR(shapesImage.toFile(), new Rectangle(rect.x, rect.y, rect.width, rect.height));
                        System.out.println("Level: " + hierarchyDepth + " Points: " + Arrays.toString(array) + " Area: " + area + " Rect: " + rect + " Center: " + new Point(rect.x + (double) rect.width / 2, rect.y + (double) rect.height / 2) + " Text: " + result);

                        Imgproc.line(cdstP, array[0], array[1], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                        Imgproc.line(cdstP, array[1], array[2], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                        Imgproc.line(cdstP, array[2], array[3], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
                        Imgproc.line(cdstP, array[3], array[0], new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);

//                    System.out.println(array[0] + " - " + array[1] + " - " + array[2] + " - " + array[3]);
                    }
                }
            }
        }

        // Save image
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "shapes_copy.png").toString(), cdstP);
    }


    // Remove duplicates from the list of rectangles
    // TODO: Check duplicate also by area more accurate
    private static boolean isDuplicated(List<Rect> rectangles, Rect rect) {
        List<Point> centers = rectangles.stream()
                .map(r -> new Point(r.x + (double) r.width / 2, r.y + (double) r.height / 2))
                .toList();

        Point center = new Point(rect.x + (double) rect.width / 2, rect.y + (double) rect.height / 2);

        boolean isDuplicate = false;
        for (Point existingCenter : centers) {
            double distance = Math.sqrt(Math.pow(center.x - existingCenter.x, 2) +
                    Math.pow(center.y - existingCenter.y, 2));

            // You can adjust this threshold value based on your image and detection accuracy
            // Line width
            if (distance < 5) {
                isDuplicate = true;
                break;
            }
        }

        return isDuplicate;
    }

    // Check if the angles between sides of the shape are close to 90 degrees
    private static boolean hasRightAngles(MatOfPoint2f approxCurve) {
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


    @Test
    void manipulationDetection_2() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Declare the output variables
        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        Path shapesImage = Paths.get("src", "test", "resources", "img_1.png");

        // Load the image
        Mat image = Imgcodecs.imread(shapesImage.toString());
//        Mat gray = Imgcodecs.imread(shapesImage.toString(), Imgcodecs.IMREAD_GRAYSCALE);
        cdstP = image.clone();

        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "1.png").toString(), grayImage);

        // Apply Gaussian blur to reduce noise
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(7, 7), 0);
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "2.png").toString(), blurredImage);

        // Create a destination Mat to store the smoothed image
        Mat smoothedImage = new Mat();
        // Define the size of the kernel (moving window)
        Size kernelSize = new Size(3, 3); // Adjust the kernel size as needed
        // Perform the moving average (blurring) on the image
        Imgproc.blur(grayImage, smoothedImage, kernelSize);

        // adaptiveThreshold
//        Mat adaptiveImage = new Mat();
//        Imgproc.adaptiveThreshold(blurredImage, adaptiveImage, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);

        // Find line segments using the Hough Line Transform
        Mat lines = new Mat();
        Imgproc.HoughLinesP(grayImage, lines, 1, Math.PI / 180, 100, 50, 10);

        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "3.png").toString(), lines);

        // Filter partially visible line segments based on criteria like length or completeness
        List<MatOfPoint> partiallyVisibleLines = filterPartiallyVisibleLines(lines);


        for (MatOfPoint lineSegment : partiallyVisibleLines) {
            Point[] points = lineSegment.toArray();
            Imgproc.line(cdstP, points[0], points[1], new Scalar(0, 255, 0), 2);
        }

        // Save image
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "shapes_copy.png").toString(), cdstP);
    }

    private static List<MatOfPoint> filterPartiallyVisibleLines(Mat lines) {
        List<MatOfPoint> partiallyVisibleLines = new ArrayList<>();

        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
            double x1 = val[0];
            double y1 = val[1];
            double x2 = val[2];
            double y2 = val[3];

            // Calculate the length of the line segment
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

            // Filter partially visible line segments based on length or other criteria
            if (length < 500 && length > 1 && meetsCompletenessCriteria(x1, y1, x2, y2)) {
                MatOfPoint lineSegment = new MatOfPoint(new Point(x1, y1), new Point(x2, y2));
                partiallyVisibleLines.add(lineSegment);
            }
        }

        return partiallyVisibleLines;
    }

    // Implement a function to check the completeness of a line segment
    private static boolean meetsCompletenessCriteria(double x1, double y1, double x2, double y2) {
        // Implement your logic to check if the line segment is partially visible or complete
        // You can consider factors like the gap in the line, visibility, or other criteria.
        // Return true if the line meets the completeness criteria; otherwise, return false.
        return true; // Replace with your completeness check logic
    }

    @Test
    public void findByTemplate() {
        OpenCV.loadLocally(); // SEVERE: OpenCV.loadShared() is not supported in Java >= 12. Falling back to OpenCV.loadLocally().

        // Declare the output variables
        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        Path shapesImage = Paths.get("src", "test", "resources", "img_2.png");

        // Load the image
        Mat mainImage = Imgcodecs.imread(shapesImage.toString());
//        Mat gray = Imgcodecs.imread(shapesImage.toString(), Imgcodecs.IMREAD_GRAYSCALE);
        cdstP = mainImage.clone();

        // Load the template image (example element to recognize)
        Mat template = Imgcodecs.imread(Paths.get("src", "test", "resources", "template.png").toString());

        // Create a Mat to store the result of template matching
        Mat result = new Mat();

        // Perform template matching
        Imgproc.matchTemplate(mainImage, template, result, Imgproc.TM_CCOEFF_NORMED);

        // Define a threshold to filter matches
        double threshold = 0.8;

        // Find matches above the threshold
        Core.compare(result, new Scalar(threshold), result, Core.CMP_GT);

        // Find locations of matches
        MatOfPoint locations = new MatOfPoint();
        Core.findNonZero(result, locations);

        // Draw rectangles around matches
        for (Point point : locations.toArray()) {
            Imgproc.rectangle(cdstP, point, new Point(point.x + template.cols(), point.y + template.rows()), new Scalar(0, 255, 0), 1);
        }

        // Save image
        Imgcodecs.imwrite(Paths.get("src", "test", "resources", "output", "shapes_copy.png").toString(), cdstP);
    }
}
