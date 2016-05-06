package com.vaadin.spreadsheet.charts.ui;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@Theme("demo")
@Title("Spreadsheet-Charts Integration Demo")
@SuppressWarnings("serial")
public class SpreadsheetChartsDemoUI extends UI {

    private Spreadsheet spreadsheet;
    private ComboBox openTestSheetSelect;

    @WebServlet(value = {"/*","/VAADIN/*"}, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = SpreadsheetChartsDemoUI.class, widgetset = "com.vaadin.addon.spreadsheet.charts.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        spreadsheet = new Spreadsheet();
        spreadsheet.setSizeFull();

        layout.addComponents(createFileLayout(), spreadsheet);
        layout.setExpandRatio(spreadsheet, 1);

        setContent(layout);

        getPage().addUriFragmentChangedListener(
                new Page.UriFragmentChangedListener() {
                    public void uriFragmentChanged(
                            Page.UriFragmentChangedEvent event) {
                        updateFromFragment();
                    }
                });

        updateFromFragment();
    }

    /*
     * Rudimentary fragment handling to make developing&testing faster
     */
    private void updateFromFragment() {
        String uriFragment = getPage().getUriFragment();
        if (uriFragment != null && uriFragment.startsWith("file/")) {
            String filename = null;
            Integer sheetIndex = null;

            // #file/<filename>/sheet/<sheetIndex>/fixture/<fixturename>

            String[] tokens = uriFragment.split("/");
            for (int i = 0; i < tokens.length; i++) {
                if ("file".equals(tokens[i])) {
                    filename = tokens[i + 1];
                    System.out.println("Opening file " + filename);
                } else if ("sheet".equals(tokens[i])) {
                    sheetIndex = Integer.valueOf(tokens[i + 1]) - 1;
                    System.out.println("Opening sheet " + sheetIndex);
                }
            }

            selectItemFromCombobox(filename, sheetIndex);
        }
    }

    private void selectItemFromCombobox(String filename, Integer sheetIndex) {
        for (Object id : openTestSheetSelect.getItemIds()) {
            File file = (File) id;
            if (filename.equals(file.getName())) {
                openTestSheetSelect.select(file);

                if (sheetIndex != null) {
                    spreadsheet.setActiveSheetIndex(sheetIndex);
                }

                return;
            }
        }

        Notification.show("File not found: " + filename,
                Notification.Type.WARNING_MESSAGE);
    }

    private Component createFileLayout() {
        File file = null;
        try {
            ClassLoader classLoader = SpreadsheetChartsDemoUI.class
                    .getClassLoader();
            URL resource = classLoader.getResource("test_sheets"
                    + File.separator);
            file = new File(resource.toURI());
        } catch (URISyntaxException e) {
            System.err.println("Could not read test_sheets directory");
            return null;
        }

        final FilesystemContainer testSheetContainer = new FilesystemContainer(
                file);

        openTestSheetSelect = new ComboBox("Select a sample file:",
                testSheetContainer);

        openTestSheetSelect.setFilteringMode(FilteringMode.CONTAINS);
        openTestSheetSelect.setItemCaptionPropertyId("Name");
        openTestSheetSelect.setPageLength(30);
        openTestSheetSelect.setWidth("500px");

        openTestSheetSelect.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                Object value = event.getProperty().getValue();
                if (value != null && value instanceof File) {
                    loadFile((File) value);
                }
            }
        });

        return openTestSheetSelect;
    }

    private void loadFile(File file) {
        try {
            spreadsheet.read(file);
            Page.getCurrent().setUriFragment("file/" + file.getName(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
