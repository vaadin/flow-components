package com.vaadin.addon.spreadsheet.test.demoapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeListener;
import com.vaadin.addon.spreadsheet.test.fixtures.ActionFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.CellMergeFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.ClassFixtureFactory;
import com.vaadin.addon.spreadsheet.test.fixtures.ColumnToggleFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.CommentFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.CustomComponentFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.FormatsFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.HyperLinkFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.LockCellFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.PopupButtonFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.RowToggleFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.SheetsFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.ShiftFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.SpreadsheetFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.SpreadsheetFixtureFactory;
import com.vaadin.addon.spreadsheet.test.fixtures.StylesFixture;
import com.vaadin.addon.spreadsheet.test.fixtures.ValueHandlerFixture;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
public class TestexcelsheetUI extends UI {

    private Spreadsheet spreadsheet;

    private Button update;
    private TextField rowBufferSizeField;
    private TextField columnBufferSizeField;
    private File previousFile = null;
    private Button save;
    private Button download;
    VerticalLayout layout = new VerticalLayout();
    final public List<CellReference> currentSelection = new ArrayList<CellReference>();
    public CellReference selectedCells;

    private HashMap<String, SpreadsheetFixtureFactory> fixtureFactories = new HashMap<String, SpreadsheetFixtureFactory>() {
        {
            put("FORMATS", new ClassFixtureFactory(FormatsFixture.class));
            put("STYLES", new ClassFixtureFactory(StylesFixture.class));
            put("SELECTION", new SelectionFixtureFactory());
            put("SHEETS", new ClassFixtureFactory(SheetsFixture.class));
            put("LOCK_SELECTED_CELLS", new EagerFixtureFactory(
                    new LockCellFixture(TestexcelsheetUI.this)));
            put("TOGGLE_COLUMNS", new EagerFixtureFactory(
                    new ColumnToggleFixture(TestexcelsheetUI.this)));
            put("TOGGLE_ROWS", new EagerFixtureFactory(new RowToggleFixture(
                    TestexcelsheetUI.this)));
            put("SHEET_RENAME_1", new ClassFixtureFactory(
                    SheetsFixture.Rename1.class));
            put("CELL_VALUE_HANDLER", new ClassFixtureFactory(
                    ValueHandlerFixture.class));
            put("CUSTOM_COMPONENTS", new ClassFixtureFactory(
                    CustomComponentFixture.class));
            put("HYPERLINKS", new ClassFixtureFactory(HyperLinkFixture.class));
            put("ACTIONS", new ClassFixtureFactory(ActionFixture.class));
            put("COMMENTS", new ClassFixtureFactory(CommentFixture.class));
            put("INSERT_ROW", new ClassFixtureFactory(
                    ShiftFixture.InsertRow.class));
            put("DELETE_ROW", new ClassFixtureFactory(
                    ShiftFixture.DeleteRow.class));
            put("MERGE_CELLS", new EagerFixtureFactory(new CellMergeFixture(
                    TestexcelsheetUI.this)));
            put("POPUPBUTTON", new EagerFixtureFactory(new PopupButtonFixture(
                    TestexcelsheetUI.this)));
        }
    };

    private ComboBox openTestSheetSelect;

    public TestexcelsheetUI() {
        super();
        layout.setId("layout");
    }

    /**
     * @return the spreadsheet
     */
    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    @Override
    protected void init(VaadinRequest request) {
        setContent(layout);

        HorizontalLayout options = new HorizontalLayout();
        options.setId("options");
        options.setSpacing(true);

        layout.setMargin(true);
        layout.setSizeFull();
        rowBufferSizeField = new TextField("Row Buffer Size:");
        rowBufferSizeField.setId("rowBufferSizeField");
        rowBufferSizeField.setImmediate(true);
        columnBufferSizeField = new TextField("Column Buffer Size:");
        columnBufferSizeField.setId("columnBufferSizeField");
        columnBufferSizeField.setImmediate(true);

        Button newSpreadsheetButton = new Button("Create new",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (getSpreadsheet() == null) {
                            spreadsheet = (new Spreadsheet());
                            getSpreadsheet().setId("spreadsheetId");

                            getSpreadsheet().addSelectionChangeListener(
                                    new SelectionChangeListener() {

                                        @Override
                                        public void onSelectionChange(
                                                SelectionChangeEvent event) {
                                            currentSelection.clear();
                                            currentSelection.add(event
                                                    .getSelectedCellReference());

                                            for (CellReference cra : event
                                                    .getIndividualSelectedCells()) {
                                                currentSelection.add(cra);
                                            }

                                            for (CellRangeAddress cra : event
                                                    .getCellRangeAddresses()) {
                                                for (int i = cra
                                                        .getFirstColumn(); i <= cra
                                                        .getLastColumn(); i++) {
                                                    for (int j = cra
                                                            .getFirstRow(); j <= cra
                                                            .getLastRow(); j++) {
                                                        currentSelection
                                                                .add(new CellReference(
                                                                        j, i));
                                                    }
                                                }
                                            }

                                            // System
                                            // .out.println(">>>>>>>>>>>>>>>>>>>>>");
                                            // System
                                            // .out.println("Cell reference:"+event.getSelectedCellReference().formatAsString());
                                            // for (CellReference cra :
                                            // event.getIndividualSelectedCells())
                                            // System
                                            // .out.println("Individual selection:"+
                                            // cra.formatAsString());
                                            //
                                            // for (CellRangeAddress cra :
                                            // event.getCellRangeAddresses())
                                            // System
                                            // .out.println("Selection addresses:"+
                                            // cra.formatAsString());
                                            //
                                            // System
                                            // .out.println("<<<<<<<<<<<<<<<<<<<<<");
                                            //
                                            // Cell c =
                                            // spreadsheet.getCell(event.getSelectedCellReference().getRow(),
                                            // event.getSelectedCellReference().getCol());
                                            // if (c == null)
                                            // return;
                                            //
                                            // System
                                            // .out.println(c.getCellType());

                                        }
                                    });
                            layout.addComponent(getSpreadsheet(), 1);
                            layout.setExpandRatio(getSpreadsheet(), 1.0f);

                            rowBufferSizeField.setValue(Integer
                                    .toString(getSpreadsheet()
                                            .getRowBufferSize()));
                            columnBufferSizeField.setValue(Integer
                                    .toString(getSpreadsheet()
                                            .getColBufferSize()));
                        }

                        previousFile = null;
                    }
                });
        newSpreadsheetButton.setId("newSpreadsheetButton");
        update = new Button("Update", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    if (getSpreadsheet() != null) {
                        Object value = openTestSheetSelect.getValue();
                        if (value != null && value instanceof File) {
                            loadFile((File) value);
                            openTestSheetSelect.setValue(null);
                        }

                        getSpreadsheet()
                                .setRowBufferSize(
                                        Integer.parseInt(rowBufferSizeField
                                                .getValue()));
                        getSpreadsheet().setColBufferSize(
                                Integer.parseInt(columnBufferSizeField
                                        .getValue()));
                    }
                } catch (NumberFormatException nfe) {
                    rowBufferSizeField.setValue(Integer
                            .toString(getSpreadsheet().getRowBufferSize()));
                    columnBufferSizeField.setValue(Integer
                            .toString(getSpreadsheet().getColBufferSize()));
                }
            }
        });
        update.setId("update");

        save = new Button("Save", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (getSpreadsheet() != null) {
                    try {
                        if (previousFile != null) {
                            int i = previousFile.getName().lastIndexOf(".xls");
                            String fileName = previousFile.getName().substring(
                                    0, i)
                                    + ("(1)")
                                    + previousFile.getName().substring(i);
                            previousFile = getSpreadsheet().write(fileName);
                        } else {
                            previousFile = getSpreadsheet().write("workbook1");
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
        save.setId("save");
        save.setEnabled(false);
        download = new Button("Downloada");
        download.setEnabled(false);
        download.setId("download");
        final ComboBox cb = new ComboBox("Type", Arrays.asList("Number",
                "String", "Date"));
        final Button changeTypeButton = new Button("Change type",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (selectedCells == null) {
                            return;
                        }

                        System.out.println("Celle selezionate: "
                                + selectedCells);
                        System.out.println("Row: " + selectedCells.getRow());
                        System.out.println("Col: " + selectedCells.getCol());
                        System.out.println("Sheet name: "
                                + selectedCells.getSheetName());
                        System.out.println("Cell ref parts: "
                                + Arrays.asList(selectedCells.getCellRefParts()));
                        System.out.println("Format as string: "
                                + selectedCells.formatAsString());
                    }
                });

        final ComboBox fixtureCombo = new ComboBox();
        fixtureCombo.setId("fixtureNameCmb");
        for (String key : fixtureFactories.keySet()) {
            fixtureCombo.addItem(key);
        }

        Button loadFixtureBtn = new Button("Load", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (getSpreadsheet() == null) {
                    return;
                }

                String fixtureName = (String) fixtureCombo.getValue();
                SpreadsheetFixtureFactory factory = fixtureFactories
                        .get(fixtureName);

                if (factory == null) {
                    return;
                }

                factory.create().loadFixture(getSpreadsheet());
            }
        });

        loadFixtureBtn.setId("loadFixtureBtn");

        File file = null;
        try {
            ClassLoader classLoader = SpreadsheetDemoUI.class.getClassLoader();
            URL resource = classLoader.getResource("test_sheets"
                    + File.separator);
            file = new File(resource.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        FilesystemContainer testSheetContainer = new FilesystemContainer(file);
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

        openTestSheetSelect = new ComboBox(null, testSheetContainer);
        openTestSheetSelect.setId("testSheetSelect");
        openTestSheetSelect.setImmediate(true);
        openTestSheetSelect.setItemCaptionPropertyId("Name");

        cb.setId("cb");
        options.addComponent(newSpreadsheetButton);
        options.addComponent(fixtureCombo);
        options.addComponent(loadFixtureBtn);
        options.addComponent(openTestSheetSelect);
        options.addComponent(rowBufferSizeField);
        options.addComponent(columnBufferSizeField);
        options.addComponent(update);
        options.addComponent(save);
        options.addComponent(download);
        options.addComponent(changeTypeButton);
        options.addComponent(cb);
        layout.addComponent(options);
    }

    protected void loadFile(File file) {
        try {
            if (getSpreadsheet() == null) {
                spreadsheet = new Spreadsheet(file);
                getSpreadsheet().setId("spreadsheetId");
                rowBufferSizeField.setValue(Integer.toString(getSpreadsheet()
                        .getRowBufferSize()));
                columnBufferSizeField.setValue(Integer
                        .toString(getSpreadsheet().getColBufferSize()));
                layout.addComponent(getSpreadsheet());
                layout.setExpandRatio(getSpreadsheet(), 1.0f);
            } else {
                if (previousFile == null
                        || !previousFile.getAbsolutePath().equals(
                                file.getAbsolutePath())) {
                    getSpreadsheet().read(file);
                }
            }
            previousFile = file;
            previousFile.deleteOnExit();
            save.setEnabled(true);
            download.setEnabled(false);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class SelectionFixture implements SpreadsheetFixture {
        @Override
        public void loadFixture(Spreadsheet spreadsheet) {
            for (CellReference cellRef : currentSelection) {
                spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(),
                        "SELECTED");
            }
            spreadsheet.refreshAllCellValues();
        }
    }

    class SelectionFixtureFactory implements SpreadsheetFixtureFactory {
        @Override
        public SpreadsheetFixture create() {
            return TestexcelsheetUI.this.new SelectionFixture();
        }
    }

    public static class EagerFixtureFactory implements
            SpreadsheetFixtureFactory {

        private SpreadsheetFixture fixture;

        public EagerFixtureFactory(SpreadsheetFixture fixture) {
            this.fixture = fixture;
        }

        @Override
        public SpreadsheetFixture create() {
            return fixture;
        }

    }
}
