package com.eoscode.springapitools.sample.infrastructure.persistence.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;


import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "city")
@NamedEntityGraphs(
        @NamedEntityGraph(name = "City.findDetailById", attributeNodes = {
                @NamedAttributeNode("state")
        })
)
@Getter
@Setter
@NoArgsConstructor
public class CityEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    private UUID id;

    @Column(name = "name", length = 60)
    private String name;

    @JsonIgnoreProperties({"cities"})
    @JoinColumn(name = "state_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private StateEntity state;

    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "population")
    private long population;

    @Column(name = "rate")
    private double rate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StateEntity getState() {
        return state;
    }

    public void setState(StateEntity state) {
        this.state = state;
    }

    public UUID getStateId() {
        return stateId;
    }

    public void setStateId(UUID stateId) {
        this.stateId = stateId;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
