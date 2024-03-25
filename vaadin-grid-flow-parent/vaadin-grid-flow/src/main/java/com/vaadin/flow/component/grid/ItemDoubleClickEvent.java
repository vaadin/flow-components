/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Event fired when a Grid item is double clicked.
 *
 * @param <T>
 *            the grid bean type
 *
 * @see ItemClickEvent
 * @see Grid#addItemDoubleClickListener(com.vaadin.flow.component.ComponentEventListener)
 *
 * @author Vaadin Ltd
 *
 */
@DomEvent("item-double-click")
public class ItemDoubleClickEvent<T> extends ItemClickEvent<T> {

    /**
     * Creates a new item double click event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param itemKey
     *            the item mapper key
     * @param internalColumnId
     *            the internal id of the column associated with the click event
     * @param screenX
     *            the x coordinate of the click event, relative to the upper
     *            left corner of the screen, -1 if unknown
     * @param screenY
     *            the y coordinate of the click event, relative to the upper
     *            left corner of the screen, -i if unknown
     * @param clientX
     *            the x coordinate of the click event, relative to the upper
     *            left corner of the browser viewport, -1 if unknown
     * @param clientY
     *            the y coordinate of the click event, relative to the upper
     *            left corner of the browser viewport, -1 if unknown
     * @param clickCount
     *            the number of consecutive clicks recently recorded
     * @param button
     *            the id of the pressed mouse button
     * @param ctrlKey
     *            <code>true</code> if the control key was down when the event
     *            was fired, <code>false</code> otherwise
     * @param shiftKey
     *            <code>true</code> if the shift key was down when the event was
     *            fired, <code>false</code> otherwise
     * @param altKey
     *            <code>true</code> if the alt key was down when the event was
     *            fired, <code>false</code> otherwise
     * @param metaKey
     *            <code>true</code> if the meta key was down when the event was
     *            fired, <code>false</code> otherwise
     *
     */
    public ItemDoubleClickEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.itemKey") String itemKey,
            @EventData("event.detail.internalColumnId") String internalColumnId,
            @EventData("event.detail.screenX") int screenX,
            @EventData("event.detail.screenY") int screenY,
            @EventData("event.detail.clientX") int clientX,
            @EventData("event.detail.clientY") int clientY,
            @EventData("event.detail.detail") int clickCount,
            @EventData("event.detail.button") int button,
            @EventData("event.detail.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.shiftKey") boolean shiftKey,
            @EventData("event.detail.altKey") boolean altKey,
            @EventData("event.detail.metaKey") boolean metaKey) {
        super(source, fromClient, itemKey, internalColumnId, screenX, screenY,
                clientX, clientY, clickCount, button, ctrlKey, shiftKey, altKey,
                metaKey);
    }

    /**
     * Creates a new item double click event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param itemKey
     *            the item mapper key
     * @param screenX
     *            the x coordinate of the click event, relative to the upper
     *            left corner of the screen, -1 if unknown
     * @param screenY
     *            the y coordinate of the click event, relative to the upper
     *            left corner of the screen, -i if unknown
     * @param clientX
     *            the x coordinate of the click event, relative to the upper
     *            left corner of the browser viewport, -1 if unknown
     * @param clientY
     *            the y coordinate of the click event, relative to the upper
     *            left corner of the browser viewport, -1 if unknown
     * @param clickCount
     *            the number of consecutive clicks recently recorded
     * @param button
     *            the id of the pressed mouse button
     * @param ctrlKey
     *            <code>true</code> if the control key was down when the event
     *            was fired, <code>false</code> otherwise
     * @param shiftKey
     *            <code>true</code> if the shift key was down when the event was
     *            fired, <code>false</code> otherwise
     * @param altKey
     *            <code>true</code> if the alt key was down when the event was
     *            fired, <code>false</code> otherwise
     * @param metaKey
     *            <code>true</code> if the meta key was down when the event was
     *            fired, <code>false</code> otherwise
     *
     * @deprecated Please use the constructor with an additional parameter
     *             {@code internalColumnId}.
     */
    @Deprecated
    public ItemDoubleClickEvent(Grid<T> source, boolean fromClient,
            String itemKey, int screenX, int screenY, int clientX, int clientY,
            int clickCount, int button, boolean ctrlKey, boolean shiftKey,
            boolean altKey, boolean metaKey) {
        super(source, fromClient, itemKey, screenX, screenY, clientX, clientY,
                clickCount, button, ctrlKey, shiftKey, altKey, metaKey);
    }

}
