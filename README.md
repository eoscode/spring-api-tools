# Spring API Tools

<p align="center">
  Simplifica o desenvolvimento de APIs, através de abstrações e padronizações que normalmente realizamos de forma
  repetitiva, durante o desenvolvimento.
</p>

## Features

 * Abstrações para Repository, Service e Resource.
 * Desenvolvimento de API para CRUD sem necessidade de implementar código. Suporte aos métodos GET, POST,
 DELETE, PUT e PATH 
 * Suporte a NoDelete annotation, para gerenciar delete lógico. 
 * Suporte a Find e FindAttribute annotation para configurar padrão de busca.
 * Configuração básica para exception handler, através de @RestControllerAdvice.
 * Suporte a query nos resources sem a implementação de código. Resource {EntityType}/query, através de método GET e POST 
 
## Como utilizar

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

### Repository

As classes `Repository` devem ser especializações da implementação do framework 
`com.eoscode.springapitools.data.repository.Repository`.

```java
package com.eoscode.springapitools.sample.repository;


import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.entity.City;

@org.springframework.stereotype.Repository
public interface CityRepository extends Repository<City, String> {}
```

### Service

As classes `Service` devem ser especializações de `AbstractService`, que implementa as rotinas para `save, update, delete,
find, findById, query e etc`.

```java
package com.eoscode.springapitools.sample.service;

import com.eoscode.springapitools.sample.entity.City;
import com.eoscode.springapitools.sample.repository.CityRepository;
import com.eoscode.springapitools.service.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class CityService extends AbstractService<StateRepository, City, String> {}
```

### Resource

As classes `Resource` devem ser especializações de `AbstractResource` ou `AbstractRepositoryResource`.

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

### {path}/ e {path}/find

Funcionalidade disponível em `AbstractResouce` e `AbstractRepositoryResource`, que possibilita aplicar filtros nos 
atributos da entidade.

Por padrão, todos os atributos são suportados (tipos primitivos e objetos). Contudo, os tipos primitivos são configurados
para ignorar o valor default. 
Ex.: Para atributo do tipo **int** e **long**, o valor zero é ignorado, para **boolean** o valor false.

Para mudar esse comportamento, temos a configuração `ignoreDefaultValue` na annotation `Find`, que por default, informa que
os valores default para tipo primitivo, deve ser ignorados. Se necessário, podemos configurar uma lista de atributos que 
devem ser exceção ao tratamento default `supportedDefaultValueForAttributes`.

A annotation `FindAttribute` possui um comportamento similiar ao `Find`, porém, aplicado aos atributos.   

### {path}/query

Diferente do `/find`, o suporte a `/query`, permite realizar consultas complexas.

Operadores suportados:

|operador   |descrição           |GET |POST|
|-----------|--------------------|----|----|
|\>         | maior que          |[x] |[x] |
|>=         | maior ou igual que |[x] |[x] |
|<          | menor que          |[x] |[x] |
|<=         | menor ou igual que |[x] |[x] |
|=          | igual              |[x] |[x] |
|!=         | diferente de       |[x] |[x] |
|$like      | contémm o valor    |[x] |[x] |
|$notLike   | não contém o valor |[x] |[x] |
|$isNull    | valor é null       |[x] |[x] |
|$isNotNull | não é valor null   |[x] |[x] |
|$btw       | entre valores      |[ ] |[x] |
|$in        | algum dos valores  |[ ] |[x] |

#### Exemplos:
##### Método **GET**

* Consultar cidades com população  maior ou igual `40000`
```http request
/api/cities/query?opt=population>=40000
```
* Consultar cidades com stateId igual a `52e0a6a7-d72d-4b0f-bab9-aebfcf888e21` e população maior que `20000`
```http request
/api/cities/query?opt=stateId=52e0a6a7-d72d-4b0f-bab9-aebfcf888e21&population>20000
```  
* Consultar cidades com população entre `40000` e `550000`
```http request
/api/cities/query?opt=population$btw40000;55000
```  
* Consultar estados que possuem cidades com população maior ou igual a `50000`
```http request
/api/state/query?opt=cities.population>=50000
```  

##### Método **POST**

* Consultar cidades com população  maior ou igual `40000`
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
* Consultar cidades com stateId igual a `52e0a6a7-d72d-4b0f-bab9-aebfcf888e21` e população maior que 20000
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
