/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class GridDndMobilePolyfillTest {

    private Grid<String> grid;
    private UI ui;

    @Before
    public void setup() {
        VaadinService service = Mockito.mock(VaadinService.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.getService()).thenReturn(service);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui = new UI() {
            @Override
            public VaadinSession getSession() {
                return session;
            }
        };
        ui.getInternals().setSession(session);

        grid = new Grid<>();
        ui.add(grid);
    }

    @Test
    public void gridDnd_setDropMode_mobilePolyfillShouldBeAdded() {
        ui.getInternals().dumpPendingJavaScriptInvocations();
        grid.setDropMode(GridDropMode.ON_GRID);
        Assert.assertEquals(
                "Grid::setDropMode should add DnD mobile polyfill script to the UI.",
                1, ui.getInternals().dumpPendingJavaScriptInvocations().size());
    }

    @Test
    public void gridDnd_setRowsDraggable_mobilePolyfillShouldBeAdded() {
        ui.getInternals().dumpPendingJavaScriptInvocations();
        grid.setRowsDraggable(true);
        Assert.assertEquals(
                "Grid::setRowsDraggable should add DnD mobile polyfill script to the UI.",
                1, ui.getInternals().dumpPendingJavaScriptInvocations().size());
    }
}
