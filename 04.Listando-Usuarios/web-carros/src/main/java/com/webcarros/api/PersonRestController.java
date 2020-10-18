package com.webcarros.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webcarros.domain.entities.Person;
import com.webcarros.services.PersonService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonRestController {
	
	private final PersonService personService;
	
	@GetMapping
	public List<Person> listPersons() {
		return personService.listAll();
	}
}
