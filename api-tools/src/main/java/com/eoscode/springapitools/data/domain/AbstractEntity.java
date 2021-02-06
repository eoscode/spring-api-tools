package com.eoscode.springapitools.data.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity<ID> extends BaseEntity implements Serializable, Identifier<ID> {

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
