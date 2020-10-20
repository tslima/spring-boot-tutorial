package com.webcarros.api;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public PersonDTO save(@RequestBody final PersonDTO dto) {
		
		final Person person = this.toEntity(dto);
		
		final Person ret = personService.save(person);
		
		return this.toDTO(ret);
	}
	
	private Person toEntity(final PersonDTO dto) {
		return modelMapper.map(dto, Person.class);
	}

	private PersonDTO toDTO(final Person entity) {
		return modelMapper.map(entity, PersonDTO.class);
	}
}
