/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.tests.MockUIExtension;

class ReloadStateTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        ui.add(spreadsheet);
        // Clear the initial reload state scheduled during construction
        ui.fakeClientCommunication();
    }

    @Test
    void reloadActiveSheetData_rerenderPendingClearedOnNextResponse() {
        // Reloading the sheet data marks a re-render as pending
        spreadsheet.createFreezePane(2, 2);
        Assertions.assertTrue(spreadsheet.isRerenderPending());

        // The pending state is cleared on the next client response
        ui.fakeClientCommunication();
        Assertions.assertFalse(spreadsheet.isRerenderPending());
    }

    @Test
    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    void reloadActiveSheetDataWithoutCurrentUI_rerenderPendingClearedOnNextResponse() {
        // Simulate a reload without a current UI, for example from a
        // background thread or a VaadinSession.access call
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);

        spreadsheet.createFreezePane(2, 2);
        Assertions.assertTrue(spreadsheet.isRerenderPending());

        // The action is scheduled on the component, so it still runs on the
        // next client response even without a current UI
        ui.fakeClientCommunication();
        Assertions.assertFalse(spreadsheet.isRerenderPending());
    }
}
