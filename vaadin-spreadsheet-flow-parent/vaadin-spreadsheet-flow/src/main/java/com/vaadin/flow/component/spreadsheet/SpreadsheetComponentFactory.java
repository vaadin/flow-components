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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;

/**
 * Defines an interface for having custom components inside the
 * {@link Spreadsheet}. Use it with
 * {@link Spreadsheet#setSpreadsheetComponentFactory(SpreadsheetComponentFactory)}
 * . The more custom components you have visible, the slower the spreadsheet
 * comes. It is a very bad idea to have layouts and complex widgets inside the
 * spreadsheet.
 * <p>
 * There are two types of custom components inside the {@link Cell}s. The ones
 * returned by
 * {@link #getCustomComponentForCell(Cell, int, int, Spreadsheet, Sheet)} are
 * always visible inside the cells as they are rendered. These components are
 * unique per cell. Having many of them visible at the same time will decrease
 * the spreadsheet performance. This method is <b>NOT</b> meant for displaying
 * images or charts inside the spreadsheet.
 * <p>
 * The components returned by
 * {@link #getCustomEditorForCell(Cell, int, int, Spreadsheet, Sheet)} are shown
 * as the cells are selected. Thus, they are meant for replacing the default
 * inline editor in the spreadsheet. These components can be shared with
 * multiple cells; when a component comes visible inside a cell, the
 * {@link #onCustomEditorDisplayed(Cell, int, int, Spreadsheet, Sheet, com.vaadin.flow.component.Component)}
 * is called with the appropriate parameters making it possible to update custom
 * editor value to correspond to the cell's value.
 * <p>
 * The {@link #getCustomComponentForCell(Cell, int, int, Spreadsheet, Sheet)} is
 * called first
 *
 * @author Vaadin Ltd.
 */
public interface SpreadsheetComponentFactory extends Serializable {

    /**
     * Should return a unique component that is displayed inside the cell
     * instead of the cell's value. Unique - because the same component instance
     * can't be at two places at once (just like any component).
     * <p>
     * Having custom components always visible inside some Spreadsheet cells
     * makes it possible to add some custom functionality into the sheet:
     * ComboBoxes for filtering, Buttons for doing calculations etc.
     * <p>
     * Note that each component makes the Spreadsheet a little bit slower.
     * <p>
     * This method is called <b>before</b>
     * {@link #getCustomEditorForCell(Cell, int, int, Spreadsheet, Sheet)}.
     * <p>
     * For merged regions, this method is only called for the first cell of the
     * merged region.
     *
     * @param cell
     *            Cell that should display the component or <code>null</code> if
     *            the cell doesn't yet exist inside POI
     * @param rowIndex
     *            0-based
     * @param columnIndex
     *            0-based
     * @param spreadsheet
     *            The target Spreadsheet component
     * @param sheet
     *            The active sheet of the workbook (never <code>null</code>)
     * @return The unique component that is displayed as the corresponding cell
     *         becomes visible or <code>null</code> if no component should be
     *         displayed when the cell is not selected.
     */
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet);

    /**
     * Should return the custom component that is displayed in the cell when it
     * has been selected. Thus, the component replaces the default inline editor
     * functionality in the Spreadsheet. This method is called only for cells
     * that are <b>not locked</b> (a cell is considered locked when the sheet or
     * cell is protected or the cell is null).
     * <p>
     * If some cells share the same type of "editor", the same component
     * instance can be shared for all of those cells. As the component comes
     * visible in Cell X, the
     * {@link #onCustomEditorDisplayed(Cell, int, int, Spreadsheet, Sheet, Component)}
     * is called with the appropriate parameters. This way, you can update the
     * editor component value accordingly to the currently selected cell.
     * <p>
     * This method is called <b>after</b>
     * {@link #getCustomComponentForCell(Cell, int, int, Spreadsheet, Sheet)},
     * if it returned <code>null</code>.
     * <p>
     * For merged regions, this method is only called for the first cell of the
     * merged region.
     *
     * @param cell
     *            Cell that should display the custom editor or
     *            <code>null</code> if the cell doesn't yet exist inside POI
     * @param rowIndex
     *            0-based
     * @param columnIndex
     *            0-based
     * @param spreadsheet
     *            The target spreadsheet component
     * @param sheet
     *            The active sheet of the workbook (never <code>null</code>)
     * @return The component that should be used as the custom editor or
     *         <code>null</code> if the default editor (input field) should be
     *         used.
     */
    public Component getCustomEditorForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet);

    /**
     * This method is called when a cell with a custom editor is displayed (the
     * cell is selected). The purpose of this method is to make it possible to
     * share the same editor component instance between multiple cells; you can
     * update the component with the appropriate value depending on the cell.
     * <p>
     * Note that the Spreadsheet component doesn't automatically update the Cell
     * value if it has a custom editor.
     *
     * @param cell
     *            The cell that has the editor, might be <code>null</code> if
     *            the cell doesn't exist it the POI model
     * @param rowIndex
     *            0-based
     * @param columnIndex
     *            0-based
     * @param spreadsheet
     *            The target spreadsheet component
     * @param sheet
     *            The active sheet of the workbook (never <code>null</code>)
     * @param customEditor
     *            The component that is displayed inside the cell
     */
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor);
}
