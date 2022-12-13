package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Arrays;

public class CustomComponentFixture implements SpreadsheetFixture {

    static final int HEADER_ROW_INDEX = 0;

    static final int COMPONENT_ROW_INDEX = 1;

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        for (ComponentType componentType : ComponentType.values()) {
            spreadsheet.createCell(HEADER_ROW_INDEX,
                    componentType.getColumnIndex(),
                    componentType.getHeaderText());
        }
        spreadsheet
                .setSpreadsheetComponentFactory(new ComponentEditorFactory());
    }
}

class ComponentEditorFactory implements SpreadsheetComponentFactory {

    private final Button button = new Button("Click",
            e -> Notification.show("Button clicked!"));

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        ComponentType componentType = ComponentType
                .getComponentTypeByIndex(rowIndex, columnIndex);
        if (componentType == null) {
            return null;
        }
        return getCustomComponent(componentType);
    }

    @Override
    public Component getCustomEditorForCell(Cell cell, final int rowIndex,
            final int columnIndex, final Spreadsheet spreadsheet, Sheet sheet) {
        return null;
    }

    @Override
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor) {
    }

    private Object getCellValueForComponent(ComponentType componentType,
            Cell cell) {
        return cell.getStringCellValue();
    }

    private Component getCustomComponent(ComponentType componentType) {
        if (componentType != null) {
            switch (componentType) {
            case BUTTON:
                // TODO revisar - js exception in mouseover
                return button;
            }
        }
        return null;
    }
}

enum ComponentType {

    // TODO revisar - add more components
    BUTTON(1, "Button");

    private int columnIndex;

    private final String headerText;

    ComponentType(int columnIndex, String headerText) {
        this.columnIndex = columnIndex;
        this.headerText = headerText;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getHeaderText() {
        return headerText;
    }

    public static ComponentType getComponentTypeByIndex(int row, int col) {
        if (row != CustomComponentFixture.COMPONENT_ROW_INDEX) {
            return null;
        }
        return Arrays.stream(values()).filter(type -> type.columnIndex == col)
                .findAny().orElse(null);
    }
}