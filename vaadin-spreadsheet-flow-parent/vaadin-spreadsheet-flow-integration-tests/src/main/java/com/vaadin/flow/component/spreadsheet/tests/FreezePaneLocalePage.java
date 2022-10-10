package com.vaadin.flow.component.spreadsheet.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/freeze-pane-locale")
public class FreezePaneLocalePage extends Div {

    public FreezePaneLocalePage() {
        super();
        setSizeFull();

        ClassLoader classLoader = SpreadsheetPage.class.getClassLoader();
        URL resource = classLoader.getResource(
                "test_sheets" + File.separator + "freezepanels.xlsx");

        try {
            File file = new File(resource.toURI());

            Spreadsheet sheet = new Spreadsheet(file);

            sheet.setLocale(Locale.FRANCE);

            add(sheet);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
