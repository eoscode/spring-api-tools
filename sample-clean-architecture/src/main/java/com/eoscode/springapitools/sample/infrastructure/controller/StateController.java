package com.eoscode.springapitools.sample.infrastructure.controller;

import com.eoscode.springapitools.resource.AbstractRepositoryResource;
import com.eoscode.springapitools.sample.core.application.state.usecases.GetAllStateUserCase;
import com.eoscode.springapitools.sample.core.application.state.usecases.GetStateUserCase;
import com.eoscode.springapitools.sample.core.domain.model.State;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.StateEntity;
import com.eoscode.springapitools.sample.infrastructure.persistence.repositories.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateController extends AbstractRepositoryResource<StateRepository, StateEntity, UUID> {

    private final GetStateUserCase getStateUserCase;
    private final GetAllStateUserCase getAllStateUserCase;


   /* @GetMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<State> find(@PathVariable String id,
                      @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) throws StateAlreadyExistException {

        return ResponseEntity.ok(getStateUserCase.execute(id));
    }*/

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<State>> findAll() {
        return ResponseEntity.ok(getAllStateUserCase.execute());
    }

}
