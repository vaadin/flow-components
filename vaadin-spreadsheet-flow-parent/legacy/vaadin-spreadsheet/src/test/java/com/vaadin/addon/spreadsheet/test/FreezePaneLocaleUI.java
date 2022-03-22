package com.vaadin.addon.spreadsheet.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/**
 * Test UI for {@link FreezePaneLocaleUITest}
 */
@Theme("demo")
public class FreezePaneLocaleUI extends UI {
    @Override
    protected void init(VaadinRequest request) {
        ClassLoader classLoader = FreezePaneLocaleUITest.class.getClassLoader();
        URL resource = classLoader.getResource("test_sheets" + File.separator
                + "freezepanels.xlsx");

        try {
            File file = new File(resource.toURI());

            Spreadsheet sheet = new Spreadsheet(file);

            sheet.setLocale(Locale.FRANCE);

            setContent(sheet);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
