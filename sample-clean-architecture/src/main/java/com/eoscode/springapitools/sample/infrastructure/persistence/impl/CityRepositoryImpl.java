package com.eoscode.springapitools.sample.infrastructure.persistence.impl;

import com.eoscode.springapitools.sample.core.city.City;
import com.eoscode.springapitools.sample.core.city.ports.CityRepository;

import java.util.List;

public class CityRepositoryImpl implements CityRepository {

    public final com.eoscode.springapitools.sample.infrastructure.persistence.repositories.CityRepository cityRepository;

    public CityRepositoryImpl(com.eoscode.springapitools.sample.infrastructure.persistence.repositories.CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public City findById(String id) {
        return null;
    }

    @Override
    public City save(City state) {
        return null;
    }

    @Override
    public City update(City state) {
        return null;
    }

    @Override
    public void delete(City state) {

    }

    @Override
    public List<City> getAll() {
        return null;
    }

}
