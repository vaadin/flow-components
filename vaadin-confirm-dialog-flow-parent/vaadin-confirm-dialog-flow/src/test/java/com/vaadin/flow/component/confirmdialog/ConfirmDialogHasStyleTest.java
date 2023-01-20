package com.vaadin.flow.component.confirmdialog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.InternalOverlayClassListProxy;
import com.vaadin.flow.server.VaadinSession;

public class ConfirmDialogHasStyleTest {

    private UI ui = new UI();
    private ConfirmDialog dialog;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        dialog = new ConfirmDialog();
        ui.add(dialog);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addClassName_dialogHasOverlayClass() {
        dialog.addClassName("foo");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo");

        dialog.addClassName("bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo bar");
    }

    @Test
    public void removeClassName_dialogHasOverlayClass() {
        dialog.addClassName("foo");

        dialog.removeClassName("foo");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void setClassNameString_dialogHasOverlayClass() {
        dialog.setClassName("foo");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo");

        dialog.setClassName("bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "bar");
    }

    @Test
    public void setClassNameBoolean_dialogHasOverlayClass() {
        dialog.setClassName("foo", true);

        dialog.setClassName("foo", false);

        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void addClassNames_dialogHasOverlayClass() {
        dialog.addClassNames("foo", "bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo bar");

        dialog.addClassNames("baz", "qux");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "foo bar baz qux");
    }

    @Test
    public void removeClassNames_dialogHasOverlayClass() {
        dialog.addClassNames("foo", "bar", "baz", "qux");

        dialog.removeClassNames("foo", "bar");
        Assert.assertEquals(dialog.getElement().getProperty("overlayClass"),
                "baz qux");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getStyle_unsupported() {
        dialog.getStyle();
    }

    @Test
    public void getClassNames_usesProxy() {
        Assert.assertTrue(dialog
                .getClassNames() instanceof InternalOverlayClassListProxy);
    }
}
