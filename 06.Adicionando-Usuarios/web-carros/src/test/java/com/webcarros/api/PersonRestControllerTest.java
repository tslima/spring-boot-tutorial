package com.webcarros.api;



import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcarros.domain.entities.Person;
import com.webcarros.dto.PersonDTO;
import com.webcarros.exceptions.CPFBadFormatedException;
import com.webcarros.services.PersonService;

@WebMvcTest(PersonRestController.class)
public class PersonRestControllerTest {
	
	@Autowired MockMvc mockMvc;
	
	@Autowired ObjectMapper objectMapper; 
	
	@MockBean PersonService personService;
	
	@Test
	void testListUsersAPI() throws Exception {
		//Arrange
		final PersonDTO person1DTO = builPersonDTO();
		final Person person1 = new Person(person1DTO.getCpf(),person1DTO.getName(),person1DTO.getEmail(), person1DTO.getBirthDate());
		
		
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
	
	@Test
	public void testSuccessfullSave() throws Exception {

		final PersonDTO personDTO = builPersonDTO();
		
		when(personService.save(any(Person.class))).then(i -> i.getArgument(0));
		
		//Act
		this.mockMvc.perform(post("/api/persons")
				.content(objectMapper.writeValueAsString(personDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8"))
				//Assert
				.andExpect(status().isCreated())
				.andExpect(content().json(objectMapper.writeValueAsString(personDTO)))
				.andDo(print());
	}
	
	@Test
	public void testCPFMalFormated() throws Exception {

		//Arrange
		final PersonDTO personDTO = builPersonDTO();
		when(personService.save(any(Person.class))).thenThrow(new CPFBadFormatedException(personDTO.getCpf()));
		
		//Act
		this.mockMvc.perform(post("/api/persons")
				.content(objectMapper.writeValueAsString(personDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8"))
				//Assert
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString(personDTO.getCpf())))
				.andDo(print());
	}

	private PersonDTO builPersonDTO() {
		final String personCPF = "12345678923";
		final String personName = "person1";
		final String personEmail = "person1@email.com.br";
		final LocalDate personDate = LocalDate.parse("2000-01-01");
		final PersonDTO personDTO = new PersonDTO(personCPF,personName,personEmail, personDate);
		return personDTO;
	}
}
