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

import java.io.Serializable;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Common interface for all Spreadsheet commands.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
public interface Command extends Serializable {
    /**
     * Executes this command.
     */
    public void execute();

    /**
     * Returns the index of the currently active sheet.
     *
     * @return index of active sheet
     */
    public int getActiveSheetIndex();

    /**
     * Sets the sheet at the given index the currently active sheet.
     *
     * @param index
     *            Index of sheet to set active
     */
    public void setActiveSheetIndex(int index);

    /**
     * The selected cell that should be set when this command is run. In case
     * this command shouldn't change the selected cell, <code>null</code> is
     * returned.
     *
     * @return the selection or <code>null</code>
     */
    public CellReference getSelectedCellReference();

    /**
     * The painted range that should be set when this command is run. In case
     * his command shouldn't set a painted range, <code>null</code> is returned.
     *
     * @return the painted range or <code>null</code>
     */
    public CellRangeAddress getPaintedCellRange();
}