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
