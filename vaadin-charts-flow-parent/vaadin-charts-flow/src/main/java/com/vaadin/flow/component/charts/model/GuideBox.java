/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Style options for the guide box. The guide box has one state by default, the
 * default state.
 */
@SuppressWarnings("unused")
public class GuideBox extends AbstractConfigurationObject {

    @JsonProperty("default")
    private GuideBoxDefaultState defaultState;

    /**
     * @see #setDefaultState(GuideBoxDefaultState)
     */
    public GuideBoxDefaultState getDefaultState() {
        if (defaultState == null) {
            defaultState = new GuideBoxDefaultState();
        }
        return defaultState;
    }

    /**
     * Style options for the guide box default state.
     * 
     * @param defaultState
     */
    public void setDefaultState(GuideBoxDefaultState defaultState) {
        this.defaultState = defaultState;
    }
}
