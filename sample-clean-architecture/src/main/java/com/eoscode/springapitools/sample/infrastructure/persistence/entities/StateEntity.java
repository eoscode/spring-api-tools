package com.eoscode.springapitools.sample.infrastructure.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "state")
/*@NamedEntityGraphs(
        @NamedEntityGraph(name = "State.findById", attributeNodes = {
                @NamedAttributeNode("cities")
        })
)*/
@Data
@AllArgsConstructor
public class StateEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    private UUID id;

    @NotNull
    @Column(name = "name", length = 60)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "state")
    private List<CityEntity> cities;

    @Column(name = "UF", length = 2)
    private String uf;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_FOUNDATION")
    private Date dateOfFoundation;

    @Column(name = "AVERAGE_SALARY")
    private BigDecimal averageSalary;

    public StateEntity() {}

}
