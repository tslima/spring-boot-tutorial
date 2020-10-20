# Isolando Persistência da Camada de Apresentação

Bom como dito anteriormente não é uma boa idéia externar as entidades de banco na API, pois qualquer modificação no banco de dados implicaria em uma modificação na interface. Afim de isolar essas duas camadas iremos utilizar um DTO, um objeto de transferência de dados, que é uma classe simples java, apenas com atributos sem comportamento de fato.

## Preparando a aplicação

Para evitar a repetição de códigos do tipo:

```java
    dto.setParam1(entity.getParam1());
    dto.setParam2(entity.getParam2());
    dto.setParam3(entity.getParam3());
```

Iremos utilizar o componente `ModelMapper` que faz essa conversão entre campos de classes diferentes e nomes semelhantes.

Para isso no arquivo pom adicione a seguinte dependência:

```xml
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>2.3.8</version>
</dependency>
```

Como iremos utilizar a classe `ModelMapper` em várias de nossas classes vamos adicioná-lo ao contexto do Spring para que não tenhamos que lidar com a instanciação dele. Para isso adicione o seguinte método na classe `WebCarrosApplication`.

```java
@Bean
ModelMapper modelMapper() {
    return new ModelMapper();
}
```

## Modificando o Teste de Unidade

Novamente iremos começar pelo teste. Não queremos mais que a listagem de usuários retorne as entidades de banco, mas, sim, os DTOs. Modificando o teste temos:

```java
@Test
void testListUsersAPI() throws Exception {
    //Arrange
    final Person person1 = new Person("12345678923","person1","person1@email.com.br", LocalDate.parse("2000-01-01"));
    final Person person2 = new Person("98765431897","person2","person2@email.com.br", LocalDate.parse("2000-01-02"));
    
    final List<Person> personList = List.of(person1,person2);		
    when(personService.listAll()).thenReturn(personList);
    
    
    final PersonDTO person1DTO = new PersonDTO("12345678923","person1","person1@email.com.br", LocalDate.parse("2000-01-01"));
    final PersonDTO person2DTO = new PersonDTO("98765431897","person2","person2@email.com.br", LocalDate.parse("2000-01-02"));
    
    final List<PersonDTO> personDTOList = List.of(person1DTO, person2DTO);
    
    //Act
    this.mockMvc.perform(get("/api/persons"))
        //Assert
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(personDTOList)))
        .andDo(print());
}
```

Vamos utilizar algumas variáveis para remover toda a repetição de valores

```java
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
```

## Implementando a funcionalidade

Teste escrito, hora de fazê-lo passar, com o cursor sobre a declaração de `PersonDTO`. pressione `ctrl+1` e crie a classe `PersonDTO` no pacote `com.webcarros.dto`.

Para este DTO em específico teremos uma implementação identica à entidade

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
	
	private String cpf;
	
	private String name;
	
	private String email;	
	
	private LocalDate birthDate;
}
```
Agora voltamos para a classe `PersonRestController` onde iremos adicionar o método que faz a consersão da entidade para o DTO. Adicionando a dependência do `modelMapper` e implementando o método de conversão:

```java
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonRestController {
	
	private final PersonService personService;
	
	private final ModelMapper modelMapper;
	
	@GetMapping
	public List<Person> listPersons() {
		return personService.listAll();
	}
	
	private PersonDTO toDTO(final Person entity) {
		return modelMapper.map(entity, PersonDTO.class);
	}
}
```

Focando agora no método `listPersons` vamos adicionar o mapeamento da entidade para o DTO:

```java
@GetMapping
public List<PersonDTO> listPersons() {
    return personService.listAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
}
```

Pronto! Teste passando, camadas de persistência e apresentação isoladas. Na próxima etapa trataremos a inserção de dados.