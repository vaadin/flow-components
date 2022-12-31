/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
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
