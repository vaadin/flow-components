/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.server.VaadinSession;

public class PopoverHasStyleTest {

    private UI ui = new UI();
    private Popover popover;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        popover = new Popover();
        ui.add(popover);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addClassName_popoverHasOverlayClass() {
        popover.addClassName("foo");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "foo");

        popover.addClassName("bar");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "foo bar");
    }

    @Test
    public void removeClassName_popoverHasOverlayClass() {
        popover.addClassName("foo");

        popover.removeClassName("foo");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void setClassNameString_popoverHasOverlayClass() {
        popover.setClassName("foo");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "foo");

        popover.setClassName("bar");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "bar");
    }

    @Test
    public void setClassNameBoolean_popoverHasOverlayClass() {
        popover.setClassName("foo", true);

        popover.setClassName("foo", false);

        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                null);
    }

    @Test
    public void setClassNameMultiple_popoverHasOverlayClass() {
        popover.setClassName("foo bar");
        popover.getClassNames().set("foo", false);

        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "bar");
    }

    @Test
    public void addClassNames_popoverHasOverlayClass() {
        popover.addClassNames("foo", "bar");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "foo bar");

        popover.addClassNames("baz", "qux");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "foo bar baz qux");
    }

    @Test
    public void removeClassNames_popoverHasOverlayClass() {
        popover.addClassNames("foo", "bar", "baz", "qux");

        popover.removeClassNames("foo", "bar");
        Assert.assertEquals(popover.getElement().getProperty("overlayClass"),
                "baz qux");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getStyle_unsupported() {
        popover.getStyle();
    }

    @Test
    public void getClassNames_usesProxy() {
        Assert.assertTrue(
                popover.getClassNames() instanceof OverlayClassListProxy);
    }
}
