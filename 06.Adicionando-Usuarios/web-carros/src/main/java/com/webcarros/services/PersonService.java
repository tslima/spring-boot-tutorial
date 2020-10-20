package com.webcarros.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.webcarros.domain.entities.Person;
import com.webcarros.domain.repositories.PersonRepository;
import com.webcarros.exceptions.CPFBadFormatedException;
import com.webcarros.exceptions.PersonNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonService {
	
	private final PersonRepository personRepository;

	public List<Person> listAll() {
		return personRepository.findAll();
	}

	public Person save(final Person person) {
		
		final String cpf = this.removeDotsAndTraces(person.getCpf());
		
		if (!cpf.matches("^[0-9]{11}$")) {
			throw new CPFBadFormatedException(person.getCpf());
		}
		
		person.setCpf(cpf);
		
		return personRepository.save(person);
	}

	public Person findByCPF(final String cpf) {
		final String replaceCPF = this.removeDotsAndTraces(cpf);
		return personRepository.findById(replaceCPF).orElseThrow(()-> new PersonNotFoundException()) ;
	}

	public Person update(final String cpf, final Person updatePerson) {
		final String updateCPF = this.removeDotsAndTraces(cpf);		
		return personRepository.findById(updateCPF)
				.map(p -> {
					p.setBirthDate(updatePerson.getBirthDate());
					p.setEmail(updatePerson.getEmail());
					p.setName(updatePerson.getName());
					return personRepository.save(p);
				}).orElseThrow(()-> new PersonNotFoundException());

	}	
	
	private String removeDotsAndTraces(final String cpf) {
		return cpf.replace(".", "").replace("-", "");
	}

	public void remove(final String cpf) {
		final String id = this.removeDotsAndTraces(cpf);
		personRepository.deleteById(id);
	}
}
