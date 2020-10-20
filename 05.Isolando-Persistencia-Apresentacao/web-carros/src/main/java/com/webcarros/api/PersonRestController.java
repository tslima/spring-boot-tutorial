package com.webcarros.api;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webcarros.domain.entities.Person;
import com.webcarros.dto.PersonDTO;
import com.webcarros.services.PersonService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonRestController {
	
	private final PersonService personService;
	
	private final ModelMapper modelMapper;
	
	@GetMapping
	public List<PersonDTO> listPersons() {
		return personService.listAll()
				.stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}
	
	private PersonDTO toDTO(final Person entity) {
		return modelMapper.map(entity, PersonDTO.class);
	}
}
