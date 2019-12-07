package com.eoscode.springapitools.sample.service;

import com.eoscode.springapitools.sample.entity.State;
import com.eoscode.springapitools.sample.repository.StateRepository;
import com.eoscode.springapitools.service.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class StateService extends AbstractService<StateRepository, State, String> {}
