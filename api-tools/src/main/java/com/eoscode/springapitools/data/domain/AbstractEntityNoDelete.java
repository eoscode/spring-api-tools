package com.eoscode.springapitools.data.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@NoDelete
public abstract class AbstractEntityNoDelete<ID> extends AbstractEntity<ID> {

	private static final long serialVersionUID = 1L;

	@Column(name="STATUS", length=1)
	//@ColumnDefault("1")
	private Integer status;
	
	public void setStatus(Integer status) {
		this.status = status;				
	}
	
	public Integer getStatus() {
		return status;
	}
	
}
