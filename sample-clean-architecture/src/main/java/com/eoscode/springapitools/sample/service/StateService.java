package com.eoscode.springapitools.sample.service;

import com.eoscode.springapitools.sample.infrastructure.persistence.entities.StateEntity;
import com.eoscode.springapitools.sample.infrastructure.persistence.repositories.StateRepository;
import com.eoscode.springapitools.service.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class StateService extends AbstractService<StateRepository, StateEntity, String> {}
