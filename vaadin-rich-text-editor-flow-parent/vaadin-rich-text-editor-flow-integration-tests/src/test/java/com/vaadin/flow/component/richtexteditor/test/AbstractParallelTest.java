/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.test;

public abstract class AbstractParallelTest
        extends com.vaadin.tests.AbstractParallelTest {

    protected String getLastValue() {
        return $("div").id("valuePanel").getText();
    }

    protected String getLastHtmlValue() {
        return $("div").id("htmlValuePanel").getText();
    }

    protected String getLastI18nValue() {
        return $("div").id("i18nPanel").getText();
    }

    protected String getLastBinderInfoValue() {
        return $("div").id("binder-info").getText();
    }

    protected String getLastHtmlBinderInfoValue() {
        return $("div").id("html-binder-info").getText();
    }

    protected String getLastRteBinderValue() {
        return $("div").id("binder-value-panel").getText();
    }

    protected String getLastRteHtmlBinderValue() {
        return $("div").id("html-binder-value-panel").getText();
    }

    protected String getLastRteTemplateValue() {
        return $("div").id("template-value-panel").getText();
    }
}
