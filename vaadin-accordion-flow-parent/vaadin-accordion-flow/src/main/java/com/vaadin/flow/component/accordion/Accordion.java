package com.vaadin.flow.component.accordion;

/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.shared.Registration;

import javax.annotation.Nullable;

/**
 * Accordion is a a vertically stacked set of expandable panels.
 * Only one panel can be expanded at a time.
 */
@Tag("vaadin-accordion")
@HtmlImport("frontend://bower_components/vaadin-accordion/src/vaadin-accordion.html")
public class Accordion extends Component implements HasSize {

    private static final String OPENED_PROPERTY = "opened";

    /**
     * Adds a panel created from the given title and content.
     *
     * @param summary the title of the panel
     * @param content the content of th panel
     * @return the panel created and added
     */
    public AccordionPanel add(String summary, Component content) {
        final AccordionPanel panel = new AccordionPanel(summary, content);
        return add(panel);
    }

    /**
     * Adds a panel.
     *
     * @param panel the panel to be added
     * @return the added panel
     */
    public AccordionPanel add(AccordionPanel panel) {
        getElement().appendChild(panel.getElement());
        return panel;
    }

    /**
     * Removes a panel.
     *
     * @param panel the panel to be removed
     */
    public void remove(AccordionPanel panel) {
        getElement().removeChild(panel.getElement());
    }

    /**
     * Collapses all the panels in this accordion.
     */
    public void collapse() {
        getElement().setProperty(OPENED_PROPERTY, null);
    }

    /**
     * Expands the panel at the specified index.
     *
     * @param index the position of the panel to be expanded.
     *              The first panel is at index zero.
     */
    public void expand(int index) {
        getElement().setProperty(OPENED_PROPERTY, index);
    }

    /**
     * Expands the specified panel.
     *
     * @param panel the panel to be expanded
     */
    public void expand(AccordionPanel panel) {
        expand(getElement().indexOfChild(panel.getElement()));
    }

    /**
     * Gets the index of the currently expanded index.
     *
     * @return the index of the expanded panel or null if the accordion is collapsed.
     */
    @Nullable
    public Integer getExpandedIndex() {
        final String opened = getElement().getProperty(OPENED_PROPERTY);
        return opened == null ? null : Integer.valueOf(opened);
    }

    /**
     * Registers a listener to be notified whenever a panel is expanded or collapsed.
     *
     * @param listener the listener to be notified
     * @return a handle to the registered listener which could also be used to unregister it.
     */
    public Registration addOpenedChangedListener(
            ComponentEventListener<AccordionOpenedChangedEvent> listener) {

        return ComponentUtil.addListener(this, AccordionOpenedChangedEvent.class, listener);
    }
}
