/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.command;

import java.util.Set;

import org.apache.poi.ss.util.CellReference;

/**
 * Common interface for all Spreadsheet commands that change cell values.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
public interface ValueChangeCommand extends Command {

    /**
     * Returns the cells that had their value(s) changed.
     */
    public Set<CellReference> getChangedCells();

}
