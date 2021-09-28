package com.eoscode.springapitools.sample.core.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
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
