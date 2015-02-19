package com.vaadin.addon.spreadsheet.command;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
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