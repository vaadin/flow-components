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
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;

/**
 * Test fixture for verifying that custom editors calling
 * {@code inputElement.select()} in {@code onCustomEditorDisplayed} do not cause
 * an infinite focus loop between client and server.
 * <p>
 * Exposes a callback counter as a custom component in cell A1 (id
 * "callbackCount") so tests can verify the number of
 * {@code onCustomEditorDisplayed} invocations.
 */
public class CustomEditorSelectFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.setColumnWidth(1, 150);
        spreadsheet.setColumnWidth(2, 150);
        spreadsheet.setColumnWidth(3, 150);
        spreadsheet.setColumnWidth(4, 150);

        spreadsheet.setSpreadsheetComponentFactory(new SelectEditorFactory());
    }

    private static class SelectEditorFactory
            implements SpreadsheetComponentFactory {

        private static final String[] FRUITS = { "Apple", "Banana", "Cherry" };

        private int callbackCount;
        private Span counterLabel;

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (rowIndex == 0 && columnIndex == 0) {
                if (counterLabel == null) {
                    counterLabel = new Span("0");
                    counterLabel.setId("callbackCount");
                }
                return counterLabel;
            }
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            if (rowIndex == 1 && columnIndex >= 1 && columnIndex <= 4) {
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
            if (counterLabel != null) {
                counterLabel.setText(String.valueOf(callbackCount));
            }

            if (customEditor instanceof ComboBox<?>) {
                try {
                    // Simulate server-side processing time (DB lookups,
                    // business logic). This delay causes the client to queue
                    // subsequent cellSelected RPCs, reproducing the scenario
                    // where the user navigates away before the response
                    // arrives.
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                @SuppressWarnings("unchecked")
                ComboBox<String> comboBox = (ComboBox<String>) customEditor;
                comboBox.setValue(FRUITS[columnIndex % FRUITS.length]);
                comboBox.getElement().executeJs("this.inputElement.select();");
            }
        }
    }
}
