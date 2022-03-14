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
package com.vaadin.flow.component.grid;

import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Event fired when a cell in the Grid is focused.
 *
 * @param <T>
 *            the grid bean type
 * @author Vaadin Ltd
 *         <p>
 * @see Grid#addCellFocusListener(com.vaadin.flow.component.ComponentEventListener)
 */
@DomEvent("grid-cell-focus")
public class CellFocusEvent<T> extends ComponentEvent<Grid<T>> {

    private final transient T item;
    private final Grid.Column<T> column;
    private final GridSection section;

    /**
     * Creates a new cell focus event.
     *
     * @param source
     *            the source component
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     */
    public CellFocusEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.itemKey") String itemKey,
            @EventData("event.detail.internalColumnId") String internalColumnId,
            @EventData("event.detail.section") String sectionName) {
        super(source, fromClient);

        item = source.getDataCommunicator().getKeyMapper().get(itemKey);
        column = source.getColumnByInternalId(internalColumnId);
        section = GridSection.ofClientSideName(sectionName);
    }

    /**
     * Indicates, if the clicked cell is part of the grid's body section.
     *
     * @return is a body cell
     */
    public boolean isBodyCell() {
        return section == GridSection.BODY;
    }

    /**
     * Indicates, if the clicked cell is part of the grid's header section.
     *
     * @return is a header cell
     */
    public boolean isHeaderCell() {
        return section == GridSection.HEADER;
    }

    /**
     * Indicates, if the clicked cell is part of the grid's footer section.
     *
     * @return is a footer cell
     */
    public boolean isFooterCell() {
        return section == GridSection.FOOTER;
    }

    /**
     * Returns the grid section, where this cell is located. Never null.
     *
     * @return section
     */
    public GridSection getSection() {
        return section;
    }

    /**
     * Returns the item represented by the focused cell. Is empty, when the
     * focused cell is not a body cell.
     *
     * @return item or empty
     */
    public Optional<T> getItem() {
        return Optional.ofNullable(item);
    }

    /**
     * Returns the column represented by the focused cell. Is empty, when the
     * focused cell is a header group (a cell with a cellspan > 1).
     *
     * @return column or empty
     */
    public Optional<Grid.Column<T>> getColumn() {
        return Optional.ofNullable(column);
    }

    /**
     * An enum representing the different sections of a grid.
     */
    public enum GridSection {
        /**
         * Header section.
         */
        HEADER("header"),

        /**
         * Body section.
         */
        BODY("body"),

        /**
         * Footer section.
         */
        FOOTER("footer");

        private final String clientSideName;

        GridSection(String clientSideName) {
            this.clientSideName = clientSideName;
        }

        /**
         * Returns the matching {@link GridSection} for the given client side
         * name. An unknown client side name will lead to an exception.
         *
         * @param clientSideName
         *            client side name to lookup
         * @throws IllegalArgumentException
         *             on an unknown client side section name
         * @return matching section instance
         */
        public static GridSection ofClientSideName(String clientSideName) {
            for (GridSection section : values()) {
                if (Objects.equals(clientSideName,
                        section.getClientSideName())) {
                    return section;
                }
            }

            throw new IllegalArgumentException(
                    "Unknown section client side section name: "
                            + clientSideName);
        }

        /**
         * Returns the client side name of the section.
         *
         * @return client side name
         */
        public String getClientSideName() {
            return clientSideName;
        }
    }
}
