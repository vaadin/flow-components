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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.util.Optional;

@DomEvent("opened-changed")
public class AccordionOpenedChanged extends ComponentEvent<Accordion> {

    private final Integer index;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public AccordionOpenedChanged(Accordion source, boolean fromClient,
                                  @EventData("event.detail.value") Integer index) {
        super(source, fromClient);
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    /**
     * Caution should be exercised when using this method with an accordion which along with its panels
     * are created in a template. Such template children would by default not be children of the
     * Accordion Flow component thus making it possible for this method to return the wrong panel in such cases.
     * @return
     */
    public Optional<AccordionPanel> getOpenedPanel() {
        return index == null || index >= getSource().getChildren().count() ? Optional.empty() :
                getSource().getElement().getChild(index).getComponent().map(AccordionPanel.class::cast);
    }
}
