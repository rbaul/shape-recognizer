package com.github.rbaul.recognizer.handler;

import com.github.rbaul.recognizer.exceptions.RecognizerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * It is recommended to replace the messages with those
 * that do not reveal details about the code.
 */
@Slf4j
@RestControllerAdvice
public class GlobalErrorHandlers {
	
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public String handleGlobalError(Exception ex) {
		log.error("Global error handler exception: ", ex);
		return ex.getLocalizedMessage();
	}
	
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(RecognizerException.class)
	public String handleRecognizerException(RecognizerException ex) {
		log.error("Recognizer error handler exception: ", ex);
		return ex.getLocalizedMessage();
	}
}