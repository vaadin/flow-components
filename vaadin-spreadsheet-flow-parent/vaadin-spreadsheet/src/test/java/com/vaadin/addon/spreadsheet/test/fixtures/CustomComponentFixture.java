package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class CustomComponentFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        CellMultiplexerEditorFactory editorFactory = new CellMultiplexerEditorFactory();

        editorFactory.addEditor(1, 1, new ClassEditorFactory(TextField.class));
        editorFactory.addEditor(1, 2, new ClassEditorFactory(CheckBox.class));
        editorFactory.addEditor(1, 3, new ClassEditorFactory(DateField.class));
        editorFactory.addEditor(1, 4, new ClassEditorFactory(
                PopupDateField.class));
        editorFactory.addEditor(1, 5, new ClassEditorFactory(
                InlineDateField.class));
        editorFactory.addEditor(1, 6, new ClassEditorFactory(TextArea.class));
        editorFactory.addEditor(1, 7, new ClassEditorFactory(Slider.class));
        spreadsheet.createCell(0, 1, "TextField");
        spreadsheet.createCell(0, 2, "CheckBox");
        spreadsheet.createCell(0, 3, "DateField");
        spreadsheet.createCell(0, 4, "InlineDate");
        spreadsheet.createCell(0, 5, "PopupDate");
        spreadsheet.createCell(0, 6, "TextArea");
        spreadsheet.createCell(0, 7, "Slider");

        editorFactory.addEditor(1, 8, new EditorFactory() {

            @Override
            public Field<?> create() {
                final NativeSelect comboBox = new NativeSelect();
                comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
                comboBox.addItem("10");
                comboBox.addItem("20");
                comboBox.addItem("30");
                comboBox.addItem("40");
                comboBox.addItem("50");

                comboBox.setItemCaption("10", "Opt 1");
                comboBox.setItemCaption("20", "Opt 2");
                comboBox.setItemCaption("30", "Opt 3");
                comboBox.setItemCaption("40", "Opt 4");
                comboBox.setItemCaption("50", "Opt 5");
                comboBox.setNullSelectionAllowed(false);
                comboBox.setSizeFull();

                return comboBox;
            }
        });
        spreadsheet.createCell(0, 8, "ComboBox");

        final CellMultiplexerComponentFactory componentFactory = new CellMultiplexerComponentFactory();

        Button b10 = new Button("click");
        b10.setId("b10-btn");
        b10.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                spreadsheet.createCell(10, 1, "42");
                Label label = new Label("b12");
                label.setId("b12-label");
                componentFactory.addEditor(11, 1, new EagerComponentFactory(
                        label));
                spreadsheet.reloadVisibleCellContents();
            }
        });

        componentFactory.addEditor(9, 1, new EagerComponentFactory(b10));

        ComponentFactoryStack factoryStack = new ComponentFactoryStack();
        factoryStack.addFactory(editorFactory);
        factoryStack.addFactory(componentFactory);

        spreadsheet.setSpreadsheetComponentFactory(factoryStack);
        spreadsheet.refreshAllCellValues();
    }

}

class CellMultiplexerEditorFactory implements SpreadsheetComponentFactory {

    private static final long serialVersionUID = 1L;
    private Map<String, EditorFactory> editorFactories = new HashMap<String, EditorFactory>();

    public void addEditor(int row, int column, EditorFactory editorFactory) {
        editorFactories.put(row + ":" + column, editorFactory);
    }

    @Override
    public Component getCustomEditorForCell(Cell cell, final int rowIndex,
            final int columnIndex, final Spreadsheet spreadsheet, Sheet sheet) {
        if (!editorFactories.containsKey(rowIndex + ":" + columnIndex)) {
            return null;
        }

        Field<?> field = editorFactories.get(rowIndex + ":" + columnIndex)
                .create();

        field.addValueChangeListener(new ValueToCellUpdater(spreadsheet,
                rowIndex, columnIndex));

        if (field instanceof AbstractComponent) {
            ((AbstractComponent) field).setImmediate(true);
        }
        return field;
    }

    @Override
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor) {
        @SuppressWarnings("unchecked")
        final Field<Object> field = (Field<Object>) customEditor;
        final Cell curCell = spreadsheet.getCell(rowIndex, columnIndex);

        if (curCell != null) {
            Object value = spreadsheet.getCellValue(curCell);
            if (field.getType().equals(Boolean.class)
                    && value instanceof String) {
                value = Boolean.parseBoolean((String) value);
            }
            field.setValue(value);
        }
    }

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        // TODO Auto-generated method stub
        return null;
    }

}

class ValueToCellUpdater implements ValueChangeListener {

    private static final long serialVersionUID = 1L;
    private Spreadsheet spreadsheet;
    private int rowIndex;
    private int columnIndex;

    public ValueToCellUpdater(Spreadsheet spreadsheet, int rowIndex,
            int columnIndex) {
        this.spreadsheet = spreadsheet;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        spreadsheet.createCell(rowIndex, columnIndex, event.getProperty()
                .getValue());
        spreadsheet.refreshCells(spreadsheet.getCell(rowIndex, columnIndex));
    }
}

interface EditorFactory {
    public Field<?> create();
}

class ClassEditorFactory implements EditorFactory {
    private Class<? extends Field<?>> editorClass;

    public ClassEditorFactory(Class<? extends Field<?>> editorClass) {
        super();
        this.editorClass = editorClass;
    }

    @Override
    public Field<?> create() {
        try {
            return editorClass.newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}

class ComponentFactoryStack implements SpreadsheetComponentFactory {

    private static final long serialVersionUID = 1L;
    private List<SpreadsheetComponentFactory> factories = new ArrayList<SpreadsheetComponentFactory>();

    public void addFactory(SpreadsheetComponentFactory factory) {
        factories.add(factory);
    }

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {

        for (SpreadsheetComponentFactory factory : factories) {
            Component c = factory.getCustomComponentForCell(cell, rowIndex,
                    columnIndex, spreadsheet, sheet);
            if (c != null) {
                return c;
            }
        }

        return null;
    }

    @Override
    public Component getCustomEditorForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        for (SpreadsheetComponentFactory factory : factories) {
            Component c = factory.getCustomEditorForCell(cell, rowIndex,
                    columnIndex, spreadsheet, sheet);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor) {
        for (SpreadsheetComponentFactory factory : factories) {
            factory.onCustomEditorDisplayed(cell, rowIndex, columnIndex,
                    spreadsheet, sheet, customEditor);
        }
    }

}

class CellMultiplexerComponentFactory implements SpreadsheetComponentFactory {

    private static final long serialVersionUID = 1L;
    private Map<String, SpreadsheetComponentFactory> factories = new HashMap<String, SpreadsheetComponentFactory>();

    public void addEditor(int row, int column,
            SpreadsheetComponentFactory factory) {
        factories.put(row + ":" + column, factory);
    }

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        if (!factories.containsKey(rowIndex + ":" + columnIndex)) {
            return null;
        }
        return factories.get(rowIndex + ":" + columnIndex)
                .getCustomComponentForCell(cell, rowIndex, columnIndex,
                        spreadsheet, sheet);
    }

    @Override
    public Component getCustomEditorForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        if (!factories.containsKey(rowIndex + ":" + columnIndex)) {
            return null;
        }
        return factories.get(rowIndex + ":" + columnIndex)
                .getCustomEditorForCell(cell, rowIndex, columnIndex,
                        spreadsheet, sheet);
    }

    @Override
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor) {
        if (!factories.containsKey(rowIndex + ":" + columnIndex)) {
            return;
        }
        factories.get(rowIndex + ":" + columnIndex).onCustomEditorDisplayed(
                cell, rowIndex, columnIndex, spreadsheet, sheet, customEditor);
    }

}

class EagerComponentFactory implements SpreadsheetComponentFactory {

    private static final long serialVersionUID = 1L;
    private Component component;

    public EagerComponentFactory(Component component) {
        super();
        this.component = component;
    }

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        return component;
    }

    @Override
    public Component getCustomEditorForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        return null;
    }

    @Override
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor) {
    }

}
