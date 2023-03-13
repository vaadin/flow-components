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
    public void getTooltip_hasTooltipElement() {
        var tooltip = component.getTooltip();
        Assert.assertNotNull(tooltip);
        Assert.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    public void getTooltip_hasNoText() {
        var tooltip = component.getTooltip();
        Assert.assertNull(tooltip.getText());
    }

    @Test
    public void setTooltipText_hasTooltipElement() {
        component.setTooltipText("foo");
        Assert.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    public void setTooltipText_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        Assert.assertEquals(tooltip, component.getTooltip());
    }

    @Test
    public void setTooltipTextAgain_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        var tooltip2 = component.setTooltipText("bar");
        Assert.assertEquals(tooltip, tooltip2);
        Assert.assertEquals(component.getTooltip().getText(), "bar");
    }

    @Test
    public void setTooltipTextNull_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        var tooltip2 = component.setTooltipText(null);
        Assert.assertEquals(tooltip, tooltip2);
        Assert.assertEquals(component.getTooltip().getText(), null);
    }

    @Test
    public void setTooltipText_tooltipHasText() {
        component.setTooltipText("foo");
        Assert.assertEquals("foo",
                getTooltipElement(component).get().getProperty("text"));
    }

    @Test
    public void setTooltipText_tooltipHasSlot() {
        component.setTooltipText("foo");
        Assert.assertEquals("tooltip",
                getTooltipElement(component).get().getAttribute("slot"));
    }

    @Test
    public void setTooltipTextAgain_hasOneTooltipElement() {
        component.setTooltipText("foo");
        component.setTooltipText("bar");
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
