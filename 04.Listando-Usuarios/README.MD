# Implementando a API

Bom nem mesmo as mais belas linhas de código valem alguma coisa se a aplicação é incapaz de se comunicar com o mundo exterior.

## Preparando a aplicação

Para realizar os testes unitários em nossos controllers teremos que subir o cotexto do Spring, porém não iremos subí-lo de maneira integral. Por isso, teremos que realizar uma pequena modificação em nossa classe `WebCarrosApplication` que deve estar da seguinte maneira:

```java
@SpringBootApplication
@Slf4j
public class WebCarrosApplication {

	public static void main(final String[] args) {
		SpringApplication.run(WebCarrosApplication.class, args);
	}
	
	@Bean
	CommandLineRunner initDatabase(final PersonRepository repository){
		return args -> {
			log.info("Saving... " + repository.save(new Person("01234567887", "Ubuntu", "ubuntu@ubuntu.com.br", LocalDate.parse("2000-01-01"))));
		};		
	}
}
```
Iremos remover o bean initDatabase, pois não iremos carregar o bean `PersonRepository`no contexto para realizar um teste nos controllers. Dessa maneira a classe ficaria da seguinte maneira:

```java
@SpringBootApplication
public class WebCarrosApplication {

	public static void main(final String[] args) {
		SpringApplication.run(WebCarrosApplication.class, args);
	}
}
```
## Criando o teste de Unidade
Novamente iremos iniciar com a classe de teste.

Dentro do caminho `src/test//java` crie a classe `PersonRestControllerTest` dentro do pacote `com.webcarros.api`. Com o cursor sobre o nome da classe pressione `crtl+J` e crie a classe `PersonRestController` que ira implementar os requisitos.

Nesse momento devemos ter duas classes em branco. A classe de teste:

```java
public class PersonRestControllerTest {
}
```

E a classe que implementa a regra de negócio:

```java
public class PersonRestController {
}
```

Bom agora é o momento de conversar com o analista de negócio e/ou o desenvolvedor front-end a fim de estabelecer os contratos que serã cumpridos pela interface.

Vamos começar de maneira simples:

---
**LISTAGEM DE USUÁRIOS**

* **ROTA:** /api/persons
* **METODO:** GET
* **STATUS(SUCESSO):** 200
* **BODY:** Listagem dos usuários   
---

Muito bem vamos transcrever esses requisitos em forma de um teste.

```java
@WebMvcTest(PersonRestController.class)
public class PersonRestControllerTest {
	
	@Autowired MockMvc mockMvc;
	
	@Autowired ObjectMapper objectMapper; 
	
	@Test
	void testListUsersAPI() throws Exception {
		//Arrange
		final Person person1 = new Person("12345678923","person1","person1@email.com.br", LocalDate.parse("2000-01-01"));
		final Person person2 = new Person("98765431897","person2","person2@email.com.br", LocalDate.parse("2000-01-02"));
		
		final List<Person> personList = List.of(person1,person2);
		//Act
		this.mockMvc.perform(get("/api/persons"))
			//Assert
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(personList)))
			.andDo(print());
	}
}
```

Vamos linha a linha:

* Temos que atentar para o fato de que o método está invocando uma rota da aplicalção e não um método de uma classe. Isso quer dizer que vamos precisar carregar o contexto do Spring para que ele lide com a exposição e roteamento dos endpoints da aplicação. Porém não queremos carregar todo o contexto do Spring queremos apenas a parte necessária para lidar com as rotas desse teste. Por isso usamos a notação `@WebMvcTest` passando para ela a classe `PersonRestController` dizendo ao Spring que queremos que ele carregue somente o necessário para instrumentar essa classe.

* `@Autowired MockMvc mockMvc` esse componente permite realizar requisições para a API, e tratar e validar as respostas. 

* `@Autowired ObjectMapper objectMapper` vamos utilizar esse componente disponibilizado pelo Spring para serializar, deserializar os objetos em nossas respostas.

* Contruímos a resposta esperada pelo teste, nesse caso uma lista com dois usuários

* Utilizando o `mockMvc` realizamos um GET na rota especificada pelo requisito.

* Validamos as respostas utizando o método `andExpect()`

Agora temos que implementar a classe que implementa a regra de negócio de fato.

```java
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonRestController {
	
	private final PersonService personService;
	
	@GetMapping
	public List<Person> listPersons() {
		return personService.listAll();
	}
}
```
Vamos discutir um pouco sobre as anotações:

* `@RestController` marca a classe como um bean do Spring. Além disso informa também ao spring que os retornos dos médotos devem ser encaminhados diretamente ao a saída do servlet, sem passar pelo template resolver.

* `RequestMapping("/api/persons")` define qual rota esse controller deve atender. A rota definida no nível de classe será préfixada as rotas definidas pelos métodos.

* `@RequiredArgsConstructor` para que o Lombok crie o construtor com os campos definidos como final

* `@GetMapping` define a rota para o método e também indica que o método http que será aceito será o médoto `GET`.

Analisando a implementação da classe que atende o requisito vemos que ela utiliza uma classe externa para atender o requisito, a classe `PersonService`. Então temos que fazer o Mock dos métodos utilizados em nosso teste.

Modificando a nossa classe de teste:

```java
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
```

Observe que desta vez utilizamos a notação `@MockBean` no lugar da `@Mock` pois, quem está gerenciando nossas dependências é o Spring.

Pronto, clicando com o botão direito sobre a classe de teste e selecionando `Run As -> JUnit Test` tudo deve funcionar.

Só que analisando nossa aplicação vemos que ela possui um grave problema. Como estamos retornando uma entidade de banco de dados qualquer requisito que modifique nossas entidades de banco levará a uma mudança em nossa interface. São coisas separadas e devem ser tratadas de maneira separada mas, isso fica para a próxima etapa, pois será a base para testar métodos que salvam entidades.