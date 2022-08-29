package com.eoscode.springapitools.sample.core.domain.model;

import com.eoscode.springapitools.sample.core.domain.shared.SelfValidating;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class City extends SelfValidating<City> implements Serializable {

    private String id;

    private String name;

    private State state;

    private String stateId;

    private long population;

    private double rate;

}
