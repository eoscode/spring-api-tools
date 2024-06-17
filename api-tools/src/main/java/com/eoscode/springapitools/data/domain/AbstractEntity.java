package com.eoscode.springapitools.data.domain;

import jakarta.persistence.MappedSuperclass;

import java.io.Serial;
import java.io.Serializable;

@MappedSuperclass
public abstract class AbstractEntity<ID> extends BaseEntity implements Serializable, Identifier<ID> {

	@Serial
	private static final long serialVersionUID = 1L;

	private ID id;

	@Override
	public ID getId() {
		return id;
	}

	@Override
	public void setId(ID id) {
		this.id = id;
	}

}
