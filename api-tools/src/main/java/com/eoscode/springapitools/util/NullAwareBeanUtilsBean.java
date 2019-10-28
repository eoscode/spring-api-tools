package com.eoscode.springapitools.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;

public class NullAwareBeanUtilsBean extends BeanUtilsBean {
	
	private static NullAwareBeanUtilsBean instance;

	private NullAwareBeanUtilsBean() {

	}
	
	public static NullAwareBeanUtilsBean getInstance() {
		if (instance == null) {
			instance = new NullAwareBeanUtilsBean();
		}
		return instance;
	}
	
    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (value == null) {
            return;
        }
        super.copyProperty(dest, name, value);
    }
}
