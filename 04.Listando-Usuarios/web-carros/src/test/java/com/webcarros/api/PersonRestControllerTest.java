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
import com.webcarros.services.PersonService;

@WebMvcTest(PersonRestController.class)
public class PersonRestControllerTest {
	
	@Autowired MockMvc mockMvc;
	
	@Autowired ObjectMapper objectMapper; 
	
	@MockBean PersonService personService;
	
	@Test
	void testListUsersAPI() throws Exception {
		//Arrange
		final Person person1 = new Person("12345678923","person1","person1@email.com.br", LocalDate.parse("2000-01-01"));
		final Person person2 = new Person("98765431897","person2","person2@email.com.br", LocalDate.parse("2000-01-02"));
		
		final List<Person> personList = List.of(person1,person2);
		
		when(personService.listAll()).thenReturn(personList);
		
		//Act
		this.mockMvc.perform(get("/api/persons"))
			//Assert
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(personList)))
			.andDo(print());
	}
}
