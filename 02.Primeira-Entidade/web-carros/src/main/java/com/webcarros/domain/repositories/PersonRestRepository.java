package com.webcarros.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.webcarros.domain.entities.Person;

@RepositoryRestResource
public interface PersonRestRepository extends JpaRepository<Person, String> {
}
