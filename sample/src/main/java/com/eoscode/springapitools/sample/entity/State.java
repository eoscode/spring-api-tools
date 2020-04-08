package com.eoscode.springapitools.sample.entity;

import com.eoscode.springapitools.data.domain.Identifier;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "state")
/*@NamedEntityGraphs(
        @NamedEntityGraph(name = "State.detail", attributeNodes = {
                @NamedAttributeNode("cities")
        })
)*/
@Getter
@Setter
public class State implements Identifier<String> {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "uuid")
    private String id;

    @NotNull
    @Column(name = "name", length = 60)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "state")
    private List<City> cities;

}
