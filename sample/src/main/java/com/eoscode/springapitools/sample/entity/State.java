package com.eoscode.springapitools.sample.entity;

import com.eoscode.springapitools.data.domain.Identifier;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
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

    @Column(name = "UF", length = 2)
    private String uf;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_FOUNDATION")
    private Date dateOfFoundation;

    public State() {}

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Date getDateOfFoundation() {
        return dateOfFoundation;
    }

    public void setDateOfFoundation(Date dateOfFoundation) {
        this.dateOfFoundation = dateOfFoundation;
    }
}
