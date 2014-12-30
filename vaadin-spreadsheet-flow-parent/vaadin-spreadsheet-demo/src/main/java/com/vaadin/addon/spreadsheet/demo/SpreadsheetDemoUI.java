package com.vaadin.addon.spreadsheet.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.annotation.WebServlet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellReference;

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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Demo class for the Spreadsheet component.
 * <p>
 * You can upload any xls or xlsx file using the upload component. You can also
 * place spreadsheet files on the classpath, under the folder /testsheets/, and
 * they will be picked up in a combobox in the menu.
 * 
 *
 */
@SuppressWarnings("serial")
public class SpreadsheetDemoUI extends UI implements Receiver {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = SpreadsheetDemoUI.class, widgetset = "com.vaadin.addon.spreadsheet.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    VerticalLayout layout = new VerticalLayout();

    Upload upload = new Upload("Upload a XLS file to view it", this);
    private File previousFile = null;

    private Button save;
    private Button download;
    private File uploadedFile;
    private ComboBox openTestSheetSelect;
    private CheckBox gridlines;
    private CheckBox rowColHeadings;

    Spreadsheet spreadsheet;
    private SelectionChangeListener selectionChangeListener;
    private SpreadsheetComponentFactory spreadsheetFieldFactory;
    private SelectedSheetChangeListener selectedSheetChangeListener;
    private final Handler spreadsheetActionHandler = new SpreadsheetDefaultActionHandler();

    public SpreadsheetDemoUI() {
        super();
        SpreadsheetFactory.logMemoryUsage();
    }

    @Override
    protected void init(VaadinRequest request) {
        SpreadsheetFactory.logMemoryUsage();
        setContent(layout);

        buildOptions();

        selectionChangeListener = new SelectionChangeListener() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                printSelectionChangeEventContents(event);
            }
        };

        selectedSheetChangeListener = new SelectedSheetChangeListener() {

            @Override
            public void onSelectedSheetChange(SelectedSheetChangeEvent event) {
                gridlines.setValue(event.getNewSheet().isDisplayGridlines());
                rowColHeadings.setValue(event.getNewSheet()
                        .isDisplayRowColHeadings());
            }
        };

        spreadsheetFieldFactory = new TestComponentFactory();

    }

    private void buildOptions() {
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
                        createNewSheet();
                    }
                });

        Button newSpreadsheetInWindowButton = new Button("New in Window",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        createNewSheetInWindow();
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
                    saveFile();
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
                        createEditorTestSheet();
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
        options.addComponent(new Button("Reset from data",
                new Button.ClickListener() {

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

        upload.setImmediate(true);
        upload.addSucceededListener(new SucceededListener() {

            @Override
            public void uploadSucceeded(SucceededEvent event) {
                loadFile(uploadedFile);
            }
        });
    }

    protected void createNewSheet() {
        if (spreadsheet == null) {
            spreadsheet = new Spreadsheet();
            spreadsheet.addSelectionChangeListener(selectionChangeListener);
            spreadsheet
                    .addSelectedSheetChangeListener(selectedSheetChangeListener);
            spreadsheet.addActionHandler(spreadsheetActionHandler);

            layout.addComponent(spreadsheet);
            layout.setExpandRatio(spreadsheet, 1.0f);
        } else {
            spreadsheet.reloadSpreadsheetWithNewWorkbook();
        }
        spreadsheet.setSpreadsheetComponentFactory(null);
        save.setEnabled(true);
        previousFile = null;
        openTestSheetSelect.setValue(null);

        gridlines.setValue(spreadsheet.isDisplayGridLines());
        rowColHeadings.setValue(spreadsheet.isDisplayRowColHeadings());
    }

    protected void createNewSheetInWindow() {
        Spreadsheet spreadsheet = new Spreadsheet();

        Window w = new Window("new Spreadsheet", spreadsheet);
        w.setWidth("50%");
        w.setHeight("50%");
        w.center();
        w.setModal(true);

        addWindow(w);
    }

    protected void saveFile() {
        try {
            if (previousFile != null) {
                int i = previousFile.getName().lastIndexOf(".xls");
                String fileName = previousFile.getName().substring(0, i)
                        + ("(1)") + previousFile.getName().substring(i);
                previousFile = spreadsheet.writeSpreadsheetIntoFile(fileName);
            } else {
                previousFile = spreadsheet
                        .writeSpreadsheetIntoFile("workbook1");
            }
            download.setEnabled(true);
            FileResource resource = new FileResource(previousFile);
            FileDownloader fileDownloader = new FileDownloader(resource);
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

    protected void createEditorTestSheet() {
        if (spreadsheet == null) {
            spreadsheet = new Spreadsheet(
                    ((TestComponentFactory) spreadsheetFieldFactory)
                            .getTestWorkbook());
            spreadsheet.setSpreadsheetComponentFactory(spreadsheetFieldFactory);
            spreadsheet.addActionHandler(spreadsheetActionHandler);
            layout.addComponent(spreadsheet);

            layout.setExpandRatio(spreadsheet, 1.0f);
        } else {
            spreadsheet
                    .setWorkbook(((TestComponentFactory) spreadsheetFieldFactory)
                            .getTestWorkbook());
            spreadsheet.setSpreadsheetComponentFactory(spreadsheetFieldFactory);
        }

        gridlines.setValue(spreadsheet.isDisplayGridLines());
        rowColHeadings.setValue(spreadsheet.isDisplayRowColHeadings());
    }

    private void printSelectionChangeEventContents(SelectionChangeEvent event) {

        CellReference[] allSelectedCells = event.getAllSelectedCells();
        spreadsheet.setInfoLabelValue(allSelectedCells.length
                + " selected cells");

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
                layout.setExpandRatio(spreadsheet, 1.0f);
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

}
