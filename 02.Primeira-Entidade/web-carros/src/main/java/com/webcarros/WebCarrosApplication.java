package com.webcarros;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.webcarros.domain.entities.Person;
import com.webcarros.domain.repositories.PersonRestRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class WebCarrosApplication {

	public static void main(final String[] args) {
		SpringApplication.run(WebCarrosApplication.class, args);
	}
	
	@Bean
	CommandLineRunner initDatabase(final PersonRestRepository repository){
		return args -> {
			log.info("Saving... " + repository.save(new Person("01234567887", "Ubuntu", "ubuntu@ubuntu.com.br", LocalDate.parse("2000-01-01"))));
		};		
	}
}
