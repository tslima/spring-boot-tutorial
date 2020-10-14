package com.webcarros.services;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webcarros.domain.entities.Person;
import com.webcarros.domain.repositories.PersonRepository;
import com.webcarros.exceptions.CPFBadFormatedException;
import com.webcarros.exceptions.PersonNotFoundException;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {
	@InjectMocks private PersonService personService;
	@Mock private PersonRepository personRepository;
	
	@Test
	public void testListPersons() {
		
		//Arrange
		final Person person1 = new Person("123456789", "NAME-1", "email-1@email.com.br", LocalDate.parse("2000-01-01"));
		final Person person2 = new Person("987654321", "NAME-2", "email-2@email.com.br", LocalDate.parse("2000-01-01"));
		when(personRepository.findAll()).thenReturn(List.of(person1, person2));

		//Act
		final List<Person> ret = personService.listAll();		
		
		//Assert
		assertThat(ret.size()).isEqualTo(2);
		assertThat(ret.get(0)).isEqualTo(person1);
		assertThat(ret.get(1)).isEqualTo(person2);
	}
	
	@Test
	void testSaveUserAsInformed() {		
		//Arrange
		final Person person = new Person("12345678901", "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
		when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
				
		//Act
		final Person ret = personService.save(person);
		
		//Assert
		assertThat(person).isEqualTo(ret);
	}
	
	@Test
	void testRemoveDotsAndTracesFromCPFThanSave() {		
		//Arrange
		final Person person = new Person("123.456.789-01", "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
		when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
				
		//Act
		final Person ret = personService.save(person);
		
		//Assert
		assertThat(ret.getCpf()).isEqualTo("12345678901");
	}
	
	@Test
	void testWrongFormatedCPFExcpetionThrown() {
		//Arrange
		final Person person = new Person("123.ABC.789-78", "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));		
		
		//Act
		//Assert
		assertThrows(CPFBadFormatedException.class, () -> personService.save(person));
	}
	
	@Test
	void testFindUserByCPF() {	
		//Arrange
		final String cpf = "12345678901";
		final Person person = new Person(cpf, "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
		when(personRepository.findById(cpf)).thenReturn(Optional.of(person));
		
		//Act
		final Person ret = personService.findByCPF(cpf);
		
		//Assert
		assertThat(ret).isEqualTo(person);
	}
	
	@Test
	void testRemoveDotsAndTracesFromCPFThanFind() {	
		//Arrange
		final String cpf = "12345678901";
		final Person person = new Person(cpf, "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
		when(personRepository.findById(cpf)).thenReturn(Optional.of(person));
		
		//Act
		final Person ret = personService.findByCPF("123.456.789-01");
		
		//Assert
		assertThat(ret).isEqualTo(person);
	}
	
	@Test
	void testThrowPersonNotFoundException() {	
		//Arrange
		final String cpf = "12345678901";
		when(personRepository.findById(any())).thenReturn(Optional.empty());
		
		//Act	
		//Assert
		assertThrows(PersonNotFoundException.class, ()-> personService.findByCPF(cpf));
	}
	
	@Test
	void testUpdateUser() {
		
		//Arrange
		final String cpf = "12345678901";
		final Person person = new Person(cpf, "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
		final Person updatePerson = new Person(cpf, "CENTOS", "centos@centos.com.br", LocalDate.parse("2010-01-01"));
		when(personRepository.findById(cpf)).thenReturn(Optional.of(person));
		when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
		
		//Act
		final Person ret = personService.update(cpf,updatePerson);
		
		//Assert
		assertThat(ret).isEqualTo(updatePerson);
	}
	
	@Test
	void testRemoveDotsAndTracesThanUpdateUser() {
		
		//Arrange
		final String cpf = "12345678901";
		final Person person = new Person(cpf, "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
		final Person updatePerson = new Person(cpf, "CENTOS", "centos@centos.com.br", LocalDate.parse("2010-01-01"));
		when(personRepository.findById(cpf)).thenReturn(Optional.of(person));
		when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
		
		//Act
		final Person ret = personService.update("123.456.789-01",updatePerson);
		
		//Assert
		assertThat(ret).isEqualTo(updatePerson);
	}
	
	@Test
	void testThrowPersonNotFoundExeption() {
		
		//Arrange
		final String cpf = "12345678901";
		final Person updatePerson = new Person(cpf, "CENTOS", "centos@centos.com.br", LocalDate.parse("2010-01-01"));
		when(personRepository.findById(cpf)).thenReturn(Optional.empty());
		
		//Act
		//Assert
		assertThrows(PersonNotFoundException.class,() -> personService.update(cpf,updatePerson));
	}
	
	@Test
	void testRemoveUser() {
		//Arrange
		final String cpf = "12345678901";
		
		//Act
		//Assert
		assertDoesNotThrow(() -> personService.remove(cpf));
	}
	
	@Test
	void testRemoveDotsAndTracesThanRemoveUser() {
		//Arrange
		final String cpf = "123.456.789-01";		
		doNothing().when(personRepository).deleteById(any());
		
		//Act
		//Assert
		assertDoesNotThrow(() -> personService.remove(cpf));
	}
	
}





