package com.eoscode.springapitools.sample.repository;


import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.entity.City;

@org.springframework.stereotype.Repository
public interface CityRepository extends Repository<City, String> { }
