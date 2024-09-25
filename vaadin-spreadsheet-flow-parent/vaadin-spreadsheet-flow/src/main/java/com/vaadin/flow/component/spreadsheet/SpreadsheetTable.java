/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;

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
    private transient CTAutoFilter ctWorksheetAutoFilter;
    private transient XSSFTable xssfTable;

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
        this(spreadsheet, sheet, fullTableRegion, null, null);
    }

    /**
     * Creates a new table for the given spreadsheet component, sheet and region
     * while referencing the backing xssfTable or ctWorksheetAutoFilter. If the
     * component is currently displaying the sheet that the table belongs to,
     * pop-up buttons are added to table headers (first row cells).
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param sheet
     *            Target sheet within the spreadsheet
     * @param fullTableRegion
     *            Cell range to build the table in
     * @param ctWorksheetAutoFilter
     *            Set this to not-null if this table is backed by a
     *            XSSFSheet.getCTWorksheet().getAutoFilter()
     * @param xssfTable
     *            Set this to not-null if this table is backed by a XSSFTable
     */
    public SpreadsheetTable(Spreadsheet spreadsheet, Sheet sheet,
            CellRangeAddress fullTableRegion,
            CTAutoFilter ctWorksheetAutoFilter, XSSFTable xssfTable) {
        this.spreadsheet = spreadsheet;
        this.sheet = sheet;
        this.fullTableRegion = fullTableRegion;
        this.xssfTable = xssfTable;
        this.ctWorksheetAutoFilter = ctWorksheetAutoFilter;
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
        if (isActiveSheet(sheet)) {
            var targetCells = resolveCellsForPopupButtonCreation();
            for (CellReference popupButtonTargetCell : targetCells) {
                PopupButton popupButton = new PopupButton();
                popupButtons.put(popupButtonTargetCell, popupButton);
                spreadsheet.setPopup(popupButtonTargetCell, popupButton);
            }
        }
    }

    private List<CellReference> resolveCellsForPopupButtonCreation() {
        if (xssfTable != null) {
            // if this SpreadsheetTable is backed by XssfTable consider content
            // of its CTAutoFilter
            return resolveCellsForPopupButtonCreation(xssfTable.getCTTable());
        } else {
            return getAllHeaderRowCells();
        }
    }

    private List<CellReference> resolveCellsForPopupButtonCreation(
            CTTable table) {
        if (table.isSetAutoFilter()) {
            return getVisibleAutoFilterPopupButtonsCells(table);
        } else {
            // if AutoFilter is not set, then don't display any popup buttons
            return Collections.emptyList();
        }
    }

    private List<CellReference> getVisibleAutoFilterPopupButtonsCells(
            CTTable table) {
        if (table.getAutoFilter().getFilterColumnList().isEmpty()) {
            // if there are no filter columns in this list, then display
            // filter popup buttons for all columns (strange, I know)
            return getAllHeaderRowCells();
        } else {
            // otherwise display filter popup buttons only for columns where the
            // popup buttons are not hidden
            return table.getAutoFilter().getFilterColumnList().stream()
                    .filter(filterColumn -> !filterColumn.isSetHiddenButton())
                    .map(filterColumn -> new CellReference(sheet.getSheetName(),
                            fullTableRegion.getFirstRow(),
                            (int) filterColumn.getColId(), true, true))
                    .collect(Collectors.toList());
        }
    }

    private List<CellReference> getAllHeaderRowCells() {
        var result = new ArrayList<CellReference>();
        for (int c = fullTableRegion.getFirstColumn(); c <= fullTableRegion
                .getLastColumn(); c++) {
            CellReference popupButtonCellReference = new CellReference(
                    sheet.getSheetName(), fullTableRegion.getFirstRow(), c,
                    true, true);
            result.add(popupButtonCellReference);
        }
        return result;
    }

    private boolean isActiveSheet(Sheet sheet) {
        return sheet.equals(spreadsheet.getActiveSheet());
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

    /**
     * Sets the CTAutoFilter object that represents this table in the underlying
     * POI model.
     *
     * @param ctWorksheetAutoFilter
     *            Referenced autofilter.
     */
    protected void setCtWorksheetAutoFilter(
            CTAutoFilter ctWorksheetAutoFilter) {
        this.ctWorksheetAutoFilter = ctWorksheetAutoFilter;
    }

    /**
     * @return Returns the CTAutoFilter object that represents this table in the
     *         underlying POI model. Can be null if this table is not backed by
     *         a Worksheet CTAutoFilter.
     */
    public CTAutoFilter getCtWorksheetAutoFilter() {
        return ctWorksheetAutoFilter;
    }

    /**
     * Sets the XSSFTable object that represents this table in the underlying
     * POI model.
     *
     * @param xssfTable
     *            Referenced table.
     */
    protected void setXssfTable(XSSFTable xssfTable) {
        this.xssfTable = xssfTable;
    }

    /**
     * @return Returns the XSSFTable object that represents this table in the
     *         underlying POI model. Can be null if this table is not backed by
     *         a XSSFTable.
     */
    public XSSFTable getXssfTable() {
        return xssfTable;
    }
}
