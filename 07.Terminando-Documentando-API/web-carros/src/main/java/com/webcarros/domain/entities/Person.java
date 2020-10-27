package com.webcarros.domain.entities;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Person {
	
	@Id
	private String cpf;
	
	@NotNull(message = "Name cannot be null")
	private String name;
	
	@Email
	@NotNull(message = "Email cannot be null")
	private String email;	
	
	private LocalDate birthDate;

}
