package com.vaadin.addon.spreadsheet.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.ProtectedCellWriteAttemptedEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.ProtectedCellWriteAttemptedListener;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectedSheetChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectedSheetChangeListener;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeListener;
import com.vaadin.addon.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.addon.spreadsheet.SpreadsheetFactory;
import com.vaadin.addon.spreadsheet.demo.action.SpreadsheetDefaultActionHandler;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SpreadsheetDemoUI extends UI implements Receiver {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = SpreadsheetDemoUI.class, widgetset = "com.vaadin.addon.spreadsheet.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    GridLayout layout = new GridLayout(1, 2);

    Upload upload = new Upload("Upload a XLS file to view it", this);
    Spreadsheet spreadsheet;

    private File previousFile = null;

    private Button save;

    private Button download;

    private File uploadedFile;

    private ComboBox openTestSheetSelect;

    private SelectionChangeListener selectionChangeListener;

    private SpreadsheetComponentFactory spreadsheetFieldFactory;

    private SelectedSheetChangeListener selectedSheetChangeListener;

    private final Handler spreadsheetActionHandler = new SpreadsheetDefaultActionHandler();

    private CheckBox gridlines;
    private CheckBox rowColHeadings;

    public SpreadsheetDemoUI() {
        super();
        SpreadsheetFactory.logMemoryUsage();
    }

    @Override
    protected void init(VaadinRequest request) {
        SpreadsheetFactory.logMemoryUsage();
        setContent(layout);

        HorizontalLayout options = new HorizontalLayout();
        options.setSpacing(true);

        layout.setMargin(true);
        layout.setSizeFull();

        gridlines = new CheckBox("display grid lines");
        gridlines.setImmediate(true);

        rowColHeadings = new CheckBox("display row and column headers");
        rowColHeadings.setImmediate(true);

        gridlines.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                Boolean display = (Boolean) event.getProperty().getValue();

                if (spreadsheet != null) {
                    spreadsheet.setDisplayGridlines(display);
                }
            }
        });

        rowColHeadings.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                Boolean display = (Boolean) event.getProperty().getValue();

                if (spreadsheet != null) {
                    spreadsheet.setDisplayRowColHeadings(display);
                }
            }
        });

        Button newSpreadsheetButton = new Button("Create new",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (spreadsheet == null) {
                            spreadsheet = new Spreadsheet();
                            spreadsheet
                                    .addSelectionChangeListener(selectionChangeListener);
                            spreadsheet
                                    .addSelectedSheetChangeListener(selectedSheetChangeListener);
                            spreadsheet
                                    .addActionHandler(spreadsheetActionHandler);
                            Window w = new Window("Spreadsheet in window",
                                    spreadsheet);
                            addWindow(w);
                        } else {
                            spreadsheet.reloadSpreadsheetWithNewWorkbook();
                        }
                        spreadsheet.setSpreadsheetComponentFactory(null);
                        save.setEnabled(true);
                        previousFile = null;
                        openTestSheetSelect.setValue(null);

                        gridlines.setValue(spreadsheet.isDisplayGridLines());
                        rowColHeadings.setValue(spreadsheet
                                .isDisplayRowColHeadings());
                    }
                });

        Button newSpreadsheetInWindowButton = new Button("Create new",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (spreadsheet == null) {
                            spreadsheet = new Spreadsheet();
                            spreadsheet
                                    .addSelectionChangeListener(selectionChangeListener);
                            spreadsheet
                                    .addSelectedSheetChangeListener(selectedSheetChangeListener);
                            spreadsheet
                                    .addActionHandler(spreadsheetActionHandler);

                            Window w = new Window("new Spreadsheet",
                                    spreadsheet);
                            w.setWidth("50%");
                            w.setHeight("50%");
                            w.center();
                            addWindow(w);
                        } else {
                            spreadsheet.reloadSpreadsheetWithNewWorkbook();
                        }
                        spreadsheet.setSpreadsheetComponentFactory(null);
                        save.setEnabled(true);
                        previousFile = null;
                        openTestSheetSelect.setValue(null);

                        gridlines.setValue(spreadsheet.isDisplayGridLines());
                        rowColHeadings.setValue(spreadsheet
                                .isDisplayRowColHeadings());
                    }
                });

        File root = null;
        try {
            ClassLoader classLoader = SpreadsheetDemoUI.class.getClassLoader();
            URL resource = classLoader.getResource("testsheets"
                    + File.separator);
            if (resource != null) {
                root = new File(resource.toURI());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        FilesystemContainer testSheetContainer = new FilesystemContainer(root);
        testSheetContainer.setRecursive(false);
        testSheetContainer.setFilter(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name != null
                        && (name.endsWith(".xls") || name.endsWith(".xlsx"))) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        openTestSheetSelect = new ComboBox("Open test sheet",
                testSheetContainer);
        openTestSheetSelect.setPageLength(0);
        openTestSheetSelect.setImmediate(true);
        openTestSheetSelect.setItemCaptionPropertyId("Name");
        openTestSheetSelect.setItemIconPropertyId("Icon");
        openTestSheetSelect.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                Object value = openTestSheetSelect.getValue();
                if (value instanceof File) {
                    loadFile((File) value);
                }
            }
        });
        save = new Button("Save", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (spreadsheet != null) {
                    try {
                        if (previousFile != null) {
                            int i = previousFile.getName().lastIndexOf(".xls");
                            String fileName = previousFile.getName().substring(
                                    0, i)
                                    + ("(1)")
                                    + previousFile.getName().substring(i);
                            previousFile = spreadsheet
                                    .writeSpreadsheetIntoFile(fileName);
                        } else {
                            previousFile = spreadsheet
                                    .writeSpreadsheetIntoFile("workbook1");
                        }
                        download.setEnabled(true);
                        FileResource resource = new FileResource(previousFile);
                        FileDownloader fileDownloader = new FileDownloader(
                                resource);
                        fileDownloader.extend(download);
                        previousFile.deleteOnExit();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        save.setEnabled(false);
        download = new Button("Download");
        download.setEnabled(false);

        Button customComponentTest = new Button(
                "Create Custom Editor Test sheet", new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (spreadsheet == null) {
                            spreadsheet = new Spreadsheet(
                                    ((SpreadsheetEditorComponentFactoryTest) spreadsheetFieldFactory)
                                            .getTestWorkbook());
                            spreadsheet
                                    .setSpreadsheetComponentFactory(spreadsheetFieldFactory);
                            spreadsheet
                                    .addActionHandler(spreadsheetActionHandler);
                            layout.addComponent(spreadsheet);
                            layout.setRowExpandRatio(1, 1.0F);
                        } else {
                            spreadsheet
                                    .setWorkbook(((SpreadsheetEditorComponentFactoryTest) spreadsheetFieldFactory)
                                            .getTestWorkbook());
                            spreadsheet
                                    .setSpreadsheetComponentFactory(spreadsheetFieldFactory);
                        }

                        gridlines.setValue(spreadsheet.isDisplayGridLines());
                        rowColHeadings.setValue(spreadsheet
                                .isDisplayRowColHeadings());
                    }
                });

        VerticalLayout checkBoxLayout = new VerticalLayout();
        checkBoxLayout.addComponents(gridlines, rowColHeadings);
        options.addComponent(checkBoxLayout);
        options.addComponent(newSpreadsheetButton);
        options.addComponent(newSpreadsheetInWindowButton);
        options.addComponent(customComponentTest);
        options.addComponent(openTestSheetSelect);
        options.addComponent(upload);
        options.addComponent(new Button("Close", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (spreadsheet != null) {
                    SpreadsheetFactory.logMemoryUsage();
                    layout.removeComponent(spreadsheet);
                    spreadsheet = null;
                    SpreadsheetFactory.logMemoryUsage();
                }
            }
        }));
        options.addComponent(new Button("Reset from data", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (spreadsheet != null) {
                    spreadsheet.setWorkbook(spreadsheet.getWorkbook());
                }
            }
        }));
        HorizontalLayout sheetOptions = new HorizontalLayout();
        sheetOptions.setSpacing(true);
        sheetOptions.addComponent(save);
        sheetOptions.addComponent(download);
        layout.addComponent(options);

        upload.addSucceededListener(new SucceededListener() {

            @Override
            public void uploadSucceeded(SucceededEvent event) {
                loadFile(uploadedFile);
            }
        });

        selectionChangeListener = new SelectionChangeListener() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                printSelectionChangeEventContents(event);
            }
        };

        selectedSheetChangeListener = new SelectedSheetChangeListener() {
            // private int counter = 0;

            @Override
            public void onSelectedSheetChange(SelectedSheetChangeEvent event) {
                gridlines.setValue(event.getNewSheet().isDisplayGridlines());
                rowColHeadings.setValue(event.getNewSheet()
                        .isDisplayRowColHeadings());

                // Workbook workbook = ((Spreadsheet) event.getComponent())
                // .getWorkbook();
                // c.removeAllItems();
                // for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                // c.addItem(i);
                // }

                // System.out.println("SELECTED SHEET CHANGED: #" + counter++);
                // System.out.println("Previous sheet:"
                // + event.getPreviousSheet().getSheetName()
                // + ", New Sheet: " + event.getNewSheet().getSheetName()
                // + " index: " + event.getNewSheetVisibleIndex()
                // + " POIIndex: " + event.getNewSheetPOIIndex());
            }
        };

        spreadsheetFieldFactory = new SpreadsheetEditorComponentFactoryTest();

    }

    private void printSelectionChangeEventContents(SelectionChangeEvent event) {

        Set<String> cells = new HashSet<String>();

        for (CellReference r : event.getIndividualSelectedCells()) {
            cells.add(r.formatAsString());
        }
        cells.add(event.getSelectedCellReference().formatAsString());

        if (event.getCellRangeAddresses() != null) {
            for (CellRangeAddress a : event.getCellRangeAddresses()) {

                for (int x = a.getFirstColumn(); x <= a.getLastColumn(); x++)
                    for (int y = a.getFirstRow(); y <= a.getLastRow(); y++)
                        cells.add(new CellReference(y, x).formatAsString());
            }
        }

        spreadsheet.setInfoLabelValue(cells.size() + " selected cells");

        // System.out.println(event.getSelectedCellReference().toString());
        // System.out.println("Merged region: "
        // + event.getSelectedCellMergedRegion());
        // System.out.println("Ranges:");
        // for (CellRangeAddress range : event.getCellRangeAddresses()) {
        // System.out.println(range.toString());
        // }
        // System.out.println("Individual Cells:");
        // for (CellReference cell : event.getIndividualSelectedCells()) {
        // System.out.println(cell.toString());
        // }
    }

    private void loadFile(File file) {
        try {
            if (spreadsheet == null) {
                spreadsheet = new Spreadsheet(file);
                spreadsheet.addSelectionChangeListener(selectionChangeListener);
                spreadsheet
                        .addSelectedSheetChangeListener(selectedSheetChangeListener);
                spreadsheet.addActionHandler(spreadsheetActionHandler);
                spreadsheet
                        .addProtectedCellWriteAttemptedListener(new ProtectedCellWriteAttemptedListener() {

                            @Override
                            public void writeAttempted(
                                    ProtectedCellWriteAttemptedEvent event) {
                                Notification
                                        .show("This cell is protected and cannot be changed");
                            }
                        });
                layout.addComponent(spreadsheet);
                layout.setRowExpandRatio(1, 1.0F);
            } else {
                if (previousFile == null
                        || !previousFile.getAbsolutePath().equals(
                                file.getAbsolutePath())) {
                    spreadsheet.reloadSpreadsheetFrom(file);
                }
            }
            spreadsheet.setSpreadsheetComponentFactory(null);
            previousFile = file;
            save.setEnabled(true);
            download.setEnabled(false);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public OutputStream receiveUpload(final String filename, String mimeType) {

        try {
            File file = new File(filename);
            file.deleteOnExit();
            uploadedFile = file;
            FileOutputStream fos = new FileOutputStream(uploadedFile);
            return fos;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    class SpreadsheetEditorComponentFactoryTest implements
            SpreadsheetComponentFactory {

        private int counter = 0;

        private final DateField dateField = new DateField();

        private final CheckBox checkBox = new CheckBox();

        private final Workbook testWorkbook;

        private final String[] comboBoxValues = { "Value 1", "Value 2",
                "Value 3" };

        private final Object[][] data = {
                { "Testing custom editors", "Boolean", "Date", "Numeric",
                        "Button", "ComboBox" },
                { "nulls:", false, null, 0, null, null },
                { "", true, new Date(), 5, "here is a button",
                        comboBoxValues[0] },
                { "", true, Calendar.getInstance(), 500.0D,
                        "here is another button", comboBoxValues[1] } };

        private final ComboBox comboBox;

        private Button button;

        private Button button2;

        private Button button3;

        private Button button4;

        private Button button5;

        private boolean hidden = false;

        private NativeSelect nativeSelect;

        private ComboBox comboBox2;

        public SpreadsheetEditorComponentFactoryTest() {
            testWorkbook = new XSSFWorkbook();
            final Sheet sheet = getTestWorkbook().createSheet(
                    "Custom Components");
            Row lastRow = sheet.createRow(100);
            lastRow.createCell(100, Cell.CELL_TYPE_BOOLEAN).setCellValue(true);
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 6000);
            sheet.setColumnWidth(3, 6000);
            sheet.setColumnWidth(4, 6000);
            sheet.setColumnWidth(5, 6000);

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i);
                row.setHeightInPoints(25F);
                for (int j = 0; j < data[0].length; j++) {
                    Cell cell = row.createCell(j);
                    Object value = data[i][j];
                    if (i == 0 || j == 0 || j == 4 || j == 5) {
                        // string cells
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                    } else if (j == 2 || j == 3) {
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    }
                    final DataFormat format = getTestWorkbook()
                            .createDataFormat();
                    if (value != null) {
                        if (value instanceof String) {
                            cell.setCellValue((String) value);
                        } else if (value instanceof Double) {
                            cell.setCellValue((Double) value);
                            CellStyle style = sheet.getWorkbook()
                                    .createCellStyle();
                            style.setDataFormat(format.getFormat("0000.0"));
                            cell.setCellStyle(style);
                        } else if (value instanceof Integer) {
                            cell.setCellValue(((Integer) value).intValue());
                            CellStyle style = sheet.getWorkbook()
                                    .createCellStyle();
                            style.setDataFormat(format.getFormat("0.0"));
                            cell.setCellStyle(style);
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else if (value instanceof Date) {
                            cell.setCellValue((Date) value);
                            CellStyle dateStyle = sheet.getWorkbook()
                                    .createCellStyle();
                            dateStyle.setDataFormat(format
                                    .getFormat("m/d/yy h:mm"));
                            cell.setCellStyle(dateStyle);
                        } else if (value instanceof Calendar) {
                            cell.setCellValue((Calendar) value);
                            CellStyle dateStyle = sheet.getWorkbook()
                                    .createCellStyle();
                            dateStyle.setDataFormat(format
                                    .getFormat("d m yyyy"));
                            cell.setCellStyle(dateStyle);
                        }
                    } // null sells don't get a value
                }
            }
            Row row5 = sheet.createRow(5);
            row5.setHeightInPoints(20F);
            row5.createCell(0).setCellValue(
                    "This cell has a value, and a component (label)");
            row5.createCell(1).setCellValue(
                    "This cell has a value, and a button");
            Cell cell2 = row5.createCell(2);
            cell2.setCellValue("This cell has a value and button, and is locked.");
            CellStyle lockedCellStyle = sheet.getWorkbook().createCellStyle();
            lockedCellStyle.setLocked(true);
            cell2.setCellStyle(lockedCellStyle);
            Row row6 = sheet.createRow(6);
            row6.setHeightInPoints(22F);
            comboBox = new ComboBox();
            for (String s : comboBoxValues) {
                comboBox.addItem(s);
            }
            comboBox.setImmediate(true);
            comboBox.setBuffered(false);
            comboBox.addValueChangeListener(new ValueChangeListener() {

                @Override
                public void valueChange(ValueChangeEvent event) {
                    String s = (String) comboBox.getValue();
                    CellReference cr = spreadsheet.getSelectedCellReference();
                    Cell cell = spreadsheet.getCell(cr.getRow(), cr.getCol());
                    if (cell != null) {
                        cell.setCellValue(s);
                        spreadsheet.markCellAsUpdated(cell, false);
                        spreadsheet.updateMarkedCells();
                    }
                }
            });
            comboBox.setWidth("100%");
            // comboBox.setWidth("100px");

            dateField.setImmediate(true);
            dateField.addValueChangeListener(new ValueChangeListener() {

                @Override
                public void valueChange(ValueChangeEvent event) {
                    CellReference selectedCellReference = spreadsheet
                            .getSelectedCellReference();
                    Cell cell = spreadsheet.getCell(
                            selectedCellReference.getRow(),
                            selectedCellReference.getCol());
                    try {
                        Date oldValue = cell.getDateCellValue();
                        Date value = dateField.getValue();
                        if (oldValue != null && !oldValue.equals(value)) {
                            cell.setCellValue(value);
                            spreadsheet.markCellAsUpdated(cell, false);
                            spreadsheet.updateMarkedCells();
                        }
                    } catch (IllegalStateException ise) {
                        ise.printStackTrace();
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
            });
            checkBox.setImmediate(true);
            checkBox.addValueChangeListener(new ValueChangeListener() {

                @Override
                public void valueChange(ValueChangeEvent event) {
                    CellReference selectedCellReference = spreadsheet
                            .getSelectedCellReference();
                    Cell cell = spreadsheet.getCell(
                            selectedCellReference.getRow(),
                            selectedCellReference.getCol());
                    try {
                        Boolean value = checkBox.getValue();
                        Boolean oldValue = cell.getBooleanCellValue();
                        if (value != oldValue) {
                            cell.setCellValue(value);
                            spreadsheet.markCellAsUpdated(cell, false);
                            spreadsheet.updateMarkedCells();
                        }
                    } catch (IllegalStateException ise) {
                        ise.printStackTrace();
                    }
                }
            });
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (spreadsheet.getActiveSheetIndex() == 0) {
                if (rowIndex == 0 || rowIndex > 3) {
                    return null;
                }
                if (1 == columnIndex) { // boolean
                    return checkBox;
                } else if (2 == columnIndex) { // date
                    return dateField;
                } else if (3 == columnIndex) { // numeric
                    return null;
                } else if (4 == columnIndex) { // button
                    return new Button("Button " + (++counter),
                            new Button.ClickListener() {

                                @Override
                                public void buttonClick(ClickEvent event) {
                                    Notification
                                            .show("Clicked button inside sheet");
                                }
                            });
                } else if (5 == columnIndex) { // combobox
                    return comboBox;
                }
            }
            return null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (customEditor instanceof Button) {
                if (rowIndex == 3) {
                    customEditor.setWidth("100%");
                } else {
                    customEditor.setWidth("100px");
                    customEditor.setCaption("Col " + columnIndex + " Row "
                            + rowIndex);
                }
                return;
            }
            if (customEditor.equals(comboBox)) {

                String stringCellValue = cell != null ? cell
                        .getStringCellValue() : null;
                comboBox.setValue(stringCellValue);
            }

            if (cell != null) {
                if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                    ((CheckBox) customEditor).setValue(cell
                            .getBooleanCellValue());
                } else if (customEditor instanceof DateField) {
                    final String s = cell.getCellStyle().getDataFormatString();
                    if (s.contains("ss")) {
                        ((DateField) customEditor)
                                .setResolution(Resolution.SECOND);
                    } else if (s.contains("mm")) {
                        ((DateField) customEditor)
                                .setResolution(Resolution.MINUTE);
                    } else if (s.contains("h")) {
                        ((DateField) customEditor)
                                .setResolution(Resolution.HOUR);
                    } else if (s.contains("d")) {
                        ((DateField) customEditor)
                                .setResolution(Resolution.DAY);
                    } else if (s.contains("m") || s.contains("mmm")) {
                        ((DateField) customEditor)
                                .setResolution(Resolution.MONTH);
                    } else {
                        ((DateField) customEditor)
                                .setResolution(Resolution.YEAR);
                    }
                    ((DateField) customEditor)
                            .setValue(cell.getDateCellValue());
                    Format format = spreadsheet.getDataFormatter()
                            .createFormat(cell);
                    String pattern = null;
                    if (format instanceof ExcelStyleDateFormatter) {
                        pattern = ((ExcelStyleDateFormatter) format)
                                .toLocalizedPattern();
                    }
                    try {
                        ((DateField) customEditor).setDateFormat(pattern);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public Component getCustomComponentForCell(Cell cell,
                final int rowIndex, final int columnIndex,
                final Spreadsheet spreadsheet, final Sheet sheet) {

            if (rowIndex == 5) {
                if (!hidden) {
                    if (columnIndex == 0) {
                        Label label = new Label(
                                "<div style=\"text-overflow: ellipsis; font-size: 15pt;"
                                        + "overflow: hidden; white-space: nowrap;\">Custom"
                                        + "Components in this row.</div>",
                                ContentMode.HTML);
                        return label;
                    }
                    if (columnIndex == 1) {
                        if (button == null) {
                            button = new Button("CLICKME",
                                    new Button.ClickListener() {

                                        @Override
                                        public void buttonClick(ClickEvent event) {
                                            Notification
                                                    .show("Clicked button at row index "
                                                            + rowIndex
                                                            + " column index "
                                                            + columnIndex);
                                        }
                                    });
                            button.setWidth("100%");
                        }
                        return button;
                    }
                    if (columnIndex == 2) {
                        if (button3 == null) {
                            button3 = new Button("Hide/Show rows 1-4",
                                    new Button.ClickListener() {

                                        @Override
                                        public void buttonClick(ClickEvent event) {
                                            boolean hidden = !sheet.getRow(0)
                                                    .getZeroHeight();
                                            spreadsheet.setRowHidden(0, hidden);
                                            spreadsheet.setRowHidden(1, hidden);
                                            spreadsheet.setRowHidden(2, hidden);
                                            spreadsheet.setRowHidden(3, hidden);
                                        }
                                    });
                        }
                        return button3;
                    }
                    if (columnIndex == 3) {
                        if (button2 == null) {
                            button2 = new Button("Hide/Show Columns F-I",
                                    new Button.ClickListener() {

                                        @Override
                                        public void buttonClick(ClickEvent event) {
                                            boolean hidden = !sheet
                                                    .isColumnHidden(5);
                                            spreadsheet.setColumnHidden(5,
                                                    hidden);
                                            spreadsheet.setColumnHidden(6,
                                                    hidden);
                                            spreadsheet.setColumnHidden(7,
                                                    hidden);
                                            spreadsheet.setColumnHidden(8,
                                                    hidden);
                                        }
                                    });
                        }
                        return button2;
                    }
                    if (columnIndex == 4) {
                        if (button4 == null) {
                            button4 = new Button("Lock/Unlock sheet",
                                    new Button.ClickListener() {

                                        @Override
                                        public void buttonClick(ClickEvent event) {
                                            if (spreadsheet.getActiveSheet()
                                                    .getProtect()) {
                                                spreadsheet
                                                        .setActiveSheetProtected(null);
                                            } else {
                                                spreadsheet
                                                        .setActiveSheetProtected("");
                                            }
                                        }
                                    });
                        }
                        return button4;
                    }
                }
                if (columnIndex == 5) {
                    if (button5 == null) {
                        button5 = new Button("Hide all custom components",
                                new Button.ClickListener() {

                                    @Override
                                    public void buttonClick(ClickEvent event) {
                                        hidden = !hidden;
                                        spreadsheet.reloadVisibleCellContents();
                                    }
                                });
                    }
                    return button5;
                }
            } else if (!hidden && rowIndex == 6) {
                if (columnIndex == 1) {
                    if (nativeSelect == null) {
                        nativeSelect = new NativeSelect();
                        nativeSelect.addItem("JEE");
                        nativeSelect.setWidth("100%");
                    }
                    return nativeSelect;
                } else if (columnIndex == 2) {
                    if (comboBox2 == null) {
                        comboBox2 = new ComboBox();
                        for (String s : comboBoxValues) {
                            comboBox2.addItem(s);
                        }
                        comboBox2.setWidth("100%");
                    }
                    return comboBox2;
                }
            }
            return null;
        }

        /**
         * @return the testWorkbook
         */
        public Workbook getTestWorkbook() {
            return testWorkbook;
        }

    }
}
