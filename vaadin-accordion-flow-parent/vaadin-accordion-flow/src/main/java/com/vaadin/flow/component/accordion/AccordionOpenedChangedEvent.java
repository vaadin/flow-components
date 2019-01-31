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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * An event fired when an Accordion is expanded or collapsed.
 */
@DomEvent("opened-changed")
public class AccordionOpenedChangedEvent extends ComponentEvent<Accordion> {

    private final Integer index;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param index the index of the expanded panel or null if the accordion is collapsed
     */
    public AccordionOpenedChangedEvent(Accordion source, boolean fromClient,
                                       @EventData("event.detail.value") Integer index) {
        super(source, fromClient);
        this.index = index;
    }

    /**
     * Gets the index of the expanded panel or null if the accordion is collapsed.
     *
     * @return the index of the expanded panel or null if collapsed
     */
    @Nullable
    public Integer getIndex() {
        return index;
    }

    /**
     * Gets the expanded panel.
     *
     * Caution should be exercised when using this method with an Accordion which along with its panels
     * were created in a template. Such template children would by default not be children of the
     * Accordion Flow component, thus making it possible for this method to return the wrong panel in such cases.
     *
     * @return the expanded panel.
     */
    public Optional<AccordionPanel> getOpenedPanel() {
        return index == null || index >= getSource().getChildren().count() ? Optional.empty() :
                getSource().getElement().getChild(index).getComponent().map(AccordionPanel.class::cast);
    }
}
