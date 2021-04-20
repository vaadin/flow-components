/*
 * Copyright 2000-2021 Vaadin Ltd.
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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.util.Optional;

/**
 * Event fired when a cell in the Grid is focused.
 *
 * @param <T> the grid bean type
 * @author Vaadin Ltd
 * <p>
 * @see Grid#addCellFocusListener(com.vaadin.flow.component.ComponentEventListener)
 */
@DomEvent("grid-cell-focus")
public class CellFocusEvent<T> extends ComponentEvent<Grid<T>> {

    private static final String SECTION_DETAILS = "body";
    private static final String SECTION_HEADER = "header";
    private static final String SECTION_FOOTER = "footer";

    private final T item;
    private final Grid.Column<T> column;

    private final boolean detailsCell;
    private final boolean headerCell;
    private final boolean footerCell;

    /**
     * Creates a new cell focus event.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public CellFocusEvent(Grid<T> source, boolean fromClient,
                          @EventData("event.detail.itemKey") String itemKey,
                          @EventData("event.detail.internalColumnId") String internalColumnId,
                          @EventData("event.detail.section") String section) {
        super(source, fromClient);

        item = source.getDataCommunicator().getKeyMapper().get(itemKey);
        column = source.getColumnByInternalId(internalColumnId);

        detailsCell = SECTION_DETAILS.equals(section);
        headerCell = SECTION_HEADER.equals(section);
        footerCell = SECTION_FOOTER.equals(section);
    }

    /**
     * Indicates, if the clicked cell is part of the table's details / body section.
     * @return is a details cell
     */
    public boolean isDetailsCell() {
        return detailsCell;
    }

    /**
     * Indicates, if the clicked cell is part of the table's header section.
     * @return is a header cell
     */
    public boolean isHeaderCell() {
        return headerCell;
    }

    /**
     * Indicates, if the clicked cell is part of the table's footer section.
     * @return is a footer cell
     */
    public boolean isFooterCell() {
        return footerCell;
    }

    /**
     * Returns the item represented by the focused cell. Is empty, when the focused cell is not a details cell.
     * @return item or empty
     */
    public Optional<T> getItem() {
        return Optional.ofNullable(item);
    }

    /**
     * Returns the column represented by the focused cell. Is empty, when the focused cell is a header group (a
     * cell with a cellspan > 1).
     * @return column or empty
     */
    public Optional<Grid.Column<T>> getColumn() {
        return Optional.ofNullable(column);
    }
}
