package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFactory;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route("vaadin-spreadsheet")
@PageTitle("Demo")
public class SpreadsheetPage extends VerticalLayout implements Receiver {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpreadsheetPage.class);

    private final Div spreadsheetContainer;

    VerticalLayout layout = new VerticalLayout();

    Spreadsheet spreadsheet;

    private File previousFile = null;

    private Button save;

    private Anchor download;

    private ComboBox<File> openTestSheetSelect;

    private Spreadsheet.SheetChangeListener selectedSheetChangeListener;

    private Button updateButton;

    private Checkbox gridlines, hideTop, hideBottom, hideBoth;

    private Checkbox rowColHeadings;

    private Upload upload = new Upload(this);

    private File uploadedFile;

    private HorizontalLayout options;

    private ComboBox<Locale> localeSelect;
    private Button loadFixtureBtn;
    private ComboBox<TestFixtures> fixtureSelect;

    public SpreadsheetPage() {
        addClassName("demo-view");
        setSizeFull();

        spreadsheetContainer = new Div();
        spreadsheetContainer.addClassName("spreadsheetContainer");
        spreadsheetContainer.setSizeFull();
        spreadsheetContainer.setMinHeight("400px");

        layout.addClassName("layout");
        layout.setSizeFull();

        options = new HorizontalLayout();
        options.addClassName("options");
        options.setSpacing(true);
        add(options);
        rowColHeadings = createRowHeadings();

        Button newSpreadsheetButton = createNewButton();
        newSpreadsheetButton.setId("createNewBtn");

        ClassLoader classLoader = SpreadsheetPage.class.getClassLoader();
        URL resource = classLoader.getResource("test_sheets" + File.separator);
        URI uri = null;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            LOGGER.warn("Incorrect resource" + resource, e);
        }
        if (uri != null) {
            String excelFilesRegex = ".*\\.xls|.*\\.xlsx|.*\\.xlsm";

            openTestSheetSelect = createTestSheetCombobox(
                    FileDataProvider.getFiles(uri, excelFilesRegex, LOGGER));
        }
        updateButton = createUpdateButton();
        save = createSaveButton();

        download = new Anchor(new StreamResource("xxxxxxxxx", () -> null),
                "Download");
        download.getElement().setAttribute("download", true);
        download.add(new Button(new Icon(VaadinIcon.DOWNLOAD_ALT)));
        download.setEnabled(false);

        gridlines = createCBNewLines();

        upload.addSucceededListener(event -> {
            loadFile(uploadedFile);
        });

        VerticalLayout checkBoxLayout = new VerticalLayout();
        checkBoxLayout.setMargin(false);
        checkBoxLayout.setSpacing(false);

        Button freezePanesButton = new Button("Freeze Pane",
                e -> new FreezePaneWindow().open());
        freezePanesButton.setId("freezePane");

        hideTop = new Checkbox("hide top bar visibility");
        hideBottom = new Checkbox("hide bottom bar visibility");
        hideBoth = new Checkbox("report mode");
        hideBoth.setId("report-mode");
        disableCheckboxes();

        hideTop.addValueChangeListener(event -> {
            spreadsheet.setFunctionBarVisible(!hideTop.getValue());
            hideBoth.setValue(spreadsheet.isReportStyle());
        });
        hideBottom.addValueChangeListener(event1 -> {
            spreadsheet.setSheetSelectionBarVisible(!hideBottom.getValue());
            hideBoth.setValue(spreadsheet.isReportStyle());
        });

        hideBoth.addValueChangeListener(event1 -> {
            spreadsheet.setReportStyle(hideBoth.getValue());
            hideTop.setValue(!spreadsheet.isFunctionBarVisible());
            hideBottom.setValue(!spreadsheet.isFunctionBarVisible());
        });

        checkBoxLayout.add(gridlines, rowColHeadings, hideTop, hideBottom,
                hideBoth);

        Button closeButton = new Button("Close", event -> {
            if (spreadsheet != null) {
                SpreadsheetFactory.logMemoryUsage();
                spreadsheetContainer.remove(spreadsheet);
                spreadsheet = null;
                SpreadsheetFactory.logMemoryUsage();
                disableCheckboxes();
            }
        });

        download.setHref(new StreamResource("testsheet.xlsx", () -> {
            try {
                return new FileInputStream(spreadsheet.write("testsheet.xlsx"));
            } catch (IOException e) {
                LOGGER.warn("ERROR reading file testsheet.xlsx", e);
            }
            return null;
        }));

        localeSelect = new ComboBox<>();
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
        localeSelect.setItemLabelGenerator(Locale::getDisplayName);
        localeSelect.addValueChangeListener(e -> updateLocale());

        HorizontalLayout sheetOptions = new HorizontalLayout();
        sheetOptions.setSpacing(true);
        sheetOptions.add(save);
        sheetOptions.add(download);

        selectedSheetChangeListener = new Spreadsheet.SheetChangeListener() {
            @Override
            public void onSheetChange(Spreadsheet.SheetChangeEvent event) {
                gridlines.setValue(spreadsheet.isGridlinesVisible());
                rowColHeadings.setValue(spreadsheet.isRowColHeadingsVisible());
            }
        };

        fixtureSelect = new ComboBox<>();
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
        loadFixture.setAlignItems(Alignment.CENTER);
        loadFixture.setVerticalComponentAlignment(Alignment.BASELINE,
                loadFixtureBtn);

        VerticalLayout createAndFreeze = new VerticalLayout();
        createAndFreeze.setSpacing(true);
        createAndFreeze.setMargin(false);
        createAndFreeze.add(newSpreadsheetButton, freezePanesButton);

        HorizontalLayout updateLayout = new HorizontalLayout();
        updateLayout.setSpacing(false);
        updateLayout.add(openTestSheetSelect, updateButton);
        VerticalLayout updateUpload = new VerticalLayout();
        updateUpload.setMargin(false);
        updateUpload.setSpacing(true);
        updateUpload.add(updateLayout, upload, localeSelect);

        VerticalLayout closeDownload = new VerticalLayout();
        closeDownload.setMargin(false);
        closeDownload.setSpacing(true);
        closeDownload.add(closeButton, download, loadFixture);

        checkBoxLayout.setWidth(null);
        createAndFreeze.setWidth(null);
        updateUpload.setWidth(null);
        closeDownload.setWidth(null);
        options.add(checkBoxLayout);
        options.add(createAndFreeze);
        options.add(updateUpload);
        options.add(closeDownload);

        add(layout);
        layout.add(spreadsheetContainer);
    }

    private void disableCheckboxes() {
        for (Checkbox b : Arrays.asList(hideTop, hideBottom, hideBoth)) {
            b.setValue(false);
            b.setEnabled(false);
        }
    }

    private Button createSaveButton() {
        Button save = new Button("Save", event -> {
            if (spreadsheet != null) {
                try {
                    if (previousFile != null) {
                        int i = previousFile.getName().lastIndexOf(".xls");
                        String fileName = previousFile.getName().substring(0, i)
                                + ("(1)") + previousFile.getName().substring(i);
                        previousFile = spreadsheet.write(fileName);
                    } else {
                        previousFile = spreadsheet.write("workbook1");
                    }
                    download.setEnabled(true);
                    download.setHref(new StreamResource(previousFile.getName(),
                            () -> null));
                    previousFile.deleteOnExit();
                } catch (Exception e) {
                    LOGGER.warn("ERROR reading file " + previousFile, e);
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
            if (file != null) {
                loadFile(file);
            }
        });
        updateButton.setId("update");
        return updateButton;
    }

    private ComboBox<File> createTestSheetCombobox(List<File> files) {
        ComboBox<File> cb = new ComboBox<>();

        cb.setItems(files);
        cb.setItemLabelGenerator(File::getName);
        cb.setId("testSheetSelect");
        cb.setPageSize(30);
        cb.setWidth("250px");
        return cb;
    }

    private Button createNewButton() {
        return new Button("Create new", event -> {

            if (spreadsheet == null) {
                spreadsheet = new Spreadsheet();
                updateLocale();
                spreadsheet.addSheetChangeListener(selectedSheetChangeListener);
                spreadsheetContainer.add(spreadsheet);
                spreadsheet.setSizeFull();
            } else {
                spreadsheet.reset();
            }
            spreadsheet.setSpreadsheetComponentFactory(null);
            save.setEnabled(true);
            previousFile = null;
            openTestSheetSelect.setValue(null);
            gridlines.setValue(spreadsheet.isGridlinesVisible());
            rowColHeadings.setValue(spreadsheet.isRowColHeadingsVisible());
            hideTop.setEnabled(true);
            hideBottom.setEnabled(true);
            hideBoth.setEnabled(true);
        });
    }

    private Checkbox createRowHeadings() {
        Checkbox rowColHeadings = new Checkbox(
                "display row and column headers");
        rowColHeadings.addValueChangeListener(event -> {
            if (spreadsheet != null) {
                spreadsheet.setRowColHeadingsVisible(event.getValue());
            }
        });
        return rowColHeadings;
    }

    private Checkbox createCBNewLines() {
        Checkbox cb = new Checkbox("display grid lines");
        cb.addValueChangeListener(event -> {
            if (spreadsheet != null) {
                spreadsheet.setGridlinesVisible(event.getValue());
            }
        });
        return cb;
    }

    private void loadFile(File file) {
        try {
            if (spreadsheet == null) {
                spreadsheet = new Spreadsheet(file);
                spreadsheet.addSheetChangeListener(selectedSheetChangeListener);
                spreadsheetContainer.add(spreadsheet);
                spreadsheet.setSizeFull();
            } else {
                if (previousFile == null || !previousFile.getAbsolutePath()
                        .equals(file.getAbsolutePath())) {
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
        } catch (Exception e) {
            LOGGER.warn("ERROR reading file " + file, e);
        }
    }

    private void updateLocale() {
        if (spreadsheet != null && localeSelect.getValue() instanceof Locale) {
            spreadsheet.setLocale(localeSelect.getValue());
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
            LOGGER.warn("ERROR reading file " + filename, e);
        }
        return null;
    }

    class FreezePaneWindow extends Dialog {

        public FreezePaneWindow() {
            add(new Text("Add/Remove freeze pane"));
            setWidth("300px");
            setHeight("300px");
            setResizable(false);
            setModal(true);

            VerticalLayout l = new VerticalLayout();
            l.setSpacing(false);
            l.setMargin(false);
            add(l);

            final TextField hSplitTF = new TextField(
                    "Horizontal Split Position");
            hSplitTF.setId("horizontalSplitPosition");
            hSplitTF.setValue("6");
            final TextField vSplitTF = new TextField("Vertical Split Position");
            vSplitTF.setId("verticalSplitPosition");
            vSplitTF.setValue("6");
            l.add(vSplitTF);
            l.add(hSplitTF);
            Button button = new Button("Submit values");
            button.setId("submitValues");
            button.addClickListener(event -> {
                try {
                    int vSplit = Integer.parseInt(vSplitTF.getValue());
                    int hSplit = Integer.parseInt(hSplitTF.getValue());

                    spreadsheet.createFreezePane(vSplit, hSplit);
                } catch (NumberFormatException e) {

                }

                close();
            });
            l.add(button);

        }
    }

}
