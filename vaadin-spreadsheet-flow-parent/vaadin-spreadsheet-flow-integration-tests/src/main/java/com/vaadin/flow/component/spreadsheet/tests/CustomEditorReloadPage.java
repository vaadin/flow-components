/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.router.Route;

/**
 * View for verifying custom editor handling on scroll versus explicit refresh
 * (vaadin/flow-components#9180).
 * <p>
 * The factory returns a new {@link ComboBox} on every
 * {@code getCustomEditorForCell} call (a common usage pattern). Scrolling must
 * reuse the editor already shown for a cell, so its value survives and
 * {@code onCustomEditorDisplayed} is not re-fired for an unchanged selection.
 * An explicit refresh via {@link Spreadsheet#reloadVisibleCellContents()}
 * ({@code reloadBtn}) keeps the long-standing behavior of recreating editors
 * from the factory.
 * <p>
 * The callback counter is rendered on the page (id {@code callbackCount})
 * rather than inside a cell, so it stays visible regardless of scrolling.
 */
@Route("vaadin-spreadsheet/custom-editor-reload")
public class CustomEditorReloadPage extends VerticalLayout {

    static final String[] FRUITS = { "Apple", "Banana", "Cherry" };

    private int callbackCount;
    private final Span callbackCounter = new Span("0");
    private final Spreadsheet spreadsheet = new Spreadsheet();

    public CustomEditorReloadPage() {
        setSizeFull();

        callbackCounter.setId("callbackCount");

        Button reload = new Button("Reload visible cell contents",
                e -> spreadsheet.reloadVisibleCellContents());
        reload.setId("reloadBtn");

        spreadsheet.setSpreadsheetComponentFactory(new ComboBoxEditorFactory());
        spreadsheet.setSizeFull();

        Div spreadsheetContainer = new Div(spreadsheet);
        spreadsheetContainer.setSizeFull();

        add(new HorizontalLayout(callbackCounter, reload),
                spreadsheetContainer);
        setFlexGrow(1, spreadsheetContainer);
    }

    private class ComboBoxEditorFactory implements SpreadsheetComponentFactory {

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            // Editor in B2. A new instance is returned on every call to
            // exercise the editor-preservation logic.
            if (rowIndex == 1 && columnIndex == 1) {
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.setItems(FRUITS);
                comboBox.setWidthFull();
                return comboBox;
            }
            return null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            callbackCount++;
            callbackCounter.setText(String.valueOf(callbackCount));
            if (customEditor instanceof ComboBox<?> comboBox) {
                @SuppressWarnings("unchecked")
                ComboBox<String> typed = (ComboBox<String>) comboBox;
                typed.setValue(FRUITS[columnIndex % FRUITS.length]);
            }
        }
    }
}
