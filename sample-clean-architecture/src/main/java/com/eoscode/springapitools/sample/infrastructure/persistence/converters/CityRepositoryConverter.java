package com.eoscode.springapitools.sample.infrastructure.persistence.converters;

import com.eoscode.springapitools.sample.core.domain.model.City;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.CityEntity;
import com.eoscode.springapitools.sample.infrastructure.shared.RepositoryConverter;

import java.util.UUID;

public class CityRepositoryConverter implements RepositoryConverter<CityEntity, City> {

    @Override
    public CityEntity mapToTable(City persistenceObject) {
        CityEntity cityEntity = new CityEntity();

        cityEntity.setId(UUID.fromString(persistenceObject.getId()));
        cityEntity.setName(persistenceObject.getName());
        cityEntity.setPopulation(persistenceObject.getPopulation());
        cityEntity.setRate(persistenceObject.getRate());

        cityEntity.setStateId(UUID.fromString(persistenceObject.getStateId()));

        return cityEntity;
    }

    @Override
    public City mapToEntity(CityEntity tableObject) {
        return new City(tableObject.getId().toString(),
                tableObject.getName(),
               null,
                tableObject.getStateId().toString(),
                tableObject.getPopulation(),
                tableObject.getRate());
    }
}
