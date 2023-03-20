
package com.vaadin.flow.component.grid;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;

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
