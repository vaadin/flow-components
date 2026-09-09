/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;

/**
 * Ports the reproducer from issues #9180 and #9036, so tests exercise the same
 * combination it uses:
 * <ul>
 * <li>four adjacent editors in B2:E2, a new instance on every factory
 * call,</li>
 * <li>a value-change listener that writes the cell and calls
 * {@code refreshCells}, <b>without</b> an {@code isFromClient} guard,</li>
 * <li>{@code autoselect} enabled through the DOM attribute, which is the only
 * way to enable it,</li>
 * <li>a slow {@code onCustomEditorDisplayed} that sets a value which is
 * <b>not</b> in the item set and then calls {@code inputElement.select()}.</li>
 * </ul>
 * <p>
 * Kept separate from {@link CustomEditorSelectFixture}, which backs the
 * infinite-loop regression tests from #9113 and must keep its current shape.
 */
public class CustomEditorDelayedCallbackFixture implements SpreadsheetFixture {

    private static final String[] FRUITS = { "Apple", "Banana", "Cherry" };

    /** Prefix of the value {@code onCustomEditorDisplayed} sets. */
    public static final String CALLBACK_VALUE_PREFIX = "Value Reset ";

    /**
     * Makes the callback slow enough for the selection to move on before it
     * completes. Shorter than the reproducer's one second, which is not needed.
     */
    private static final long CALLBACK_DELAY_MILLIS = 200;

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        for (int column = 1; column <= 4; column++) {
            spreadsheet.setColumnWidth(column, 150);
        }
        spreadsheet.setSpreadsheetComponentFactory(
                new DelayedCallbackEditorFactory());
    }

    private static class DelayedCallbackEditorFactory
            implements SpreadsheetComponentFactory {

        private int callbackCount;
        private Span counterLabel;

        /**
         * Exposes the callback count in cell A1 so tests can check that the
         * un-guarded {@code refreshCells} above does not keep re-firing
         * {@code onCustomEditorDisplayed}.
         */
        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (rowIndex != 0 || columnIndex != 0) {
                return null;
            }
            if (counterLabel == null) {
                counterLabel = new Span("0");
                counterLabel.setId("callbackCount");
            }
            return counterLabel;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (rowIndex != 1 || columnIndex < 1 || columnIndex > 4) {
                return null;
            }
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(FRUITS);
            comboBox.setWidthFull();

            if (cell != null && cell.getCellType() == CellType.STRING) {
                comboBox.setValue(cell.getStringCellValue());
            }

            // No isFromClient guard, matching the reported code: the callback's
            // own setValue also reaches this listener.
            comboBox.addValueChangeListener(event -> {
                if (cell != null) {
                    cell.setCellValue(event.getValue());
                    spreadsheet.refreshCells(cell);
                }
            });

            comboBox.getElement().setAttribute("autoselect", true);
            return comboBox;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (!(customEditor instanceof ComboBox<?> comboBox)) {
                return;
            }
            callbackCount++;
            if (counterLabel != null) {
                counterLabel.setText(String.valueOf(callbackCount));
            }
            try {
                Thread.sleep(CALLBACK_DELAY_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            @SuppressWarnings("unchecked")
            ComboBox<String> typed = (ComboBox<String>) comboBox;
            typed.setValue(CALLBACK_VALUE_PREFIX + columnIndex);
            // autoselect only fires on focus, which has already happened, so
            // the new text has to be selected explicitly.
            customEditor.getElement().executeJs("this.inputElement.select();");
        }
    }
}
