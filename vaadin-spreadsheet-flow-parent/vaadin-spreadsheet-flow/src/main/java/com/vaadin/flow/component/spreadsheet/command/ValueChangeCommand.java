package com.vaadin.flow.component.spreadsheet.command;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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