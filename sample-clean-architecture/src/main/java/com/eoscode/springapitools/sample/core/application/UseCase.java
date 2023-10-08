package com.eoscode.springapitools.sample.core.application;

public abstract class UseCase<IN, OUT> {

	public abstract OUT execute(IN anIn);

}
