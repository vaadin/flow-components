/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.dnd;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.grid.Grid;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Drop event that occurs on the {@link Grid} or its rows.
 *
 * @param <T>
 *            The Grid bean type.
 * @author Vaadin Ltd.
 * @see Grid#addDropListener(GridDropListener)
 */
@SuppressWarnings("serial")
@DomEvent("grid-drop")
public class GridDropEvent<T> extends ComponentEvent<Grid<T>> {

    private final T dropTargetItem;
    private final GridDropLocation dropLocation;
    private final Map<String, String> data;

    /**
     * Creates a grid drop event.
     *
     * @param source
     *            Component that was dragged.
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param item
     *            The item on which the drop occurred, from
     *            {@code detail.dropTargetItem}.
     * @param dropLocation
     *            Drop location from {@code detail.dropLocation}.
     * @param dragData
     *            Drag data from {@code detail.dragData}.
     */
    public GridDropEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.dropTargetItem") JsonObject item,
            @EventData("event.detail.dropLocation") String dropLocation,
            @EventData("event.detail.dragData") JsonArray dragData) {
        super(source, fromClient);

        data = new HashMap<>();
        IntStream.range(0, dragData.length()).forEach(i -> {
            JsonObject jsonData = dragData.getObject(i);
            data.put(jsonData.getString("type"), jsonData.getString("data"));
        });

        if (item != null) {
            this.dropTargetItem = source.getDataCommunicator().getKeyMapper()
                    .get(item.getString("key"));
        } else {
            this.dropTargetItem = null;
        }

        this.dropLocation = Arrays.asList(GridDropLocation.values()).stream()
                .filter(dl -> dl.getClientName().equals(dropLocation))
                .findFirst().get();
    }

    /**
     * Get the row the drop happened on.
     * <p>
     * If the drop was not on top of a row (see {@link #getDropLocation()}) or
     * {@link GridDropMode#ON_GRID} is used, then returns an empty optional.
     *
     * @return The item of the row the drop happened on, or an empty optional if
     *         drop was not on a row
     */
    public Optional<T> getDropTargetItem() {
        return Optional.ofNullable(dropTargetItem);
    }

    /**
     * Get the location of the drop within the row.
     * <p>
     * <em>NOTE: the location will be {@link GridDropLocation#EMPTY} if:
     * <ul>
     * <li>dropped on an empty grid</li>
     * <li>dropping on rows was not possible because of
     * {@link GridDropMode#ON_GRID } was used</li>
     * <li>{@link GridDropMode#ON_TOP} is used and the drop happened on empty
     * space after last row or on top of the header / footer</li>
     * </ul>
     * </em>
     *
     * @return location of the drop in relative to the
     *         {@link #getDropTargetItem()} or {@link GridDropLocation#EMPTY} if
     *         no target row present
     * @see Grid#setDropMode(GridDropMode)
     */
    public GridDropLocation getDropLocation() {
        return dropLocation;
    }

    /**
     * Get data from the {@code DataTransfer} object.
     *
     * @param type
     *            Data format, e.g. {@code text/plain} or {@code text/uri-list}.
     * @return Optional data for the given format if exists in the {@code
     * DataTransfer}, otherwise {@code Optional.empty()}.
     */
    public Optional<String> getDataTransferData(String type) {
        return Optional.ofNullable(data.get(type));
    }

    /**
     * Get data of any of the types {@code "text"}, {@code "Text"} or {@code
     * "text/plain"}.
     * <p>
     * IE 11 transfers data dropped from the desktop as {@code "Text"} while
     * most other browsers transfer textual data as {@code "text/plain"}.
     *
     * @return First existing data of types in order {@code "text"}, {@code
     * "Text"} or {@code "text/plain"}, or {@code null} if none of them exist.
     */
    public String getDataTransferText() {
        // Read data type "text"
        String text = data.get("text");

        // IE stores data dragged from the desktop as "Text"
        if (text == null) {
            text = data.get("Text");
        }

        // Browsers may store the key as "text/plain"
        if (text == null) {
            text = data.get("text/plain");
        }

        return text;
    }

    /**
     * Get all of the transfer data from the {@code DataTransfer} object. The
     * data can be iterated to find the most relevant data as it preserves the
     * order in which the data was set to the drag source element.
     *
     * @return Map of type/data pairs, containing all the data from the {@code
     * DataTransfer} object.
     */
    public Map<String, String> getDataTransferData() {
        return Collections.unmodifiableMap(data);
    }

}
