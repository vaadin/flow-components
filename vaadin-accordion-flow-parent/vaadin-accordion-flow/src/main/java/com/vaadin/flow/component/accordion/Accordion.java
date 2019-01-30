package com.vaadin.flow.component.accordion;

/*
 * #%L
 * Vaadin Accordion
 * %%
 * Copyright (C) 2018 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.shared.Registration;

@Tag("vaadin-accordion")
@HtmlImport("frontend://bower_components/vaadin-accordion/src/vaadin-accordion.html")
public class Accordion extends Component implements HasSize {

    private static final String OPENED_PROPERTY = "opened";

    public AccordionPanel add(String summary, Component detail) {
        final AccordionPanel panel = new AccordionPanel(summary, detail);
        return add(panel);
    }

    public AccordionPanel add(AccordionPanel panel) {
        getElement().appendChild(panel.getElement());
        return panel;
    }

    public void remove(AccordionPanel panel) {
        getElement().removeChild(panel.getElement());
    }

    public void collapse() {
        getElement().setProperty(OPENED_PROPERTY, null);
    }

    public void expand(int index) {
        getElement().setProperty(OPENED_PROPERTY, index);
    }

    public void expand(AccordionPanel panel) {
        expand(getElement().indexOfChild(panel.getElement()));
    }

    public Integer getExpandedIndex() {
        final String opened = getElement().getProperty(OPENED_PROPERTY);
        return opened == null ? null : Integer.valueOf(opened);
    }

    public Registration addOpenedChangedListener(
            ComponentEventListener<AccordionOpenedChangedEvent> listener) {

        return ComponentUtil.addListener(this, AccordionOpenedChangedEvent.class, listener);
    }
}
