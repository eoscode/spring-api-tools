package com.eoscode.springapitools.sample.core.domain.shared;

import java.util.List;

public interface CrudRepository<Domain, ID> {

    Domain findById(ID id);
    Domain save(Domain domain);
    Domain update(Domain domain);
    void delete(Domain domain);
    List<Domain> getAll();

}
