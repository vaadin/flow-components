/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SpreadsheetEvent;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

public class TestHelper {

    /**
     * Fires a SpreadsheetEvent with the given event name and data. Can be used
     * to simulate events dispatched by the <vaadin-spreadsheet> Web Component.
     *
     * @param spreadsheet
     *            the Spreadsheet component
     * @param eventName
     *            the name of the event
     * @param jsonData
     *            the data of the event as a JSON object, for example
     *            {@code {"row": 1, "column": 1}}. Field names are the ones
     *            declared in rpc-client-to-server.ts.
     */
    public static void fireClientEvent(Spreadsheet spreadsheet,
            String eventName, String jsonData) {
        try {
            ComponentUtil.fireEvent(spreadsheet,
                    new SpreadsheetEvent(spreadsheet, true, eventName,
                            JacksonUtils.getMapper().readTree(jsonData)));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * What the spreadsheet sent the client as JSON, read back: an argument of a
     * call, or the value of a property. It goes out as JSON, not as text
     * holding JSON, so text fails here rather than reading as nothing.
     *
     * @param sent
     *            the argument or the property value
     * @return the JSON
     */
    public static JsonNode sentJson(Object sent) {
        if (sent instanceof JsonNode json) {
            return json;
        }
        throw new AssertionError("Not sent as JSON: " + sent);
    }

    /**
     * Creates a Spreadsheet component with the given Excel file as the data
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
