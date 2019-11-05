package com.eoscode.springapitools.sample.resource;

import com.eoscode.springapitools.resource.AbstractResource;
import com.eoscode.springapitools.sample.entity.State;
import com.eoscode.springapitools.sample.service.StateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/state")
public class StateResource extends AbstractResource<StateService, State, String> {

    private final StateService stateService;

    public StateResource(StateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public StateService getService() {
        return stateService;
    }
}
