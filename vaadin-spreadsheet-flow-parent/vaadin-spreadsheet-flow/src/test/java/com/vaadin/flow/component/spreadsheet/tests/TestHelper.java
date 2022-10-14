package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SpreadsheetEvent;

import elemental.json.impl.JsonUtil;

class TestHelper {

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
    static void fireClientEvent(Spreadsheet spreadsheet, String eventName,
            String jsonDataArray) {
        ComponentUtil.fireEvent(spreadsheet, new SpreadsheetEvent(spreadsheet,
                true, eventName, JsonUtil.parse(jsonDataArray)));
    }

}
