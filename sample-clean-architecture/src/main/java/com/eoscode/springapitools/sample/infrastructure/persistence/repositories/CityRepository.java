package com.eoscode.springapitools.sample.infrastructure.persistence.repositories;


import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.CityEntity;

@org.springframework.stereotype.Repository
public interface CityRepository extends Repository<CityEntity, String> { }
