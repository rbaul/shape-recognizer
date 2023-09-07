package com.github.rbaul.recognizer.shape.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("tesseract")
public class TesseractProperties {
    /**
     * Location of Tess Data
     */
    private String dataPath = "tessdata";
}
