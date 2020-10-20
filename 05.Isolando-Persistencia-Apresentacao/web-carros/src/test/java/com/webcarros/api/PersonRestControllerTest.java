package com.webcarros.api;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcarros.domain.entities.Person;
import com.webcarros.dto.PersonDTO;
import com.webcarros.services.PersonService;

@WebMvcTest(PersonRestController.class)
public class PersonRestControllerTest {
	
	@Autowired MockMvc mockMvc;
	
	@Autowired ObjectMapper objectMapper; 
	
	@MockBean PersonService personService;
	
	@Test
	void testListUsersAPI() throws Exception {
		//Arrange
		final String person1CPF = "12345678923";
		final String person1Name = "person1";
		final String person1Email = "person1@email.com.br";
		final LocalDate person1Date = LocalDate.parse("2000-01-01");
		final Person person1 = new Person(person1CPF,person1Name,person1Email, person1Date);
		final PersonDTO person1DTO = new PersonDTO(person1CPF,person1Name,person1Email, person1Date);
		
		final String person2CPF = "98765431897";
		final String person2Name = "person2";
		final String person2Email = "person2@email.com.br";
		final LocalDate person2Date = LocalDate.parse("2000-01-02");		
		final Person person2 = new Person(person2CPF,person2Name,person2Email, person2Date);
		final PersonDTO person2DTO = new PersonDTO(person2CPF,person2Name,person2Email, person2Date);
		
		final List<Person> personList = List.of(person1,person2);		
		when(personService.listAll()).thenReturn(personList);		
		
		final List<PersonDTO> personDTOList = List.of(person1DTO, person2DTO);
		
		//Act
		this.mockMvc.perform(get("/api/persons"))
			//Assert
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(personDTOList)))
			.andDo(print());
	}
}
