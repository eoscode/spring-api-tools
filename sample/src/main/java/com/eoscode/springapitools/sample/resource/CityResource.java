package com.eoscode.springapitools.sample.resource;

import com.eoscode.springapitools.resource.AbstractRepositoryResource;
import com.eoscode.springapitools.sample.entity.City;
import com.eoscode.springapitools.sample.repository.CityRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityResource extends AbstractRepositoryResource<CityRepository, City, String> {

}
