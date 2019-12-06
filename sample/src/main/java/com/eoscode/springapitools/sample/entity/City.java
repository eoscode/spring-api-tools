package com.eoscode.springapitools.sample.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "city")
@NamedEntityGraphs(
        @NamedEntityGraph(name = "City.detail", attributeNodes = {
                @NamedAttributeNode("state")
        })
)
@NoArgsConstructor
public class City {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "uuid")
    private String id;

    @Column(name = "name", length = 60)
    private String name;

    @JsonIgnoreProperties({"cities"})
    @JoinColumn(name = "state_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private State state;

    @Column(name = "state_id", updatable = false, insertable = false)
    private String stateId;

    @Column(name = "population")
    private long population;

    public String getStateId() {
        if (state != null && state.getId() != null) {
            return state.getId();
        } else {
            return stateId;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }
    
}
