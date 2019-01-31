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
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.shared.Registration;

/**
 * An accordion panel which could be expanded or collapsed.
 */
@Tag("vaadin-accordion-panel")
public class AccordionPanel extends Details implements HasEnabled {

    /**
     * Creates an empty panel.
     */
    public AccordionPanel() {
    }

    /**
     * Creates a panel with the provided summary text and content.
     *
     * @param summary the summary
     * @param content the content
     */
    public AccordionPanel(String summary, Component content) {
        super(summary, content);
    }

    /**
     * Creates a panel with the provided summary component and content.
     *
     * @param summary the summary
     * @param content the content
     */
    public AccordionPanel(Component summary, Component content) {
        super(summary, content);
    }

    /**
     * Registers a listener to be notified when the panel is expanded or collapsed.
     *
     * @param listener the listener to be notified
     * @return a handle to the registered listener which could also be used to unregister it
     */
    public Registration addOpenedChangedListener(
            ComponentEventListener<AccordionPanelOpenedChangedEvent> listener) {

        return ComponentUtil.addListener(this, AccordionPanelOpenedChangedEvent.class, listener);
    }
}
