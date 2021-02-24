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
 * Suporte a query nos resources sem a implementação de código. Resource {path}/query, através de método GET e POST. 
 * Consultas com configuração de join e fecth de forma dinâmica e sem codificação.
 * Suporte a query com definição de visualização, onde é possível definir quais atributos devem ser retornados.
 
## Como utilizar

Configure o Spring para carregar as configurações do `Spring-API-Tools`. 

### Dependência
```xml
<dependency>
    <groupId>com.github.eoscode</groupId>
    <artifactId>spring-api-tools</artifactId>
    <version>1.3.0-RELEASE</version>
</dependency>
```

### Carregando o spring-api-tools com `@Configuration` do Spring
```java
package com.eoscode.springapitools.sample.config;

import com.eoscode.springapitools.config.SpringApiToolsScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringApiToolsScanConfig extends SpringApiToolsScan {}
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

A classe `Repository`, deve especializar a implementação do framework `com.eoscode.springapitools.data.repository.Repository`.

```java
package com.eoscode.springapitools.sample.repository;


import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.entity.City;

@org.springframework.stereotype.Repository
public interface CityRepository extends Repository<City, String> {}
```

### Service

A classe `Service`, deve especializar `AbstractService`, que implementa as rotinas para `save, update, delete, find, findById, query e etc`.

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
        <td>Realiza consulta detalhada pelo id da entidade. Utiliza <b>findDetailByI</b> definido através de <b>@NamedEntityGraph</b>.</td>
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
        <td>Remove a entidade, utilizando o id.</td>
    </tr>
    <tr>
        <td>{path}/</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza filtro nos atributos da entidade. Também pode ser acessado através de <b>{path}/{find}</b>. Por padrão,
        utiliza valores exatos, ou seja, o operador <b>=</b> (igual).
        Retorna uma lista da entidade consulta. Contudo, podemos usar o parâmetro <b>pageable</b> com valor true para 
        retornar o tipo <b>Page</b> do Spring.
        </td>
    </tr>
    <tr>
        <td>{path}/page</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza filtro nos atributos da entidade. Também pode ser acessado através de <b>{path}/find/page</b>. Por padrão,
        utiliza valores exatos, ou seja, o operador <b>=</b> (igual).
        Retorna o tipo <b>Page</b> do Spring.
        </td>
    </tr>
   <tr>
        <td>{path}/query</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza query nos atributos da entidade, com suporte a múltiplos <a href="#operadores">operadores</a>.
        Retorna uma lista da entidade consulta. Contudo, podemos usar o parâmetro <b>pageable</b> com valor true para retornar o tipo 
        <b>Page</b> do Spring
        </td>
    </tr>
    <tr>
        <td>{path}/query/page</td>
        <td>GET</td>
        <td>200</td>
        <td>Realiza query nos atributos da entidade, com suporte a múltiplos <a href="#operadores">operadores</a>.
        Retorna o tipo <b>Page</b> do Spring. 
        </td>
    </tr>
    <tr>
        <td>{path}/query</td>
        <td>POST</td>
        <td>200</td>
        <td>Realiza query nos atributos da entidade, com suporte a múltiplos <a href="#operadores">operadores</a>.
        Retorna uma lista da entidade consulta. Contudo, podemos usar o parâmetro <b>pageable</b> com valor true para
         retornar o tipo <b>Page</b> do Spring. 
        <br>Obs.: Utiliza requisição JSON.</td>
    </tr>
    <tr>
        <td>{path}/query/page</td>
        <td>POST</td>
        <td>200</td>
        <td>Realiza query nos atributos da entidade, com suporte a múltiplos <a href="#operadores">operadores</a>.
        Retorna o tipo <b>Page</b> do Spring. 
        <br>Obs.: Utiliza requisição JSON.</td>
    </tr>    
    <tr>
        <td>{path}/all</td>
        <td>GET</td>
        <td>200</td>
        <td>Lista todas as entidades associadas ao recurso.</td>
    </tr>          
</table>


#### AbstractResource

Disponibiliza as funcionalidades comuns de uma API CRUD. Contudo, permite o desenvolvimento de novas funcionalidades, 
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
Para alterar esse comportamento, utilize o parâmetro `operator`, que suporta o valor `and` e `or`.

As consultas passaram a ser executadas com paginação desabilitada, a partir da versão 1.1.0. Sendo assim, devemos utilizar
os novos recursos `{path}/page e {path}/query/page` para realizar consultas paginadas ou utilizar `{path}/ e {path}/query` com o 
parâmetro `pageable informando valor true`, para indicar que a consulta deve retornar o tipo Page do Spring.
 
Outra forma de configurar o comportamento da paginação, é através do arquivo de configuração do Spring `application.proporties ou application.yml`:

```yaml
spring-api-tools:
  enable-default-pageable: false //valor default
``` 
O valor `true`, irá indicar que toda consulta deve retornar o tipo `Page` do Spring. O parâmetro `pageable`, enviado nas consultas, 
pode ser utilizado para mudar o comportamento padrão.

Obs.: A paginação e ordenação, utilizam as configurações padrões e sintaxe do Spring. Sendo assim, devemos consultar a documentação do 
framework, se necessário alterar algum comportamento padrão.

```yaml
spring:
  data:
    rest:
      default-page-size: 10 //valor default do spring
``` 

As consultas realizadas sem paginação, suportam o parâmetro `size` para impor um limite na quantidade de registros retornados.
Essa configuração pode ser realizada por consulta ou de forma global, no `application.yml` do Spring.

Também é possível desabilitar o override do `size` nas consultas, e forçar o uso da configuração global.

```yaml
spring-api-tools:
  list-default-size-override: true //permite alterar o valor do size nas consultas
  list-default-size: 0 //valor limite desabilitado. Se diferente de zero, ira impor o limite
``` 

Filtros aplicados a atributos do tipo string, realizam comparação com case sensitive habilitado. Para alterar o comportamento, configure o 
parâmetro `string-case-sensitive`, que indica se o valor informado na expressão, será no formato lowercase ou uppercase.    

```yaml
spring-api-tools:
  string-case-sensitive: lowercase
```

#### Tipos suportados

<table>
    <tr>
        <th>Tipo</th>
        <th>Operadores</th>
    </tr>
    <tr>
        <td>String</td>
        <td>=, !=, $like, $notLike, $startsWith, $endsWth, $btw, $in, $isNull, $isNotNull</td>
    </tr>
    <tr>
        <td>Integer / int</td>
        <td>=, !=, >, >=, <, <=, $in, $btw, $isNull, $isNotNull</td>
    </tr>
    <tr>
        <td>Long / long</td>
        <td>=, !=, >, >=, <, <=, $in, $btw, $isNull, $isNotNull</td>
    </tr>
    <tr>
        <td>Double / double</td>
        <td>=, !=, >, >=, <, <=, $in, $btw, $isNull, $isNotNull</td>
    </tr>
    <tr>
        <td>Boolean / boolean</td>
        <td>=, !=, $isNull, $isNotNull</td>
    </tr>
    <tr>
        <td>BigDecimal</td>
        <td>=, !=, >, >=, <, <=, $in, $btw, $isNull, $isNotNull</td>
    </tr>
    <tr>
        <td>Date</td>
        <td>=, !=, >, >=, <, <=, $in, $btw, $isNull, $isNotNull
        <br>Obs.: Não suportado por <b>{path}/ e {path}/find</b>.
        <br>Suporta representação da data no formato timestamp (long) e ISO 8601 no formato UTC time zone (String)</td>
    </tr>
    <tr>
        <td>List</td>
        <td>$size, $isEmpty, $isNotEmpty</td>
    </tr>
    <tr>
        <td>Set</td>
        <td>$size, $isEmpty, $isNotEmpty</td>
    </tr>    
</table>    

### Join e Fetch

As consultas suportam configuração automática de join, baseada na sintaxe `atributo.atributo`, onde a primeira ocorrência,
indica o atributo da Entidade consultada e mapeada no JPA com OneToMany, ManyToMany ou ManyToOne. 
A segunda ocorrência, o atributo da classe que foi associada ao mapeamento e que será aplicado o filtro. 
Sendo assim, quando realizamos filtros utilizando essa sintaxe, é realizado um left outer join de forma automática, possibilitando 
consultas com um nível de profundidade.

Para indicar que a consulta deve retornar os dados da associação `join`, é possível utilizar o parâmetro `fetches` nas consultas com método 
**GET**, onde se espera uma lista com o nome dos atributos que representam as associações. Ou o parâmetro `fetch`, introduzido 
no filtro das consultas com método **POST**.

As consultas realizadas com mensagens do tipo `json` e método **POST** são mais flexíveis, em relação à configuração do join,
uma vez que permitem indicar se o join será do tipo `inner outer join` ou `left outer join` (default), além de uma configuração 
centralizada para o `fetch`.

#### Consultar as cidades, onde o nome do Estado inicia com pe.

**GET** 
```http request
/api/cities/query?opt=state.name=%startsWithpe&fetches=state
```  

**POST**
```json
{
  "filters": [
    {
     "field": "state.name",
     "operator": "$startsWith",
     "value": "pe",
     "fetch":true
    }
  ]
}
```
ou
```json
{
  "filters": [
    {
     "field": "state.name",
     "operator": "$startsWith",
     "value": "pe"
    }
  ],
  "joins": [
    {
     "field": "state",
     "fetch": "true"
    }
  ]
}
```

Esse comportamento pode ser desabilitado no arquivo de configuração do Spring.
```yaml
spring-api-tools:
  query-with-join-configuration: false // desabilitar o suporte a consultas com configuracao de join
``` 

### {path}/ e {path}/find

As funcionalidades disponíveis em `AbstractResouce` e `AbstractRepositoryResource`, possibilitam aplicar filtros nos atributos da entidade.

Por padrão, todos os atributos são suportados (tipos primitivos e objetos). Contudo, os tipos primitivos são configurados
para ignorar o valor default. 
Ex.: Para os atributos do tipo **int** e **long**, o valor zero é ignorado, para **boolean**, o valor false.

Para mudar esse comportamento, temos a configuração `ignoreDefaultValue` na annotation `Find`, que por padrão, informa que
os valores default, devem ser ignorados. Se necessário, podemos configurar uma lista de atributos que devem ser exceção ao tratamento
default `supportedDefaultValueForAttributes`.

A annotation `FindAttribute`, possui um comportamento similiar ao `Find`, porém, aplicado aos atributos.   

### {path}/query

Diferente do `/find`, o suporte a `/query`, permite realizar consultas com um conjunto maior de operadores.

#### Operadores
<table>
    <tr>
        <th>Operador</th>
        <th>Descrição</th>
        <th>GET</th>
        <th>POS</th>
    </tr>
    <tr>
        <td>></td>
        <td>Maior que</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>
    <tr>
        <td>>=</td>
        <td>Maior ou igual que</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>
    <tr>
        <td><</td>
        <td>Menor que</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td><=</td>
        <td>Menor ou igual que</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>=</td>
        <td>Igual a</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>!=</td>
        <td>Diferente de</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>$like</td>
        <td>Contém o valor</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>
    <tr>
        <td>$notLike</td>
        <td>Não contém o valor</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>$isNull</td>
        <td>Valor é NULL</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>$isNotNull</td>
        <td>Valor não é NULL</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>$btw</td>
        <td>Entre valores
        Sintaxe: "10;50" (deve ser informado como <b>String</b>)</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>    
    <tr>
        <td>$in</td>
        <td>Algum dos valores
        Sintaxe: "2;4;5;6" (deve ser informado como <b>String</b>)</td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>
    <tr>
        <td>$size</td>
        <td>Verifica o número de ocorrências na coleção, com suporte aos operadores: <b>>, >=, <, <=, =, !=</b>.
        Sintaxe: <b>operador</b>;<b>valor</b>. Ex.: ">=;2" (deve ser informado como <b>String</b>).
        <br>Obs.: Aplicado a atributos do tipo coleção. 
        </td>
        <td>[ ]</td>
        <td>[x]</td>
    </tr>
    <tr>
        <td>$isEmpty</td>
        <td>Verifica se a coleção está vazia
        <br>Obs.: Aplicado a atributos do tipo coleção.
        </td>
         <td>[x]</td>
         <td>[x]</td>
    </tr>
    <tr>
        <td>$isNotEmpty</td>
        <td>Verifica se a coleção não está vazia
        <br>Obs.: Aplicado a atributos do tipo coleção.
        </td>
        <td>[x]</td>
        <td>[x]</td>
    </tr>            
</table>

#### Exemplos:
##### Método **GET**

Para consultas com método `GET`, devemos utilizar a sintaxe `atributo` + `operador` + `valor`. Quando mais de um filtro for 
aplicado a consulta, devemos utilizar o separador `,` para indicar o limite de cada filtro. Os filtros são informados através do parâmetro `opt`.

Os filtros que utilizam operadores que não suportam valor, tais como: `$isNull`, `$isNotNull`, `$isEmpty`, `$isNotEmpty`, 
devem omitir o valor. Ex.: `name$isNotNull`. 

A partir da versão **1.1.0**, podemos utilizar o parâmetro `filters` com sintaxe `multiple values` para informar múltiplos 
filtros.

```http request
/query?filters=population>=40000&filters=rate=5.5&operator=or&sort=population,desc
```

Exemplos:

* Listar as cidades com população  maior ou igual a `40000` habitantes ou rate igual a 5.5,
 ordenado pelo número de habitantes de forma decrescente
```http request
/query?opt=population>=40000,rate=5.5&operator=or&sort=population,desc
```
* Listar as cidades com stateId igual a `52e0a6a7-d72d-4b0f-bab9-aebfcf888e21` e população maior que `20000` habitantes
```http request
/api/cities/query?opt=stateId=52e0a6a7-d72d-4b0f-bab9-aebfcf888e21,population>20000
```  
* Listar as cidades com população entre `40000` e `550000` habitantes
```http request
/api/cities/query?opt=population$btw40000;55000
```  
* Listar os estados que possuem cidades com população maior ou igual a `50000` habitantes
```http request
/api/state/query?opt=cities.population>=50000&distinct=true
```  
* Listar os estados que foram fundados no dia 14/04/20
```http request
/api/state/query?opt=dateOfFoundation=2020-04-14T22:42:53Z
/api/state/query?opt=dateOfFoundation=1586833200000
```  

Obs.: 
* As consultas suportam `org.springframework.data.domain.Pageable` (parâmetro page e size), com os valores default do Spring.
* As consultas suportam `org.springframework.data.domain.Sort`, com os valores default do Spring.
* O valor default do parâmetro `distinct` é true. Sendo assim, pode ser omitido.

##### Método **POST**

O Layout da consulta, segue a seguinte definição: 

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

Exemplos:

* Listar as cidades com população  maior ou igual a `40000` habitantes
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

* Listar os estados que possuem cidades com população maior ou igual a 50000 habitantes.

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

* Listar os estados que foram fundados antes de 14/04/20

```json
{
  "filters": [
    {
     "field": "dateOfFoundation",
     "operator": "<",
     "value": "2020-04-14T22:42:53Z"
    }
  ]
}
``` 

Obs.:
* O valor default do parâmetro `distinct` é true. Sendo assim, pode ser omitido.
* O tipo `Sort`, suporta `direction` com valores ASC e DESC.
* Todas as configurações de consulta, são realizadas com base no nome do atributo. Também é suportado consultas no
atributo filho (**ainda não suportado para o tipo Sort**).
* O parâmetro `operator`, possui valor default `and` e pode ser omitido.
* O parâmetro `value` do tipo `Filter`, suporta múltiplos tipos. Sendo assim, deve ser informado com sintaxe equivalente 
ao tipo definido na entidade. 

### Visualização (views)

Os `resources` de consulta suportam configuração dinâmica de visualização. Ou seja, permite minimizar o over-fetching comum em `API-REST`, uma vez que possibilita indicar quais informações devem ser retornadas no `json`.

Essa configuração dinâmica é realiza através do atributo `views`, do `QueryDefinition`. Exemplo:

```json
{
  "views": ["id", "name", "population"],
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
  ]
}
```
Da mesma forma que temos o suporte ao fetch dinâmico, que possibilita indicar quais relacionamentos devem ser retornados na consulta, também temos suporte a visualização dinâmica desses atributos. Exemplo:

**POST** - {path}/query, {path}/query/page

```json
{
  "views": ["id", "name", "population", "state.id", "state.name"],
  "joins": [
    {
     "field": "state",
     "fetch": true
    }
  ]
}
```

**GET** - {path}/{id}, {path}/detail/{id}, {path}/all, {path}/query, {path}/query/page 
```http request
/api/cities/dcacfe35-2aa7-4627-972e-1d42f6c8cc1f?views=id,name
```

```http request
/api/cities/detail/dcacfe35-2aa7-4627-972e-1d42f6c8cc1f?views=id,name,state.id,state.name
```

```http request
/api/cities/query?opt=population>=50000views=id,name
```  

```http request
/api/cities/query?opt=population>=50000&fetches=state&views=id,name,state.id,state.name
```

```http request
/api/cities/all?views=id,name
```

Obs.:
* A visualização dinâmica em relacionamentos é aplicada apenas em relacionamentos que foram definidos com `fetch true`. Sendo assim, o relacionamento deve estar configurado em algum filtro com `fetch true` ou indicado explicitamente na lista de `joins`.
* O parser do `json` é realizado nativamente com o `jackson`. Sendo assim, as visualizações respeitam as annotations `@JsonIgnore` e `@JsonIgnoreProperties`.
* Quando configurado com `query-with-views: entity`, os relacionamentos só serão carregados se estiverem com annotation `@DynamicView`.
* O processamento da visualização ocorre a nível do `resource`, no momento da serialização do `json`. Sendo assim, não existe uma otimização a nível das consultas de banco, onde essas são realizadas com base nos mapeamentos `JPA` e definições do `Spring Data`.
* O suporte a {path}/{id} e {path}/detail/{id} com método `GET`, não permite a configuração de `fetch`. Sendo assim, será aplicado diretamente ao resultado de `findById` e `findDetailById` do **Service** correspondente ou a definição de `NamedEntityGraphs` do `Entity`.  

#### Configuração

```yaml
spring-api-tools:
  query-with-views: all
``` 
* all - habilita para todas as classes de domínio (valor default).
* entity - Indica que estará habilitado apenas para os domínios que estiverem configurados com a annotation `@DynamicView`.
* none - desabilitado.
