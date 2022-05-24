package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
