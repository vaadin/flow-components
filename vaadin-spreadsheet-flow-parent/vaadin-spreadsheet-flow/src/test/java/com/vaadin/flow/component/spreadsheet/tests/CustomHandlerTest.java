/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetHandlerImpl;

public class CustomHandlerTest {

    @Test
    public void singleOriginalSpreadsheetEventIsRegistered() {
        var originalHandler = mock(SpreadsheetHandlerImpl.class);

        var spreadsheet = new Spreadsheet() {
            @Override
            public ComponentEventBus getEventBus() {
                return super.getEventBus();
            }

            @Override
            protected SpreadsheetHandlerImpl createDefaultHandler() {
                return originalHandler;
            }
        };

        var activeListeners = spreadsheet.getEventBus()
                .getListeners(Spreadsheet.SpreadsheetEvent.class);
        Assert.assertEquals(1, activeListeners.size());

        // Mock spreadsheet event to trigger the original handler
        var spreadsheetEvent = mock(Spreadsheet.SpreadsheetEvent.class);
        when(spreadsheetEvent.getType()).thenReturn("onConnectorInit");

        verify(originalHandler, times(0)).onConnectorInit();
        ComponentUtil.fireEvent(spreadsheet, spreadsheetEvent); // Fire
                                                                // spreadsheet
                                                                // event
        verify(originalHandler).onConnectorInit();
    }

    @Test
    public void newCustomHandlerOverridesOriginal() {
        var originalHandler = mock(SpreadsheetHandlerImpl.class);
        var newHandler = mock(SpreadsheetHandlerImpl.class);

        var spreadsheet = new Spreadsheet() {
            @Override
            public ComponentEventBus getEventBus() {
                return super.getEventBus();
            }

            @Override
            protected SpreadsheetHandlerImpl createDefaultHandler() {
                return originalHandler;
            }
        };

        spreadsheet.setSpreadsheetHandler(newHandler);

        var activeListeners = spreadsheet.getEventBus()
                .getListeners(Spreadsheet.SpreadsheetEvent.class);
        Assert.assertEquals(1, activeListeners.size());

        // Mock spreadsheet event to trigger the new handler
        var spreadsheetEvent = mock(Spreadsheet.SpreadsheetEvent.class);
        when(spreadsheetEvent.getType()).thenReturn("onConnectorInit");

        verify(newHandler, times(0)).onConnectorInit();
        ComponentUtil.fireEvent(spreadsheet, spreadsheetEvent); // Fire
                                                                // spreadsheet
                                                                // event
        verify(originalHandler, times(0)).onConnectorInit();
        verify(newHandler).onConnectorInit();
    }

}
