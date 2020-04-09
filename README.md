# Spring API Tools

<p>
  Simplifica o desenvolvimento de APIs, através de abstrações e padronizações que normalmente realizamos de forma
  repetitiva, durante o desenvolvimento.
</p>

## Features

 * Abstrações para Repository, Service e Resource.
 * Desenvolvimento de API para CRUD sem necessidade de implementar código. Suporte aos métodos GET, POST,
 DELETE, PUT e PATH 
 * Suporte a NoDelete annotation, para gerenciar delete lógico. 
 * Suporte a Find e FindAttribute annotation para configurar filtros.
 * Configuração básica para exception handler, através de @RestControllerAdvice.
 * Suporte a query nos resources sem a implementação de código. Resource {path}/query, através de método GET e POST 
 
## Como utilizar

Para começar, devemos configurar o Spring para carregar as configurações do `Spring-API-Tools`. 

### Dependência
```xml
<dependency>
    <groupId>com.github.eoscode</groupId>
    <artifactId>spring-api-tools</artifactId>
    <version>1.0.6-RELEASE</version>
</dependency>
```

### Carregando o spring-api-tools com `@Configuration` do Spring
```java
package com.eoscode.springapitools.sample.config;

import com.eoscode.springapitools.config.SpringApiToolsScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringApiToolsScanConfig extends SpringApiToolsScan {
}
```

### Entity

As entidades devem implementar a interface `Identifier` para indicar o atributo que representa a chave
da entidade.

```java
package com.eoscode.springapitools.sample.entity;

import com.eoscode.springapitools.data.domain.Identifier;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "city")
@NamedEntityGraphs(
        @NamedEntityGraph(name = "City.findDetailById", attributeNodes = {
                @NamedAttributeNode("state")
        })
)
public class City implements Identifier<String> {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "uuid")
    private String id;

    @Column(name = "name", length = 60)
    private String name;

    @JsonIgnoreProperties({"cities"})
    @JoinColumn(name = "state_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private State state;

    @Column(name = "state_id")
    private String stateId;

    @Column(name = "population")
    private long population;

}
```
#### @NamedEntityGraphs
Também é possível utilizar `@NamedEntityGraphs` para ajustar alguns comportamentos default. Para isso, é
necessário informar o `@NamedEntityGraph` com os seguintes nomes:

*  {Entity}.findById - Altera o comportamento default para busca por id.
*  {Entity}.findDetailById - Altera o comportamento default da busca detalhada por id.

Observações: 
+ Os `@NamedEntityGraph` devem ser declarados da seguite forma: nome da entidade + nome da query. Ex.: `{Entity}.findById`.
+ O `findDetailByid` deve ser definido quando for necessário um carregamento diferente do mapeamento da entidade,
realizado através de `fetch = FetchType.LAZY` do JPA. 
+ Por padrão, será utilizado o `findById` da implementação do `Spring Data`, caso identificado um `@NamedEntityGraph`, 
ele será selecionado de forma prioritária.

### Repository

A classe `Repository`, deve especializar a implementação do framework 
`com.eoscode.springapitools.data.repository.Repository`.

```java
package com.eoscode.springapitools.sample.repository;


import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.entity.City;

@org.springframework.stereotype.Repository
public interface CityRepository extends Repository<City, String> {}
```

### Service

A classe `Service`, deve especializar `AbstractService`, que implementa as rotinas para `save, update, delete,
find, findById, query e etc`.

```java
package com.eoscode.springapitools.sample.service;

import com.eoscode.springapitools.sample.entity.City;
import com.eoscode.springapitools.sample.repository.CityRepository;
import com.eoscode.springapitools.service.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class CityService extends AbstractService<CityRepository, City, String> {}
```

### Resource

A classe `Resource`, deve especializar `AbstractResource` ou `AbstractRepositoryResource`.

<table>
    <tr>
        <th>Path</th>
        <th>Método</th>
        <th>Resposta HTTP</th>
        <th>Descrição</th>
    </tr>
    <tr>
        <td>{path}/{id}</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza consulta pelo id da entidade.</td>
    </tr>
    <tr>
        <td>{path}/detail/{id}</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza consulta detalhada pelo id da entidade. Utiliza <b>findDetailByI</b> definido através de <b>@NamedEntityGraph</b>. </td>
    </tr>
    <tr>
        <td>{path}/</td>
        <td>POST</td>
        <td>201</td>
        <td>Salva a entidade. Devolve o header Location, indicando o caminho para o recurso.</td>
    </tr>
    <tr>
        <td>{path}/</td>
        <td>PUT</td>
        <td>204</td>
        <td>Atualiza a entidade.</td>
    </tr>
    <tr>
        <td>{path}/</td>
        <td>PATH</td>
        <td>204</td>
        <td>Atualiza a entidade de forma parcial, aplicando o update apenas aos atributos enviados.</td>
    </tr>
    <tr>
        <td>{path}/{id}</td>
        <td>DELETE</td>
        <td>204</td>
        <td>Deleta pelo id da entidade.</td>
    </tr>
    <tr>
        <td>{path}/</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza filto nos atributos da entidade. Também pode ser acessodo através de <b>{path}/{id}</b>. Por padrão,
        utiliza valores exatos, ou seja, o operador <b>=</b> (igual).
        </td>
    </tr>
   <tr>
        <td>{path}/query</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza query nos atributos da entidade, com suporte a múltiplos <a href="#operadores">operadores</a>.</td>
    </tr>
    <tr>
        <td>{path}/query</td>
        <td>POST</td>
        <td>200</td>
        <td>Realiza query nos atributos da entidade, com suporte a múltiplos <a href="#operadores">operadores</a>. 
        Obs.: Utiliza requisição JSON.</td>
    </tr>
    <tr>
        <td>{path}/all</td>
        <td>GET</td>
        <td>200</td>
        <td>Lista todas as entidades associadas ao recurso.</td>
    </tr>          
</table>


#### AbstractResource

Disponibiliza as funcionalidades comuns de um CRUD. Contudo, permite o desenvolvimento de novas funcionaldiades 
através do `AbstractService`. 

```java
package com.eoscode.springapitools.sample.resource;

import com.eoscode.springapitools.resource.AbstractResource;
import com.eoscode.springapitools.sample.entity.City;
import com.eoscode.springapitools.sample.service.CityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityResource extends AbstractResource<CityService, City, String> {}
```

#### AbstractRepositoryResource

Disponibiliza as funcionalidades comuns de um CRUD padrão, que não requer o desenvolvimento de funcionalidades adicionais.

```java
package com.eoscode.springapitools.sample.resource;

import com.eoscode.springapitools.resource.AbstractRepositoryResource;
import com.eoscode.springapitools.sample.entity.City;
import com.eoscode.springapitools.sample.repository.CityRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityResource extends AbstractRepositoryResource<CityRepository, City, String> {}
```

## Consultas

Por padrão, todas as consultas são realizadas com operador lógico `and`, quando possuem mais de um filtro. 
Contudo, é possível alterar esse comportamento, utilizar o parâmetro `operator`, que suporta o valor `and` e `or`.

### {path}/ e {path}/find

Funcionalidade disponível em `AbstractResouce` e `AbstractRepositoryResource`, que possibilita aplicar filtros nos 
atributos da entidade.

Por padrão, todos os atributos são suportados (tipos primitivos e objetos). Contudo, os tipos primitivos são configurados
para ignorar o valor default. 
Ex.: Para atributo do tipo **int** e **long**, o valor zero é ignorado, para **boolean** o valor false.

Para mudar esse comportamento, temos a configuração `ignoreDefaultValue` na annotation `Find`, que por default, informa que
os valores default para tipo primitivo, devem ser ignorados. Se necessário, podemos configurar uma lista de atributos que 
devem ser exceção ao tratamento default `supportedDefaultValueForAttributes`.

A annotation `FindAttribute`, possui um comportamento similiar ao `Find`, porém, aplicado aos atributos.   

### {path}/query

Diferente do `/find`, o suporte a `/query`, permite realizar consultas com um conjunto maior de operadores.
<a id=“operadores”><a/>

|Operador   |Descrição           |GET |POST|
|-----------|--------------------|----|----|
|\>         | maior que          |[x] |[x] |
|>=         | maior ou igual que |[x] |[x] |
|<          | menor que          |[x] |[x] |
|<=         | menor ou igual que |[x] |[x] |
|=          | igual a            |[x] |[x] |
|!=         | diferente de       |[x] |[x] |
|$like      | contém o valor     |[x] |[x] |
|$notLike   | não contém o valor |[x] |[x] |
|$isNull    | valor é NULL       |[x] |[x] |
|$isNotNull | valor não é NULL   |[x] |[x] |
|$btw       | entre valores      |[ ] |[x] |
|$in        | algum dos valores  |[ ] |[x] |

#### Exemplos:
##### Método **GET**

* Listar as cidades com população  maior ou igual a `40000` habitantes ou rate igual a 5.5,
 ordenado pelo número de habitantes de forma decrescente
```http request
/query?opt=population>=40000,rate=5.5&operator=or&sort=population,desc
```
* Listar as cidades com stateId igual a `52e0a6a7-d72d-4b0f-bab9-aebfcf888e21` e população maior que `20000` habitantes
```http request
/api/cities/query?opt=stateId=52e0a6a7-d72d-4b0f-bab9-aebfcf888e21&population>20000
```  
* Listar as cidades com população entre `40000` e `550000` habitantes
```http request
/api/cities/query?opt=population$btw40000;55000
```  
* Listar os estados que possuem cidades com população maior ou igual a `50000` habitantes
```http request
/api/state/query?opt=cities.population>=50000&distinct=true
```  
Obs.: 
* As consultas suportam `org.springframework.data.domain.Pageable` (parâmetro page e size), com os valores default do Spring.
* As consultas suportam `org.springframework.data.domain.Sort`, com os valores default do Spring.
* O valor default do parâmetro `distinct` é true. Sendo assim, pode ser omitido.

##### Método **POST**

* Listar cidades com população  maior ou igual a `40000` habitantes
```json
{
  "filters": [
    {
     "field": "population",
     "operator": ">=",
     "value": 40000
    }
  ],
"distinct": true
}
```
* Listar as cidades com stateId igual a `52e0a6a7-d72d-4b0f-bab9-aebfcf888e21` e população maior que `20000` habitantes
```json
{
  "filters": [
    {
     "field": "stateId",
     "operator": "=",
     "value": "52e0a6a7-d72d-4b0f-bab9-aebfcf888e21"
    },
    {
     "field": "population",
     "operator": ">",
     "value": 20000
    }
  ],
  "distinct": true
}
```
* Listar as cidades com população entre `40000` e `550000` habitantes
```json
{
  "filters": [
    {
     "field": "population",
     "operator": "$btw",
     "value": "40000;55000"
    }
  ]
}
```

O Layout da consulta, segue a seguinte definição: 

**Query com método POST**
```json
{
  "operator": "and",
  "filters": [
    {
     "field": "population",
     "operator": ">=",
     "value": 50000
    }
  ],
  "sorts": [
    {
     "field": "population",
     "direction": "ASC"
    }	
  ],
  "distinct": true
}
```

Obs.:
* O valor default do parâmetro `distinct` é true. Sendo assim, pode ser omitido.
* O tipo `Sort`, suporta `direction` com valores ASC e DESC.
* Todas as configurações de consulta, são realizadas com base no nome do atributo. Também é suportado consultas no
atributo filho (**ainda não suportado para o tipo Sort**).
* O parâmetro `operator`, possui valor default `and` e pode ser omitido.

Listar os estados que possuem cidades com população maior ou igual a 50000 habitantes.

```json
{
  "filters": [
    {
     "field": "cities.population",
     "operator": ">=",
     "value": 50000
    }
  ],
  "distinct": true
}
```
