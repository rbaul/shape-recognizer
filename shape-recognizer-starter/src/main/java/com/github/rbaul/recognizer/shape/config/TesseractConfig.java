package com.github.rbaul.recognizer.shape.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        TesseractProperties.class
})
@Configuration
public class TesseractConfig {

    @Bean
    public Tesseract tesseract(TesseractProperties tesseractProperties) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractProperties.getDataPath());
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }
}
