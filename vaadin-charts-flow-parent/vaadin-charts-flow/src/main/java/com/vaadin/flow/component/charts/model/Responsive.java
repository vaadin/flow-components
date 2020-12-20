package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */


import javax.annotation.Generated;

/**
 * Allows setting a set of rules to apply for different screen or chart sizes.
 * Each rule specifies additional chart options.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Responsive extends AbstractConfigurationObject {

	private Rules rules;

	public Responsive() {
	}

	/**
	 * @see #setRules(Rules)
	 */
	public Rules getRules() {
		if (rules == null) {
			rules = new Rules();
		}
		return rules;
	}

	/**
	 * A set of rules for responsive settings. The rules are executed from the
	 * top down.
	 */
	public void setRules(Rules rules) {
		this.rules = rules;
	}
}
