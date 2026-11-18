/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource;
import com.vaadin.flow.server.streams.DownloadDisposition;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import com.vaadin.tests.DownloadHandlerTestUtil;
import com.vaadin.tests.MockUIExtension;

class SpreadsheetTest {
    @Test
    void setId_getId_returnsId() {
        var spreadsheet = new Spreadsheet();
        spreadsheet.setId("foo");
        assertEquals("foo", spreadsheet.getId().orElse(null));
    }

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void setResource_servedInline_handlerNotChanged() throws Exception {
        InputStreamDownloadHandler handler = DownloadHandler.fromInputStream(
                event -> DownloadResponse.error(500), "chart.png");
        var spreadsheet = new Spreadsheet() {
            {
                setResource("chart", handler);
            }
        };
        ui.add(spreadsheet);

        var resource = (ElementStreamResource) ui.getSession()
                .getResourceRegistry().getResource(URI.create(spreadsheet
                        .getElement().getAttribute("resource-chart")))
                .orElseThrow();
        assertEquals(DownloadDisposition.INLINE, resource.getDisposition());
        assertEquals("attachment; filename=\"chart.png\"",
                DownloadHandlerTestUtil.servedContentDisposition(handler,
                        resource.getOwner()),
                "The given handler should not be changed");
    }
}
