package com.vaadin.addon.spreadsheet.test.demoapps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Format;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SheetChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.SheetChangeListener;
import com.vaadin.addon.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.addon.spreadsheet.SpreadsheetFactory;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("demo")
@Widgetset("com.vaadin.addon.spreadsheet.Widgetset")
public class SpreadsheetDemoUI extends UI implements Receiver {
    private static final Logger LOGGER = Logger
            .getLogger(SpreadsheetDemoUI.class.getName());
    VerticalLayout layout = new VerticalLayout();

    Spreadsheet spreadsheet;

    private File previousFile = null;

    private Button save;

    private Button download;

    private ComboBox<File> openTestSheetSelect;
    private SpreadsheetComponentFactory spreadsheetFieldFactory;

    private SheetChangeListener selectedSheetChangeListener;

    private Button updateButton;

    private CheckBox gridlines, hideTop, hideBottom, hideBoth;

    private AbstractField<Boolean> rowColHeadings;

    private Upload upload = new Upload(null, this);

    private File uploadedFile;

    private HorizontalLayout options;

    private NativeSelect<Locale> localeSelect;
    private Button loadFixtureBtn;
    private NativeSelect<TestFixtures> fixtureSelect;

    public SpreadsheetDemoUI() {
        super();
        SpreadsheetFactory.logMemoryUsage();
    }

    protected HorizontalLayout getOptionsLayout() {
        return options;
    }

    @Override
    protected void init(VaadinRequest request) {
        SpreadsheetFactory.logMemoryUsage();
        setContent(layout);
        layout.setMargin(true);
        layout.setSpacing(false);
        layout.setSizeFull();

        options = new HorizontalLayout();
        options.setSpacing(true);
        rowColHeadings=createRowHeadings();

        Button newSpreadsheetButton = createNewButton();


        ClassLoader classLoader = SpreadsheetDemoUI.class.getClassLoader();
        URL resource = classLoader.getResource("test_sheets" + File.separator);
        URI uri = null;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (uri != null) {
            String excelFilesRegex = ".*\\.xls|.*\\.xlsx|.*\\.xlsm";

            openTestSheetSelect = createTestSheetCombobox(
                    FileDataProvider.getFiles(uri, excelFilesRegex, LOGGER));
        }
        updateButton = createUpdateButton();
        save = createSaveButton();

        download = new Button("Download");
        download.setEnabled(false);

        gridlines =createCBNewLines();
        Button customComponentTest = new Button(
                "Create Custom Editor Test sheet", new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (spreadsheet == null) {
                            spreadsheet = new Spreadsheet(
                                    ((SpreadsheetEditorComponentFactoryTest) spreadsheetFieldFactory)
                                            .getTestWorkbook());
                            updateLocale();
                            spreadsheet
                                    .setSpreadsheetComponentFactory(spreadsheetFieldFactory);
                            layout.addComponent(spreadsheet);
                            layout.setExpandRatio(spreadsheet, 1.0F);
                        } else {
                            spreadsheet
                                    .setWorkbook(((SpreadsheetEditorComponentFactoryTest) spreadsheetFieldFactory)
                                            .getTestWorkbook());
                            spreadsheet
                                    .setSpreadsheetComponentFactory(spreadsheetFieldFactory);
                        }
                        gridlines.setValue(spreadsheet.isGridlinesVisible());
                        rowColHeadings.setValue(spreadsheet
                                .isRowColHeadingsVisible());
                    }
                });

        upload.addSucceededListener(new SucceededListener() {

            @Override
            public void uploadSucceeded(SucceededEvent event) {
                loadFile(uploadedFile);
            }
        });

        VerticalLayout checkBoxLayout = new VerticalLayout();
        checkBoxLayout.setMargin(false);
        checkBoxLayout.setSpacing(false);

        Button freezePanesButton = new Button("Freeze Pane",
                e->addWindow(new FreezePaneWindow()));

        hideTop = new CheckBox("toggle top bar visibility");
        hideBottom = new CheckBox("toggle bottom bar visibility");
        hideBoth = new CheckBox("report mode");

        hideTop.addValueChangeListener(event -> {
            spreadsheet.setFunctionBarVisible(!hideTop.getValue());
            hideBoth.setValue(spreadsheet.isReportStyle());
        });
        hideBottom.addValueChangeListener(event1 ->  {
                spreadsheet.setSheetSelectionBarVisible(!hideBottom.getValue());
                hideBoth.setValue(spreadsheet.isReportStyle());
        });

        hideBoth.addValueChangeListener(event1 -> {
                spreadsheet.setReportStyle(hideBoth.getValue());
                hideTop.setValue(!spreadsheet.isFunctionBarVisible());
                hideBottom.setValue(!spreadsheet.isFunctionBarVisible());
        });

        checkBoxLayout.addComponents(gridlines, rowColHeadings, hideTop,
                hideBottom, hideBoth);

        Button closeButton = new Button("Close", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (spreadsheet != null) {
                    SpreadsheetFactory.logMemoryUsage();
                    layout.removeComponent(spreadsheet);
                    spreadsheet = null;
                    SpreadsheetFactory.logMemoryUsage();
                }
            }
        });

        Button downloadButton = new Button("Download");
        new FileDownloader(new StreamResource(new StreamSource() {

            @Override
            public InputStream getStream() {
                try {
                    return new FileInputStream(
                            spreadsheet.write("testsheet.xlsx"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, "testsheet.xlsx")).extend(downloadButton);

        localeSelect = new NativeSelect<>();
        localeSelect.setWidth("200px");
        localeSelect.setId("localeSelect");

        final List<Locale> locales = Arrays
                .asList(Locale.getAvailableLocales());
        Collections.sort(locales, new Comparator<Locale>() {
            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        localeSelect.setItems(locales);
        localeSelect.setItemCaptionGenerator(Locale::getDisplayName);
        localeSelect.addValueChangeListener(e-> updateLocale());

        HorizontalLayout sheetOptions = new HorizontalLayout();
        sheetOptions.setSpacing(true);
        sheetOptions.addComponent(save);
        sheetOptions.addComponent(download);
        layout.addComponent(options);

        selectedSheetChangeListener = new SheetChangeListener() {
            @Override
            public void onSheetChange(SheetChangeEvent event) {
                gridlines.setValue(spreadsheet.isGridlinesVisible());
                rowColHeadings.setValue(spreadsheet.isRowColHeadingsVisible());
            }
        };

        spreadsheetFieldFactory = new SpreadsheetEditorComponentFactoryTest();

        fixtureSelect = new NativeSelect<>();
        fixtureSelect.setId("fixtureSelect");
        fixtureSelect.setItems(TestFixtures.values());

        loadFixtureBtn = new Button("Load");
        loadFixtureBtn.addClickListener(event -> {
            if (spreadsheet == null) {
                return;
            }

            TestFixtures fixture = fixtureSelect.getValue();
            fixture.factory.create().loadFixture(spreadsheet);
        });

        loadFixtureBtn.setId("loadFixtureBtn");

        HorizontalLayout loadFixture = new HorizontalLayout(fixtureSelect,
                loadFixtureBtn);
        loadFixture.setSpacing(false);
        loadFixture.setComponentAlignment(loadFixtureBtn,
                Alignment.BOTTOM_CENTER);

        VerticalLayout createAndFreeze = new VerticalLayout();
        createAndFreeze.setSpacing(true);
        createAndFreeze.setMargin(false);
        createAndFreeze.addComponents(newSpreadsheetButton,
                customComponentTest, freezePanesButton);

        HorizontalLayout updateLayout = new HorizontalLayout();
        updateLayout.setSpacing(false);
        updateLayout.addComponents(openTestSheetSelect, updateButton);
        VerticalLayout updateUpload = new VerticalLayout();
        updateUpload.setMargin(false);
        updateUpload.setSpacing(true);
        updateUpload.addComponents(updateLayout, upload, localeSelect);

        VerticalLayout closeDownload = new VerticalLayout();
        closeDownload.setMargin(false);
        closeDownload.setSpacing(true);
        closeDownload.addComponents(closeButton, downloadButton, loadFixture);

        checkBoxLayout.setWidth(null);
        createAndFreeze.setWidth(null);
        updateUpload.setWidth(null);
        closeDownload.setWidth(null);
        options.addComponent(checkBoxLayout);
        options.addComponent(createAndFreeze);
        options.addComponent(updateUpload);
        options.addComponent(closeDownload);

        getPage().addUriFragmentChangedListener(e -> {
                updateFromFragment();
        });

        updateFromFragment();

    }

    private Button createSaveButton() {
        Button save = new Button("Save", new Button.ClickListener() {

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
                            previousFile = spreadsheet.write(fileName);
                        } else {
                            previousFile = spreadsheet.write("workbook1");
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
        return save;
    }

    private Button createUpdateButton() {
        Button updateButton = new Button("Update");
        updateButton.addClickListener(e -> {
            File file = openTestSheetSelect.getValue();
            if (file != null ) {
                loadFile(file);
                Page.getCurrent().setUriFragment("file/" + file.getName(), false);
            }
        });
        updateButton.setId("update");
        return updateButton;
    }

    private ComboBox<File> createTestSheetCombobox(List<File> files) {
        ComboBox<File> cb = new ComboBox<>(null);

        cb.setItems(files);
        cb.setItemCaptionGenerator(File::getName);
        cb.setId("testSheetSelect");
        cb.setPageLength(30);
        cb.setWidth("250px");
        return cb;
    }

    private Button createNewButton() {
        return new Button("Create new",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (spreadsheet == null) {
                            spreadsheet = new Spreadsheet();
                            updateLocale();
                            spreadsheet
                                    .addSheetChangeListener(selectedSheetChangeListener);
                            layout.addComponent(spreadsheet);
                            layout.setExpandRatio(spreadsheet, 1.0f);
                        } else {
                            spreadsheet.reset();
                        }
                        spreadsheet.setSpreadsheetComponentFactory(null);
                        save.setEnabled(true);
                        previousFile = null;
                        openTestSheetSelect.setValue(null);
                        gridlines.setValue(spreadsheet.isGridlinesVisible());
                        rowColHeadings.setValue(spreadsheet
                                .isRowColHeadingsVisible());

                        Page.getCurrent().setUriFragment(null, false);
                    }
                });
    }

    private CheckBox createRowHeadings() {
        CheckBox rowColHeadings = new CheckBox("display row and column headers");
        rowColHeadings.addValueChangeListener(event ->{
                if (spreadsheet != null) {
                    spreadsheet.setRowColHeadingsVisible(event.getValue());
                }
        });
        return rowColHeadings;
    }

    private CheckBox createCBNewLines() {
        CheckBox cb = new CheckBox("display grid lines");
        cb.addValueChangeListener(event -> {
            if (spreadsheet != null) {
                spreadsheet.setGridlinesVisible(event.getValue());
            }
        });
        return cb;
    }

    /*
     * Rudimentary fragment handling to make developing&testing faster
     */
    private void updateFromFragment() {
        String uriFragment = getPage().getUriFragment();
        if (uriFragment != null && uriFragment.startsWith("file/")) {
            String filename = null;
            Integer sheetIndex = null;
            TestFixtures fixture = null;

            // #file/<filename>/sheet/<sheetIndex>/fixture/<fixturename>

            String[] tokens = uriFragment.split("/");
            for (int i = 0; i < tokens.length; i++) {
                if ("file".equals(tokens[i])) {
                    filename = tokens[i + 1];
                    System.out.println("Opening file " + filename);
                } else if ("sheet".equals(tokens[i])) {
                    sheetIndex = Integer.valueOf(tokens[i + 1]) - 1;
                    System.out.println("Opening sheet " + sheetIndex);
                } else if ("fixture".equals(tokens[i])) {
                    fixture = TestFixtures.valueOf(tokens[i + 1]);
                    System.out.println("Opening fixture " + fixture);
                }
            }
            open(filename,fixture,sheetIndex);
        }
    }

    private void open(String filename, TestFixtures fixture,
            Integer sheetIndex) {
        Optional<File> file = openTestSheetSelect.getDataProvider()
                .fetch(new Query<>()).filter(f -> f.getName().equals(filename))
                .findFirst();
        if (file.isPresent()) {
            openTestSheetSelect.setValue(file.get());
            updateButton.click();

            if (sheetIndex != null) {
                spreadsheet.setActiveSheetIndex(sheetIndex);
            }

            if (fixture != null) {
                fixtureSelect.setValue(fixture);
                loadFixtureBtn.click();
            }
        } else {
            Notification.show("File not found: " + filename,
                    Notification.Type.WARNING_MESSAGE);
        }
    }

    private void loadFile(File file) {
        try {
            if (spreadsheet == null) {
                spreadsheet = new Spreadsheet(file);
                spreadsheet.addSheetChangeListener(selectedSheetChangeListener);
                layout.addComponent(spreadsheet);
                layout.setExpandRatio(spreadsheet, 1.0f);
            } else {
                if (previousFile == null
                        || !previousFile.getAbsolutePath().equals(
                                file.getAbsolutePath())) {
                    spreadsheet.read(file);
                }
            }
            updateLocale();
            spreadsheet.setSpreadsheetComponentFactory(null);
            previousFile = file;
            save.setEnabled(true);
            download.setEnabled(false);
            gridlines.setValue(spreadsheet.isGridlinesVisible());
            rowColHeadings.setValue(spreadsheet.isRowColHeadingsVisible());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateLocale() {
        if (spreadsheet != null && localeSelect.getValue() instanceof Locale) {
            spreadsheet.setLocale(localeSelect.getValue());
        }
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

        private final ComboBox<String> comboBox;

        private boolean initializingComboBoxValue;

        private Button button;

        private Button button2;

        private Button button3;

        private Button button4;

        private Button button5;

        private boolean hidden = false;
        private NativeSelect<String> nativeSelect;

        private ComboBox<String> comboBox2;

        private NativeSelect<String> createNativeSelect() {
            if (nativeSelect == null) {
                List<String> items = new ArrayList<>();
                items.add("JEE");
                nativeSelect = new NativeSelect<>();
                nativeSelect.setDataProvider(new ListDataProvider<>(items));
                nativeSelect.setWidth("100%");
            }
            return nativeSelect;
        }

        private ComboBox<String> createCombobox() {
            final ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(comboBoxValues);
            comboBox.addValueChangeListener(e -> {
                if (!initializingComboBoxValue) {
                    CellReference cr = spreadsheet
                            .getSelectedCellReference();
                    Cell cell = spreadsheet.getCell(cr.getRow(),
                            cr.getCol());
                    if (cell != null) {
                        cell.setCellValue(comboBox.getValue());
                        spreadsheet.refreshCells(cell);
                    }
                }
            });
            comboBox.setWidth("100%");
            return comboBox;

        }

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

            comboBox = createCombobox();

            dateField.addValueChangeListener(event ->{

                    CellReference selectedCellReference = spreadsheet
                            .getSelectedCellReference();
                    Cell cell = spreadsheet.getCell(
                            selectedCellReference.getRow(),
                            selectedCellReference.getCol());
                    try {
                        Date oldValue = cell.getDateCellValue();
                        Date value = Date.from(dateField.getValue().atStartOfDay().toInstant(ZoneOffset.UTC));
                        if (oldValue != null && !oldValue.equals(value)) {
                            cell.setCellValue(value);
                            spreadsheet.refreshCells(cell);
                        }
                    } catch (IllegalStateException ise) {
                        ise.printStackTrace();
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
            });
            checkBox.addValueChangeListener(event -> {
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
                        spreadsheet.refreshCells(cell);
                    }
                } catch (IllegalStateException ise) {
                    ise.printStackTrace();
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
                    customEditor.setWidth("110px");
                    customEditor.setCaption("Col " + columnIndex + " Row "
                            + rowIndex);
                }
                return;
            }
            if (customEditor.equals(comboBox)) {
                initializingComboBoxValue = true;
                String stringCellValue = cell != null ? cell
                        .getStringCellValue() : null;
                comboBox.setValue(stringCellValue);
                comboBox.setWidth("100%");
                initializingComboBoxValue = false;
            }

            if (cell != null) {
                if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                    ((CheckBox) customEditor).setValue(cell
                            .getBooleanCellValue());
                } else if (customEditor instanceof DateField) {
                    final String s = cell.getCellStyle().getDataFormatString();

                    if (s.contains("d")) {
                        ((DateField) customEditor)
                                .setResolution(DateResolution.DAY);
                    } else if (s.contains("m") || s.contains("mmm")) {
                        ((DateField) customEditor)
                                .setResolution(DateResolution.MONTH);
                    } else {
                        ((DateField) customEditor)
                                .setResolution(DateResolution.YEAR);
                    }

                    LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    ((DateField) customEditor).setValue(date);
                    ((DateField) customEditor).setWidth("100%");
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
                                e -> {
                                    hidden = !hidden;
                                    spreadsheet.reloadVisibleCellContents();
                                });
                    }
                    return button5;
                }
            } else if (!hidden && rowIndex == 6) {
                if (columnIndex == 1) {
                    return createNativeSelect();
                } else if (columnIndex == 2) {
                    if (comboBox2 == null) {
                        comboBox2 = createCombobox();
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

    class FreezePaneWindow extends Window {

        public FreezePaneWindow() {
            setCaption("Add/Remove freeze pane");
            setWidth("300px");
            setHeight("300px");
            setResizable(false);
            setModal(true);
            center();

            VerticalLayout l = new VerticalLayout();
            l.setSpacing(false);
            l.setMargin(false);
            setContent(l);

            final TextField hSplitTF = new TextField(
                    "Horizontal Split Position");
            hSplitTF.setValue("6");
            final TextField vSplitTF = new TextField("Vertical Split Position");
            vSplitTF.setValue("6");
            l.addComponent(vSplitTF);
            l.addComponent(hSplitTF);
            Button button = new Button("Submit values");
            button.addClickListener(event -> {
                try {
                    int vSprlit = Integer.parseInt(vSplitTF.getValue());
                    int hSprlit = Integer.parseInt(hSplitTF.getValue());

                    spreadsheet.createFreezePane(vSprlit, hSprlit);
                } catch (NumberFormatException e) {

                }

                close();
            });
            l.addComponent(button);

        }
    }
}
