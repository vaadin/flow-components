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

/**
 * An event fired when an AccordionPanel is expanded or collapsed.
 */
@DomEvent("opened-changed")
public class AccordionPanelOpenedChangedEvent extends ComponentEvent<AccordionPanel> {

    private final boolean opened;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param opened true if the panel was opened, otherwise false
     */
    public AccordionPanelOpenedChangedEvent(AccordionPanel source, boolean fromClient,
                                            @EventData("event.detail.value") boolean opened) {
        super(source, fromClient);
        this.opened = opened;
    }

    /**
     * Gets whether the panel was expanded or collapsed.
     *
     * @return true if the panel was expanded, otherwise false
     */
    public boolean isOpened() {
        return opened;
    }
}
