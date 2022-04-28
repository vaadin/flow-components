package com.vaadin.flow.component.spreadsheet;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Represents a "table" inside a spreadsheet.
 *
 * A table is a region ( {@link CellRangeAddress}), that has {@link PopupButton}
 * on the column header cells of the region. In this context the column header
 * cells refer to the cells on the first row of the region.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SpreadsheetTable implements Serializable {

    private final CellRangeAddress fullTableRegion;
    private final Sheet sheet;
    private final Spreadsheet spreadsheet;
    protected final Map<CellReference, PopupButton> popupButtons;

    /**
     * Creates a new table for the given spreadsheet component, its active sheet
     * (returned by {@link Spreadsheet#getActiveSheet()}) and the given region.
     * Adds pop-up buttons for table headers (cells in the first row).
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param tableRegion
     *            Cell range to build the table in
     */
    public SpreadsheetTable(Spreadsheet spreadsheet,
            CellRangeAddress tableRegion) {
        this(spreadsheet, spreadsheet.getActiveSheet(), tableRegion);
    }

    /**
     * Creates a new table for the given spreadsheet component, sheet and
     * region. If the component is currently displaying the sheet that the table
     * belongs to, pop-up buttons are added to table headers (first row cells).
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param sheet
     *            Target sheet within the spreadsheet
     * @param fullTableRegion
     *            Cell range to build the table in
     */
    public SpreadsheetTable(Spreadsheet spreadsheet, Sheet sheet,
            CellRangeAddress fullTableRegion) {
        this.spreadsheet = spreadsheet;
        this.sheet = sheet;
        this.fullTableRegion = fullTableRegion;
        popupButtons = new HashMap<CellReference, PopupButton>();

        if (isTableSheetCurrentlyActive()) {
            initPopupButtons();
        }
    }

    /**
     * Reload the table's pop-up buttons, if spreadsheet component is currently
     * presenting the sheet this table belongs to.
     * <p>
     * If there are no pop-up buttons stored, when {@link #clear()} has been
     * called, the pop-up buttons are recreated. Otherwise they are just added
     * to the spreadsheet component again.
     */
    public void reload() {
        if (isTableSheetCurrentlyActive()) {
            if (popupButtons.isEmpty()) {
                initPopupButtons();
            } else {
                for (PopupButton popupButton : popupButtons.values()) {
                    spreadsheet.setPopup(popupButton.getCellReference(),
                            popupButton);
                }
            }
        }
    }

    /**
     * Clears all the table's pop-up buttons and their pop-up content.
     */
    public void clear() {
        for (PopupButton popupButton : popupButtons.values()) {
            popupButton.setContent(null);
        }
        popupButtons.clear();
    }

    /**
     * Returns true if the spreadsheet component is currently displaying the
     * sheet that this table belongs to.
     *
     * @return true if the sheet this table belongs to is active
     */
    public boolean isTableSheetCurrentlyActive() {
        return spreadsheet.getActiveSheet().equals(sheet);
    }

    /**
     * Initializes the pop-up buttons of this table.
     */
    protected void initPopupButtons() {
        if (sheet.equals(spreadsheet.getActiveSheet())) {
            for (int c = fullTableRegion.getFirstColumn(); c <= fullTableRegion
                    .getLastColumn(); c++) {
                CellReference popupButtonCellReference = new CellReference(
                        sheet.getSheetName(), fullTableRegion.getFirstRow(), c,
                        true, true);
                PopupButton popupButton = new PopupButton();
                popupButtons.put(popupButtonCellReference, popupButton);
                spreadsheet.setPopup(popupButtonCellReference, popupButton);
            }
        }
    }

    /**
     * Gets the {@link Sheet} this table belongs to.
     *
     * @return Sheet this table belongs to
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * Gets the {@link Spreadsheet} component this table belongs to.
     *
     * @return Spreadsheet this table belongs to
     */
    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    /**
     * Gets the full table region, {@link CellRangeAddress} for this table.
     *
     * @return Table region
     */
    public CellRangeAddress getFullTableRegion() {
        return fullTableRegion;
    }

    /**
     * Gets the {@link PopupButton} for the given column. If given column is
     * outside of the table region, <code>null</code> will be returned.
     *
     * @param col
     *            Column index, 0-based
     * @return the {@link PopupButton} contained in the header column of this
     *         table.
     */
    public PopupButton getPopupButton(int col) {
        for (PopupButton button : popupButtons.values()) {
            if (button.getColumn() == col) {
                return button;
            }
        }

        return null;
    }

    /**
     * Gets the {@link PopupButton} for the header cell pointed by
     * {@link CellReference}. If given reference is not a header cell for this
     * table, or is outside of the table region, <code>null</code> will be
     * returned.
     *
     * @param filterCellReference
     *            header cell reference
     * @return Pop-up button from the given cell, or null if not found
     */
    public PopupButton getPopupButton(CellReference filterCellReference) {
        return popupButtons.get(filterCellReference);
    }

    /**
     * Returns all of the {@link PopupButton}s for this table.
     *
     * @return the pop-up buttons for this table in no specific order.
     */
    public Collection<PopupButton> getPopupButtons() {
        return Collections.unmodifiableCollection(popupButtons.values());
    }

}
