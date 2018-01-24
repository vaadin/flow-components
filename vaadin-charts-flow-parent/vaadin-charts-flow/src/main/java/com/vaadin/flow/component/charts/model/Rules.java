package com.vaadin.flow.component.charts.model;
/**
 * A set of rules for responsive settings. The rules are executed from the top
 * down.
 */
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