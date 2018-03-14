package com.vaadin.flow.component.splitlayout.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.splitlayout.SplitLayout;

public class SplitLayoutUnitTest {

    @Test
    public void testGetOrientation_nothingSet_defaultReturnsHORIZONTAL() {
        SplitLayout splitLayout = new SplitLayout();
        Assert.assertEquals("Invalid default orientation", SplitLayout.Orientation.HORIZONTAL, splitLayout.getOrientation());
    }

    @Test
    public void testAddingSeveralComponents_slotspresent_wrapsInDiv() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addToPrimary(new Label("1"), new Label("2"), new Label("3"));
        splitLayout.addToSecondary(new Label("4"), new Label("5"));

        Component primaryComponent = splitLayout.getPrimaryComponent();

        Assert.assertEquals("The slot doesn't contain the primary slot.",
                "primary", primaryComponent.getElement().getAttribute("slot"));
        Assert.assertEquals("No wrapper div", "div", primaryComponent.getElement().getTag());
        Assert.assertEquals("Wrong number of children", 3, primaryComponent.getChildren().count());

        Component secondaryComponent = splitLayout.getSecondaryComponent();
        Assert.assertEquals("The slot doesn't contain the secondary slot.",
                "secondary",
                secondaryComponent.getElement().getAttribute("slot"));
        Assert.assertEquals("No wrapper div", "div", secondaryComponent.getElement().getTag());
        Assert.assertEquals("Wrong number of children", 2, secondaryComponent.getChildren().count());
    }
}
