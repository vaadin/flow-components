package com.vaadin.flow.component.splitlayout.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.SplitterDragendEvent;

public class SplitLayoutUnitTest {

    @Test
    public void testGetOrientation_nothingSet_defaultReturnsHORIZONTAL() {
        SplitLayout splitLayout = new SplitLayout();
        Assert.assertEquals("Invalid default orientation",
                SplitLayout.Orientation.HORIZONTAL,
                splitLayout.getOrientation());
    }

    @Test
    public void testAddingSeveralComponents_slotspresent_wrapsInDiv() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addToPrimary(new Label("1"), new Label("2"),
                new Label("3"));
        splitLayout.addToSecondary(new Label("4"), new Label("5"));

        Component primaryComponent = splitLayout.getPrimaryComponent();

        Assert.assertEquals("The slot doesn't contain the primary slot.",
                "primary", primaryComponent.getElement().getAttribute("slot"));
        Assert.assertEquals("No wrapper div", "div",
                primaryComponent.getElement().getTag());
        Assert.assertEquals("Wrong number of children", 3,
                primaryComponent.getChildren().count());

        Component secondaryComponent = splitLayout.getSecondaryComponent();
        Assert.assertEquals("The slot doesn't contain the secondary slot.",
                "secondary",
                secondaryComponent.getElement().getAttribute("slot"));
        Assert.assertEquals("No wrapper div", "div",
                secondaryComponent.getElement().getTag());
        Assert.assertEquals("Wrong number of children", 2,
                secondaryComponent.getChildren().count());
    }

    @Test
    public void testGetSplitterPosition() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;

        splitLayout.setSplitterPosition(splitterPosition);

        Assert.assertEquals(splitterPosition, splitLayout.getSplitterPosition(),
                0.01);
    }

    @Test
    public void testUpdateSplitterPosition_dragEndEvent_widthInPixels() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragendEvent(
                splitLayout, true, "432.68px", "267.32px"));

        Assert.assertEquals(61.81, splitLayout.getSplitterPosition(), 0.01);
    }

    @Test
    public void testUpdateSplitterPosition_dragEndEvent_widthInPercentage() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragendEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assert.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
    }

    @Test
    public void testUpdateSplitterPosition_dragEndEvent_primaryWidthNull() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragendEvent(splitLayout, true, null, "41.77%"));

        Assert.assertEquals(45.66, splitLayout.getSplitterPosition(), 0);
    }

    @Test
    public void testUpdateSplitterPosition_dragEndEvent_secondaryWidthNull() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragendEvent(splitLayout, true, "41.77%", null));

        Assert.assertEquals(45.66, splitLayout.getSplitterPosition(), 0);
    }

    @Test
    public void testUpdateSplitterPosition_noInitialPosition() {
        SplitLayout splitLayout = new SplitLayout();

        ComponentUtil.fireEvent(splitLayout, new SplitterDragendEvent(
                splitLayout, true, "432.68px", "267.32px"));

        Assert.assertEquals(61.81, splitLayout.getSplitterPosition(), 0.01);
    }
}
