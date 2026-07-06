/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

class GridProHiddenColumnCellEditableTest {

    @RegisterExtension
    private MockUIExtension ui = new MockUIExtension();

    private GridPro<String> grid;
    private GridPro.EditColumn<String> column;
    private AtomicInteger cellEditableProviderCallCount = new AtomicInteger(
            0);

    @BeforeEach
    void setup() {
        grid = new GridPro<>();
        grid.setItems("Item 0");

        column = (GridPro.EditColumn<String>) grid.addEditColumn(item -> item)
                .text((item, newValue) -> {
                });
        column.setCellEditableProvider(item -> {
            cellEditableProviderCallCount.incrementAndGet();
            return true;
        });

        ui.add(grid);
    }

    @Test
    void visibleColumn_cellEditableProviderCalled() {
        ui.fakeClientCommunication();
        Assertions.assertEquals(1, cellEditableProviderCallCount.get());
    }

    @Test
    void hiddenColumn_cellEditableProviderNotCalled() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        Assertions.assertEquals(0, cellEditableProviderCallCount.get());
    }
}
