/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Allows setting a set of rules to apply for different screen or chart sizes.
 * Each rule specifies additional chart options.
 */
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
