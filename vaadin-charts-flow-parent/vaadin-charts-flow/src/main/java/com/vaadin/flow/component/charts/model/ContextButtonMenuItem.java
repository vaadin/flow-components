/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

public class ContextButtonMenuItem extends AbstractConfigurationObject {

    private String text;
    private String _fn_onclick;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOnclick() {
        return _fn_onclick;
    }

    public void setOnclick(String _fn_onclick) {
        this._fn_onclick = _fn_onclick;
    }
}
