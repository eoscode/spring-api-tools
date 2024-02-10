package com.eoscode.springapitools.sample.core.domain.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class State implements Serializable {

    private String id;

    @NotEmpty
    private String name;

    @Min(2)
    @Max(2)
    @NotEmpty
    private String uf;

    private Date dateOfFoundation;

    private BigDecimal averageSalary;

}
