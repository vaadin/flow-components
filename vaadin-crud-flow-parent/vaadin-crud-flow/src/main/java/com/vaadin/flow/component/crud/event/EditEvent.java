package com.vaadin.flow.component.crud.event;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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
import com.vaadin.flow.component.crud.Crud;
import elemental.json.JsonObject;

@DomEvent("crud-edit")
public class EditEvent<E> extends ComponentEvent<Crud<E>> {

    private E item;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public EditEvent(Crud<E> source, boolean fromClient,
                     @EventData("event.detail.item") JsonObject item) {
        super(source, fromClient);
        try {
            this.item = source.getGrid().getDataCommunicator()
                    .getKeyMapper().get(item.getString("key"));
        } catch (NullPointerException ex) {
            // TODO(oluwasayo): Remove when WC no longer fires edit event on grid active item change
        }
    }

    public E getItem() {
        return item;
    }
}
