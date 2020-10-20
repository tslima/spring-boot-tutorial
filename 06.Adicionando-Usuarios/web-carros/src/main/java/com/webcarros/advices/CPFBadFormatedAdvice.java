package com.webcarros.advices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.webcarros.exceptions.CPFBadFormatedException;

@ControllerAdvice
public class CPFBadFormatedAdvice {
	
	@ResponseBody
	@ExceptionHandler(CPFBadFormatedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String cpfBadFormatedHandler(final CPFBadFormatedException e) {
		return e.getMessage();
	}
}