package com.eoscode.springapitools.sample.infrastructure.persistence.impl;

import com.eoscode.springapitools.sample.core.domain.model.City;
import com.eoscode.springapitools.sample.core.domain.repositories.ICityRepository;
import com.eoscode.springapitools.sample.infrastructure.persistence.converters.CityRepositoryConverter;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.CityEntity;
import com.eoscode.springapitools.sample.infrastructure.persistence.repositories.CityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CityRepositoryImpl implements ICityRepository {

    public final CityRepository cityRepository;

    private final CityRepositoryConverter cityRepositoryConverter;

    public CityRepositoryImpl(CityRepository cityRepository, CityRepositoryConverter cityRepositoryConverter) {
        this.cityRepository = cityRepository;
        this.cityRepositoryConverter = cityRepositoryConverter;
    }

    @Override
    public City findById(String id) {
        Optional<CityEntity> cityEntity = cityRepository.findById(UUID.fromString(id));
        return cityEntity.map(cityRepositoryConverter::mapToEntity).orElse(null);
    }

    @Override
    public City save(City city) {
        CityEntity cityEntity = cityRepository.save(cityRepositoryConverter.mapToTable(city));
        return cityRepositoryConverter.mapToEntity(cityEntity);
    }

    @Override
    public City update(City city) {
        CityEntity cityEntity = cityRepository.save(cityRepositoryConverter.mapToTable(city));
        return cityRepositoryConverter.mapToEntity(cityEntity);
    }

    @Override
    public void delete(City city) {
        cityRepository.delete(cityRepositoryConverter.mapToTable(city));
    }

    @Override
    public List<City> getAll() {
        return cityRepository.findAll()
                .stream()
                .map(cityRepositoryConverter::mapToEntity)
                .collect(Collectors.toList());
    }

}
