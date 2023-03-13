package com.vaadin.flow.component.spreadsheet.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.apache.poi.ss.usermodel.SheetVisibility;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.framework.Action;
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

            SpreadsheetActionHandler handler = new SpreadsheetActionHandler();
            handler.addCellHandler(new SpreadsheetActionHandler.Cell() {
                @Override
                public void handleAction(Action action,
                        Spreadsheet.SelectionChangeEvent sender,
                        Spreadsheet target) {
                    sheet.setSheetHidden(sheet.getActiveSheetPOIIndex(),
                            SheetVisibility.HIDDEN);
                }

                @Override
                public Action[] getActions(
                        Spreadsheet.SelectionChangeEvent selection,
                        Spreadsheet sender) {
                    return new Action[] { new Action("Hide sheet"), };
                }
            });
            sheet.addActionHandler(handler);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
