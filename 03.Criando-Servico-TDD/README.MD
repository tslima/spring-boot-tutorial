# Implementando a classe de Serviço

## Preparando a aplicação

Bom agora iremos implementar a classe de serviço, que irá atender aos requisistos levantados no final da última etapa.

Primeiramente tem-se que remover a dependência Rest Repositories pois, eremos tratar os dados antes dela ser entregue a camada de persistência.

Abra o arquivo `pom.xml` localize a depência abaixo e a remova

```xml
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>
```

Isso irá forçá-lo a remover a notação `@RepositoryRestResource` da classe `PersonRestRepository`. Pode-se aproveitar o momento também para renomear a interface afim de explicitar suas novas funções. Para isso com o cursor sobre o nome da interface pressione `alt+shift+R` e informe o nome `PersonRepository` e aguarde enquanto o STS refatora as referências para o arquivo.

A interface deve ficar da seguinte forma

```java
public interface PersonRepository extends JpaRepository<Person, String> {
}
```

## Criado a classe de Serviço

O próximo passo é criar a classe de serviço. Para isso navamente selecione o pacote raiz, criar nova classe, informe no nome do pacote `com.webcarros.services` e o nome de classe `PersonService`.

As classes de serviço abrigam grande parte das regras de negócio da aplicação. É nelas que o trabalho é de fato realizado. Nessa camada é feita a ponte entre a interface da aplicação e a camada de persistência, aplicando todas as regras e restrições necessárias. Bom uma vez criada a classe temos que anotá-la com a anotação `@Service` para que ela seja adicionada ao contexto do Spring. Como ela faz a comunicação com a camada de persistência iremos adicionar como um atributo o repositório PersonRepository. Por fim, para que o Spring consiga injetar a dependência corretamente iremos adicionar a anotação `@RequiredArgsConstructor` para que o Lombok gere o construtor da classe.

Com isso a classe deve ficar da seguinte maneira:

```java
@Service
@RequiredArgsConstructor
public class PersonService {
	
	private final PersonRepository personRepository;	
}
```
Toda infraestrutura provida podemos começar a implementar os métodos, correto? Não! Agora é momento de escrever os testes.

## Criando os testes de unitários

Parece estranho criar um teste para algo que ainda não existe. Porém o momento de escrever um teste unitário é o momento de mapear em código um requisito. Veja, não estou dizendo atender, um requisito. Quem atende o requisito é a implementação dele, o teste traduz a linguagem convencional para uma que pode ser validada automaticamente pelo build do sistema.

### Preparando o sistema

Para realizar os testes unitários vamos utilizar o `JUnit5` para isso adicione as seguintes dependências no `pom.xml` da aplicação

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
	<artifactId>junit-jupiter-engine</artifactId>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.mockito</groupId>
	<artifactId>mockito-junit-jupiter</artifactId>			
	<scope>test</scope>
</dependency>
```

Além disso, vamos adicionar a extensão `MoreUnit` ao STS para facilitar a navegação entre a classe e a classe de teste unitário que a implementa.

Depois de tudo instalado com o cursor sobre o nome da classe `PersonService` pressione `ctrl+J` um formulário irá se abrir. A extensão já se ocupa da parte de criar o nome da classe te teste como `PersonServiceTest` no pacote correspondente porém no diretório `src/test/java`. Selecione `JUnit5` e clique em finish

Para prosseguirmos é preciso diferenciar dois conceitos:

* **Teste Unitário:** também chamados de teste de unidade, ou testes automatizados. Testes unitários testam apenas o comportamento da classe sobre teste. Qualquer agente externo, como outras classes, banco de dados, deve ser maquetado de maneira atender o teste. Esse métodos serão testados pela classe de teste de unidade desses métodos. Testes unitários também não devem provocar side-effects, ou seja alterar o estado da aplicação, um exemplo simples é a criação, ou remoção de registros no banco de dados.

* **Teste de Integração:** são os testes que validam o comportamento dos componentes em conjunto. Como cada um interfere no funcionamento do outro.

Em outras palavras, embora nossa classe dependa do repositório `PersonRepository` e, em última instância, do banco de dados, nossos testes não devem depender deles. Para isso temos que montar uma maquete, ou seja prover uma interface, que porém, irá responder exatamente da maneira que precisamos para o teste, ou no jargão do desenvolvimento iremos **mockar** a classe `PersonRepository`.

Feita a digreção retornando a classe `PersonServiceTest` modifique-a para que fique da seguinte forma:

```java
@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {
	@InjectMocks private PersonService personService;
	@Mock private PersonRepository personRepository;
}
```

Vamos entendê-la linha a linha:

* `@ExtendWith(MockitoExtension.class)` indica que em nossos testes iremos utilizar objetos "mockados". Outras referências usarão a notação `@SpringBootTest` aqui. Dese-se tomar um cuidado especial em relação a isso. Essa notação faz com que todo o contexto do spring seja carregado, o que pode levar alguns segundos ao contrário da primeira notação que não carrega o contexto do Spring fazendo com que os testes levem apenas alguns milisegundos. A `@SpringBootTest` deve ser utilizada em testes de integração.
* `@InjectMocks` essa notação deve ser utilizada na classe em que serão realizar os testes. Ela indica que um objeto da classe deve ser instanciado e que os mocks das outras classes injetados nela.
* `@Mock` a notação identifica que a classe deve ser mockada e depois injetada na classe sobre teste.

Bom depois de toda essa infra gerada chega a hora de pensar em entradas e saídas, ou seja que informações eu desejo entregar para a classe `PersonService` e qual o resultado que espero depois dela operar sobre essas informações.

Um pouco abstrato demais não? Sem problema vamos começar do princípio

### Listagem dos usuários

Começando de maneira bem simples, Eu desejo obter a lista de usuários do sistema que seja necessário passar nenhum valor para a clsse.

Ótimo traduzindo isso para linguagem de desenvolvimento:

```java
@Test
public void testListPersons() {
	final List<Person> ret = personService.listAll();		
}
```

A notação `@Test` indica para o sistema que o método anotado é um teste e deve ser executado na fase de testes unitários. Importante o retorno desse método deve ser void. As validações são feitas internamente como veremos a seguir.

Bom quando você adicionar isso a classe de teste será exibido um erro indicando que o método **listAll()** não existe. Sem problema, sobre o erro pressione `ctrl+1` e selecione implementar o método. A implementação na classe `PersonService` é direta:

```java
public List<Person> listAll() {
	return personRepository.findAll();
}
```

Aí chegamos a uma questão a classe `PersonService` está chamando o método `findAll()` da interface `PersonRepository`. Como vimos anteriomente esse método deve ser mockado.

Voltando a classe de teste:

```java
@Test
public void testListPersons() {
    
    final Person person1 = new Person("123456789", "NAME-1", "email-1@email.com.br", LocalDate.parse("2000-01-01"));
    final Person person2 = new Person("987654321", "NAME-2", "email-2@email.com.br", LocalDate.parse("2000-01-01"));
    
    when(personRepository.findAll()).thenReturn(List.of(person1, person2));
    
    final List<Person> ret = personService.listAll();		
}
```
Primeiro criamos dois usuários, afinal desejamos uma lista e seria interessante possuir mais de uma entidade. Depois temos de fato a parte de mockar o método. O pacote `Mockito` disponibiliza o método `when` em que você deve passar o método que será invocado. E então o método `thenReturn` que permite definir o retorno. Em outras palavras quando invocar esse método, retone esses dados.

Falta a parte da verificação do teste. O que desejamos? Desejamos que algo seja feito? Na verdade não. Queremos que a lista seja retornada exatamente como é obtida logo:

```java
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
```

As últimas três linhas garantem que a lista tal qual foi retornada do repository e externada pela classe `PersonService`.

### Adicionando Usuários

Bom de volta a nossa classe de teste vamos imaginar a inserção de um usuário. O que eu desejo:

    Ao informar um usuário eu quero que ele seja adicionado ao sistema.

Muito bem, claro, mas, e quanto ao tratamento do CPF? Como devemos aceitá-lo?

E o usuário responde: 

    Não quero me importar com isso, ele pode vir com pontos e traços, ou somente números.

Ok mas... E se for um CPF inválido? 

    Me informe que ele é inválido.

Ótimo já podemos escrever nosso testes. Primeiro temos que tomar uma decisão, como salvaremos o CPF no banco de dados? Com ou sem os pontos e traços? No caso irei remover os pontos e traços, salvando apenas os números que devem ser 11.

> Obs: Não irei usar a classe de validação de CPF que vem pronta no java para que não precise usar CPFs reais. Para a nossa aplicação uma string de 11 números é um CPF válido.

Então o primeiro cenário é o que não temos que fazer nada. O usuário a ser salvo é informado com um CPF somente numérico.

Traduzindo para um teste:

```java
@Test
void testSaveUserAsInformed() {		
    //Arrange
    final Person person = new Person("12345678901", "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
            
    //Act
    final Person ret = personService.save(person);
    
    //Assert
    assertThat(person).isEqualTo(ret);
}
```

Novamente o STS irá apontar que o método save não existe. Novamente `ctrl+1` criar o método e para atender esse teste. A implementação também é direta, na classe `PersonService`:

```java
public Person save(final Person person) {
    return personRepository.save(person);
}
```
Bom nota-se que teremos que mockar o método save da interface `PersonRepository` mas, existe uma diferença básica. Ao contrário do método `listAll()` nesse caso queremos que o método mockado retorne exatamente o argumento passado para ele. Para isso temos que modificar o teste em nossa classe `PersonServiceTest`

```java
@Test
void testSaveUserAsInformed() {		
    //Arrange
    final Person person = new Person("12345678901", "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));
    when(personService.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
            
    //Act
    final Person ret = personService.save(person);
    
    //Assert
    assertThat(person).isEqualTo(ret);
}
```

Ou seja, quando o método save de `PersonRepository` for invocado ele irá retornar o argumento passado para o método.

Temos agora o outro cenário, se for passado um CPF com pontos e traços esse traços devem ser removidos antes da entidade ser passada para a camada de persistência. Novamente traduzindo para a linguagem de teste:

```java
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
```

Se executarmos o teste veremos que ele falha. Temos então que modificar a regra de negócio. Uma das implementações possíveis é:

```java
public Person save(final Person person) {
    
    final String cpf = person.getCpf().replace(".", "").replace("-", "");
    
    person.setCpf(cpf);
    
    return personRepository.save(person);
}
```

Por fim, no caso de um CPF inválido devemos informar que o CPF é inválido. Nesse caso iremos lançar uma exceção criada por nós `CPFBadFormatedException`:

Traduzindo essa necessidade para a linguagem de testes:
```java
@Test
void testWrongFormatedCPFExcpetionThrown() {
    //Arrange
    final Person person = new Person("123.ABC.789-78", "UBUNTU", "email@email.com", LocalDate.parse("2000-01-01"));		
    
    //Act
    //Assert
    assertThrows(CPFBadFormatedException.class, () -> personService.save(person));
}
```
Nesse caso estamos verificando se o método save lança uma exceção. Novamente temos um teste falhando, e precisamos corrigir a classe `PersonService` para corrigir a necessidade.

Uma das soluções possíveis:

```java
public Person save(final Person person) {
    
    final String cpf = person.getCpf().replace(".", "").replace("-", "");
    
    if (!cpf.matches("^[0-9]{11}$")) {
        throw new CPFBadFormatedException();
    }
    
    person.setCpf(cpf);
    
    return personRepository.save(person);
}	
```

Bom a classe service tem que implementar além desses os métodos de atualização, seleção e remoção dos usuários mas, acredito que a dinâmica está estabelecida.

Nos arquivos de código estão disponíveis todos os testes realizados.

Implementamos nossa classe de serviço mas, falta a nossa API. Que é assunto da próxima etapa.