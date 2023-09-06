package com.github.rbaul.recognizer.shape.service;

import com.github.rbaul.recognizer.shape.models.RectangleShape;
import com.github.rbaul.recognizer.shape.models.Text;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class TesseractTextRecognizerService implements TextRecognizer {

    private final Tesseract tesseract;

    public String recognizeText(byte[] imageBytes) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return tesseract.doOCR(bufferedImage);
        } catch (TesseractException | IOException e) {
            log.error("Failed recognize text in image", e);
            return "";
        }
    }

    public String recognizeText(byte[] imageBytes, Rectangle rectangle) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return tesseract.doOCR(bufferedImage, rectangle);
        } catch (TesseractException | IOException e) {
            log.error("Failed recognize text in image", e);
            return "";
        }
    }

    public List<Text> recognizeTextLocation(byte[] imageBytes) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return tesseract.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_BLOCK).stream()
                    .filter(word -> StringUtils.hasText(word.getText()))
                    .map(word -> new Text(word.getText().trim(), RectangleShape.builder()
                            .x(word.getBoundingBox().x)
                            .y(word.getBoundingBox().y)
                            .width(word.getBoundingBox().width)
                            .height(word.getBoundingBox().height).build()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed recognize text in image", e);
            return List.of();
        }
    }
}
