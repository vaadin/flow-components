package com.vaadin.flow.component.shared;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;

public class HasTooltipTest {

    private TestComponent component;
    private UI ui;

    @Before
    public void setup() {
        component = new TestComponent();
        ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void default_doesNotHaveTooltipElement() {
        Assert.assertFalse(getTooltipElement(component).isPresent());
    }

    @Test
    public void default_doesNotHaveTooltip() {
        Assert.assertNull(component.getTooltip());
    }

    @Test
    public void setTooltip_hasTooltipElement() {
        component.setTooltip("foo");
        Assert.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    public void setTooltip_hasTooltip() {
        var tooltip = component.setTooltip("foo");
        Assert.assertEquals(tooltip, component.getTooltip());
    }

    @Test
    public void setTooltipAgain_hasTooltip() {
        var tooltip = component.setTooltip("foo");
        var tooltip2 = component.setTooltip("bar");
        Assert.assertEquals(tooltip, tooltip2);
        Assert.assertEquals(component.getTooltip().getText(), "bar");
    }

    @Test
    public void setTooltipNull_hasNoTooltip() {
        var tooltip = component.setTooltip("foo");
        component.setTooltip(null);
        Assert.assertEquals(component.getTooltip(), null);
        Assert.assertFalse(getTooltipElement(component).isPresent());
    }

    @Test
    public void setTooltip_tooltipHasText() {
        component.setTooltip("foo");
        Assert.assertEquals("foo",
                getTooltipElement(component).get().getProperty("text"));
    }

    @Test
    public void setTooltip_tooltipHasSlot() {
        component.setTooltip("foo");
        Assert.assertEquals("tooltip",
                getTooltipElement(component).get().getAttribute("slot"));
    }

    @Test
    public void setTooltipAgain_hasOneTooltipElement() {
        component.setTooltip("foo");
        component.setTooltip("bar");
        Assert.assertEquals(1, getTooltipElements(component).count());
    }

    private Optional<Element> getTooltipElement(HasTooltip component) {
        return getTooltipElements(component).findFirst();
    }

    private Stream<Element> getTooltipElements(HasTooltip component) {
        return component.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

    @Tag("test")
    private static class TestComponent extends Component implements HasTooltip {
    }
}
