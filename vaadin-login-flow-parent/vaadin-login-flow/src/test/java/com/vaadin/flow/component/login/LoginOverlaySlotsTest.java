package com.vaadin.flow.component.login;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.server.VaadinSession;

public class LoginOverlaySlotsTest {

    private UI ui = new UI();
    private LoginOverlay overlay;

    @Tag("div")
    private static class TestComponent extends Component {
    }

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        overlay = new LoginOverlay();
        ui.add(overlay);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void customFields_addBeforeOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFields().add(foo, bar);

        Assert.assertEquals(foo.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(bar.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(foo.getElement().getAttribute("slot"),
                "custom-fields");
        Assert.assertEquals(bar.getElement().getAttribute("slot"),
                "custom-fields");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void customFields_addAfterOpened_throwsUnsupportedOperationException() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.setOpened(true);
        overlay.getCustomFields().add(foo, bar);
    }

    @Test
    public void customFields_removeBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFields().add(foo, bar);

        overlay.getCustomFields().remove(foo);
        Assert.assertEquals(foo.getElement().getParent(), null);
        Assert.assertNotEquals(foo.getElement().getAttribute("slot"),
                "custom-fields");

        overlay.getCustomFields().remove(bar);
        Assert.assertEquals(bar.getElement().getParent(), null);
        Assert.assertNotEquals(bar.getElement().getAttribute("slot"),
                "custom-fields");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void customFields_removeAfterOpened_throwsUnsupportedOperationException() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFields().add(foo, bar);
        overlay.setOpened(true);

        overlay.getCustomFields().remove(foo);
    }

    @Test
    public void customFields_removeAllBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFields().add(foo, bar);

        overlay.getCustomFields().removeAll();

        Assert.assertEquals(foo.getElement().getParent(), null);
        Assert.assertEquals(bar.getElement().getParent(), null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void customFields_removeAllAfterOpened_throwsUnsupportedOperationException() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getCustomFields().add(foo, bar);
        overlay.setOpened(true);

        overlay.getCustomFields().removeAll();
    }

    @Test
    public void footer_addBeforeOpened_componentsAreAdded() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        Assert.assertEquals(foo.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(bar.getElement().getParent(), overlay.getElement());
        Assert.assertEquals(foo.getElement().getAttribute("slot"), "footer");
        Assert.assertEquals(bar.getElement().getAttribute("slot"), "footer");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void footer_addAfterOpened_throwsUnsupportedOperationException() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.setOpened(true);
        overlay.getFooter().add(foo, bar);
    }

    @Test
    public void footer_removeBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        overlay.getFooter().remove(foo);
        Assert.assertEquals(foo.getElement().getParent(), null);
        Assert.assertNotEquals(foo.getElement().getAttribute("slot"), "footer");

        overlay.getFooter().remove(bar);
        Assert.assertEquals(bar.getElement().getParent(), null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void footer_removeAfterOpened_throwsUnsupportedOperationException() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);
        overlay.setOpened(true);

        overlay.getFooter().remove(foo);
    }

    @Test
    public void footer_removeAllBeforeOpened_componentsAreRemoved() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);

        overlay.getFooter().removeAll();

        Assert.assertEquals(foo.getElement().getParent(), null);
        Assert.assertEquals(bar.getElement().getParent(), null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void footer_removeAllAfterOpened_throwsUnsupportedOperationException() {
        TestComponent foo = new TestComponent();
        TestComponent bar = new TestComponent();

        overlay.getFooter().add(foo, bar);
        overlay.setOpened(true);

        overlay.getFooter().removeAll();
    }
}
