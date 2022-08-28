package com.eoscode.springapitools.sample.infrastructure.controller;

import com.eoscode.springapitools.sample.core.domain.model.State;
import com.eoscode.springapitools.sample.core.application.state.exception.StateAlreadyExistException;
import com.eoscode.springapitools.sample.core.application.state.usecases.GetAllStateUserCase;
import com.eoscode.springapitools.sample.core.application.state.usecases.GetStateUserCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateController /*extends AbstractRepositoryResource<StateRepository, StateEntity, String>*/ {

    private final GetStateUserCase getStateUserCase;
    private final GetAllStateUserCase getAllStateUserCase;

    @GetMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<State> find(@PathVariable String id,
                      @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) throws StateAlreadyExistException {

        return ResponseEntity.ok(getStateUserCase.execute(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<State>> findAll() {
        return ResponseEntity.ok(getAllStateUserCase.execute());
    }

}
