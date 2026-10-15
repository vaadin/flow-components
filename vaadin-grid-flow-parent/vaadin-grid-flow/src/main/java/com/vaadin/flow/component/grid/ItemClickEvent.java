/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Event fired when a Grid item is clicked.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid#addItemClickListener(com.vaadin.flow.component.ComponentEventListener)
 *
 */
@DomEvent("item-click")
public class ItemClickEvent<T> extends ClickEvent<Grid<T>> {

    private final T item;

    private final Grid.Column<T> column;

    /**
     * Creates a new item click event.
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
    public ItemClickEvent(Grid<T> source, boolean fromClient,
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
        super(source, fromClient, screenX, screenY, clientX, clientY,
                clickCount, button, ctrlKey, shiftKey, altKey, metaKey);
        item = source.getDataCommunicator().getKeyMapper().get(itemKey);
        column = source.getColumnByInternalId(internalColumnId);
    }

    /**
     * Gets the clicked item.
     *
     * @return the clicked item
     */
    public T getItem() {
        return item;
    }

    /**
     * Gets the column that was clicked.
     *
     * @return the clicked column or {@code null} if it was the multi-selection
     *         column.
     */
    public Grid.Column<T> getColumn() {
        return column;
    }

}
