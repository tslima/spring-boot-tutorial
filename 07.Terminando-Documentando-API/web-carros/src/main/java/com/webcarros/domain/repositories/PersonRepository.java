package com.webcarros.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webcarros.domain.entities.Person;

public interface PersonRepository extends JpaRepository<Person, String> {
}
