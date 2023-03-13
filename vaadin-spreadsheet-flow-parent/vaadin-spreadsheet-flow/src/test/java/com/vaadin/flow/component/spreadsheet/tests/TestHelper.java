package com.vaadin.flow.component.spreadsheet.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SpreadsheetEvent;

import elemental.json.impl.JsonUtil;

public class TestHelper {

    /**
     * Fires a SpreadsheetEvent with the given event name and data. Can be used
     * to simulate events dispatched by the <vaadin-spreadsheet> Web Component.
     *
     * @param spreadsheet
     *            the Spreadsheet component
     * @param eventName
     *            the name of the event
     * @param jsonDataArray
     *            the data of the event as a JSON array, for example "[1, 1, 0]"
     */
    public static void fireClientEvent(Spreadsheet spreadsheet,
            String eventName, String jsonDataArray) {
        ComponentUtil.fireEvent(spreadsheet, new SpreadsheetEvent(spreadsheet,
                true, eventName, JsonUtil.parse(jsonDataArray)));
    }

    /**
     * Ceates a Spreadsheet component with the given Excel file as the data
     * source.
     *
     * @param fileName
     *            the name of the file. The file must be in the test_sheets
     *            folder.
     * @return the created Spreadsheet component
     */
    public static Spreadsheet createSpreadsheet(String fileName) {
        File testSheetFile = getTestSheetFile(fileName);
        try {
            return new Spreadsheet(testSheetFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not create Spreadsheet", e);
        }
    }

    /**
     * Gets the File object for the given file name.
     *
     * @param fileName
     *            the name of the file. The file must be in the test_sheets
     *            folder.
     * @return the File object
     */
    public static File getTestSheetFile(String fileName) {
        try {
            return new File(TestHelper.class.getClassLoader()
                    .getResource("test_sheets" + File.separator + fileName)
                    .toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(
                    "Can't find test sheet file " + fileName);
        }
    }

}
