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
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */


import javax.annotation.Generated;

/**
 * A set of rules for responsive settings. The rules are executed from the top
 * down.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Rules extends AbstractConfigurationObject {

	private Condition condition;

	public Rules() {
	}

	/**
	 * @see #setCondition(Condition)
	 */
	public Condition getCondition() {
		if (condition == null) {
			condition = new Condition();
		}
		return condition;
	}

	/**
	 * Under which conditions the rule applies.
	 */
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
}
