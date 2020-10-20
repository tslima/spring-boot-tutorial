package com.webcarros.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
	
	private String cpf;
	
	private String name;
	
	private String email;	
	
	private LocalDate birthDate;
}
