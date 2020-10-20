package com.webcarros.exceptions;

public class CPFBadFormatedException extends RuntimeException{
	public CPFBadFormatedException(final String personCPF) {
		super("CPF INVÁLIDO " + personCPF);
	}

	private static final long serialVersionUID = 3059519487106258012L;
}
