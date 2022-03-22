package com.example.application.views.demo.views;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class ReportModeExample extends Div {
    public ReportModeExample() {
        add(createSpreadsheet());
    }

    private Spreadsheet createSpreadsheet() {
        setSizeFull();
        Spreadsheet spreadsheet = null;
        File sampleFile = null;
        try {
            ClassLoader classLoader = ReportModeExample.class.getClassLoader();
            URL resource = classLoader.getResource(
                    "testsheets" + File.separator + "Simple Invoice.xlsx");
            if (resource != null) {
                sampleFile = new File(resource.toURI());
                spreadsheet = new Spreadsheet(sampleFile);
                spreadsheet.setReportStyle(true);
                spreadsheet.setActiveSheetProtected("");
                spreadsheet.setRowColHeadingsVisible(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return spreadsheet;
    }
}
