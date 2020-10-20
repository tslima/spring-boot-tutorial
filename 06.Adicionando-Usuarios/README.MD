# Adicionando  Usuários

Bom momento de adicionar usuários. Pegando a história de nosso backlog

---

**SENDO:** um usuário

**EU DESEJO:** Cadastrar novos usuários

**PARA QUE:** Possa conceder acesso a aplicação

--- 

Bom vamos então conversar com o desenvolvedor front-end para estabelecermos o contrato da nossa API. Depois de uma reunião rascunhamos o seguinte atalho:

---
**ROTA** /api/persons

**METODO:** POST

**REQUEST BODY:** Será um json igual ao que é listado

**STATUS(SUCESSO):** 201 (CREATED) 

**RESPONSE BODY:** Entidade persistida

---

Bom com o contrato estabelecido podemos escrever nosso teste

## Criando o teste unitário

Transcrevendo o contrato para um teste temos:

```java
@Test
public void testSuccessfullSave() throws Exception {

    //Arrange
    final String personCPF = "12345678923";
    final String personName = "person1";
    final String personEmail = "person1@email.com.br";
    final LocalDate personDate = LocalDate.parse("2000-01-01");
    final PersonDTO personDTO = new PersonDTO(personCPF,personName,personEmail, personDate);
    
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
```

Agora temos que fazer nosso teste passar. Modificando a classe `PersonRestController`

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.CREATED)
public PersonDTO save(@RequestBody final PersonDTO dto) {
    
    final Person person = this.toEntity(dto);
    
    final Person ret = personService.save(person);
    
    return this.toDTO(ret);
}
```

Vamos analisar as notações:

* `@PostMapping` indica que essa rota aceitará o método post. Com o tipo application/json

* `@ResponseStatus` indica que esse método deve retornar o status 201 e não o 200

* `@RequestBody` indica que o corpo da requisição deve ser convertido para a classe PersonDTO.

Temos um problema. O método `toEntity` não foi criado ainda. Posicionando o cursor sobre ele e pressionando `ctrl+1` criar método.

```java
private Person toEntity(final PersonDTO dto) {
    return modelMapper.map(dto, Person.class);
}
```
Podemos notar a invocação do método `save` da classe PersonService. Então temos que retornar ao nosso teste e mocká-lo:

```java
@Test
public void testSuccessfullSave() throws Exception {

    //Arrange
    final String personCPF = "12345678923";
    final String personName = "person1";
    final String personEmail = "person1@email.com.br";
    final LocalDate personDate = LocalDate.parse("2000-01-01");
    final PersonDTO personDTO = new PersonDTO(personCPF,personName,personEmail, personDate);
    
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
```

Muito bem! Teste passando! Mas... E se o CPF for inválido? O que esperamos de resposta? Conversando com desenvolvedor front-end ele diz que espera um status 400 (Bad Request). E o cpf informado.

Como sempre vamos escrever o teste:

```java
@Test
public void testCPFMalFormated() throws Exception {

    //Arrange
    final String personCPF = "12345678923";
    final String personName = "person1";
    final String personEmail = "person1@email.com.br";
    final LocalDate personDate = LocalDate.parse("2000-01-01");
    final PersonDTO personDTO = new PersonDTO(personCPF,personName,personEmail, personDate);
    
    when(personService.save(any(Person.class))).thenThrow(new CPFBadFormatedException(personCPF));
    
    //Act
    this.mockMvc.perform(post("/api/persons")
            .content(objectMapper.writeValueAsString(personDTO))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8"))
            //Assert
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(personCPF)))
            .andDo(print());
}
```
Primeira ponto vamos extrair a criação do `personDTO` visto que essa seqUência de comandos já é utilizada em dois testes diferentes.

```java
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
```

Agora vamos fazer o teste passar. Para isso iremo utilizar um advice. Dessa maneira qualquer um dos nossos controllers que tenha que tratar uma exceção do tipo `CPFBadFormatedException` irá tratá-la da mesma maneira.

Vamos criar a classe `CPFBadFormatedAdvice` no pacote `com.webcarros.advices`. Com o seguinte conteúdo:

```java
@ControllerAdvice
public class CPFBadFormatedAdvice {
	
	@ResponseBody
	@ExceptionHandler(CPFBadFormatedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String cpfBadFormatedHandler(final CPFBadFormatedException e) {
		return e.getMessage();
	}
}
```

Pronto! Agora temos novamente o teste passando. Porém é bom notar que temos um novo requisito. A exceção que indica CPF inválido deve carregar o valor do CPF. Então temos que modificar o testes `testWrongFormatedCPFExcpetionThrown` para garantir que a exceção possui essa informação.

```java
@Test
void testWrongFormatedCPFExcpetionThrown() {
    String cpf = "123.ABC.789-78";
    //Arrange
    final Person person = new Person(cpf, "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));		
    
    //Act
    //Assert
    final CPFBadFormatedException ex = assertThrows(CPFBadFormatedException.class, () -> personService.save(person));
    
    assertThat(ex.getMessage()).contains(cpf);
}
```
Para atender o teste temos que modificar o método da seguinte forma:

```java
public Person save(final Person person) {
    
    final String cpf = this.removeDotsAndTraces(person.getCpf());
    
    if (!cpf.matches("^[0-9]{11}$")) {
        throw new CPFBadFormatedException(person.getCpf());
    }
    
    person.setCpf(cpf);
    
    return personRepository.save(person);
}
```