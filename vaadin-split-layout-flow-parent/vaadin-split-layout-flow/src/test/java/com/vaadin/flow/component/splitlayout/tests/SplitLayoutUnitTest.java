package com.vaadin.flow.component.splitlayout.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.SplitterDragendEvent;

import java.util.concurrent.atomic.AtomicInteger;

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
    public void splitLayoutWithPrimaryComponent_secondComponentAdded_primaryIsNotDetached() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var primaryComponent = new Div();
        var detachCounter = new AtomicInteger();
        primaryComponent
                .addDetachListener(event -> detachCounter.incrementAndGet());

        splitLayout.addToPrimary(primaryComponent);
        splitLayout.addToSecondary(new Div());
        Assert.assertEquals(0, detachCounter.get());
    }

    @Test
    public void splitLayoutWithSecondaryComponent_primaryComponentAdded_secondaryIsNotDetached() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var secondaryComponent = new Div();
        var detachCounter = new AtomicInteger();
        secondaryComponent
                .addDetachListener(event -> detachCounter.incrementAndGet());

        splitLayout.addToSecondary(secondaryComponent);
        splitLayout.addToPrimary(new Div());
        Assert.assertEquals(0, detachCounter.get());
    }

    @Test
    public void splitLayoutWithPrimaryComponent_primaryComponentRemoved_referenceUpdated() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var primaryComponent = new Div();
        splitLayout.addToPrimary(primaryComponent);
        Assert.assertEquals(primaryComponent,
                splitLayout.getPrimaryComponent());

        splitLayout.remove(primaryComponent);
        Assert.assertNull(splitLayout.getPrimaryComponent());
    }

    @Test
    public void splitLayoutWithSecondaryComponent_secondaryComponentRemoved_referenceUpdated() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var secondaryComponent = new Div();
        splitLayout.addToSecondary(secondaryComponent);
        Assert.assertEquals(secondaryComponent,
                splitLayout.getSecondaryComponent());

        splitLayout.remove(secondaryComponent);
        Assert.assertNull(splitLayout.getSecondaryComponent());
    }

    @Test
    public void splitLayoutTwoComponents_removeAll_bothReferencesUpdated() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        splitLayout.addToPrimary(new Div());
        splitLayout.addToSecondary(new Div());

        splitLayout.removeAll();
        Assert.assertNull(splitLayout.getPrimaryComponent());
        Assert.assertNull(splitLayout.getSecondaryComponent());
    }

    @Test
    public void withoutSplitterPosition_addComponents_noFlexStyle() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addToPrimary(new Div());
        splitLayout.addToSecondary(new Div());

        Assert.assertNull(
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertNull(
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
    }

    @Test
    public void addComponents_setSplitterPosition_appliesFlexStyle() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addToPrimary(new Div());
        splitLayout.addToSecondary(new Div());

        splitLayout.setSplitterPosition(75);
        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
    }

    @Test
    public void setSplitterPosition_addComponents_appliesFlexStyle() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(75);

        splitLayout.addToPrimary(new Div());
        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));

        splitLayout.addToSecondary(new Div());
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
    }

    @Test
    public void setSplitterPosition_replaceComponents_restoresFlexStyle() {
        SplitLayout splitLayout = new SplitLayout(new Div(), new Div());
        splitLayout.setSplitterPosition(75);
        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));

        splitLayout.removeAll();

        splitLayout.addToPrimary(new Div());
        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        splitLayout.addToSecondary(new Div());
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
    }

    @Test
    public void setSplitterPosition_dragEndEvent_updatesFlexStyle() {
        SplitLayout splitLayout = new SplitLayout(new Div(), new Div());
        splitLayout.setSplitterPosition(75);
        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));

        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragendEvent(splitLayout, true, "30px", "70px"));

        Assert.assertEquals("1 1 30px",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 70px",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
    }

    @Test
    public void dragEndEvent_setSplitterPosition_updatesFlexStyle() {
        SplitLayout splitLayout = new SplitLayout(new Div(), new Div());
        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragendEvent(splitLayout, true, "30px", "70px"));
        Assert.assertEquals("1 1 30px",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 70px",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));

        splitLayout.setSplitterPosition(75);

        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
    }

    @Test
    public void setSplitterPosition_dragEndEvent_replaceComponents_restoresFlexStyle() {
        SplitLayout splitLayout = new SplitLayout(new Div(), new Div());
        splitLayout.setSplitterPosition(75);
        Assert.assertEquals("1 1 75.0%",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 25.0%",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));

        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragendEvent(splitLayout, true, "30px", "70px"));

        Assert.assertEquals("1 1 30px",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        Assert.assertEquals("1 1 70px",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));

        splitLayout.removeAll();

        splitLayout.addToPrimary(new Div());
        Assert.assertEquals("1 1 30px",
                splitLayout.getPrimaryComponent().getStyle().get("flex"));
        splitLayout.addToSecondary(new Div());
        Assert.assertEquals("1 1 70px",
                splitLayout.getSecondaryComponent().getStyle().get("flex"));
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
    public void constructorDefault() {
        SplitLayout splitLayout = new SplitLayout();

        Assert.assertEquals(SplitLayout.Orientation.HORIZONTAL,
                splitLayout.getOrientation());
    }

    @Test
    public void constructorWithOrientation() {
        SplitLayout splitLayout = new SplitLayout(
                SplitLayout.Orientation.VERTICAL);
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragendEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assert.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
        Assert.assertEquals(SplitLayout.Orientation.VERTICAL,
                splitLayout.getOrientation());
    }

    @Test
    public void constructorWithComponents() {
        var primaryComponent = new Div();
        var secondaryComponent = new Div();
        SplitLayout splitLayout = new SplitLayout(primaryComponent,
                secondaryComponent);
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragendEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assert.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
        Assert.assertEquals(primaryComponent,
                splitLayout.getPrimaryComponent());
        Assert.assertEquals(secondaryComponent,
                splitLayout.getSecondaryComponent());
        Assert.assertEquals(SplitLayout.Orientation.HORIZONTAL,
                splitLayout.getOrientation());
    }

    @Test
    public void constructorWithComponentsAndOrientation() {
        var primaryComponent = new Div();
        var secondaryComponent = new Div();
        SplitLayout splitLayout = new SplitLayout(primaryComponent,
                secondaryComponent, SplitLayout.Orientation.VERTICAL);
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragendEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assert.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
        Assert.assertEquals(primaryComponent,
                splitLayout.getPrimaryComponent());
        Assert.assertEquals(secondaryComponent,
                splitLayout.getSecondaryComponent());
        Assert.assertEquals(SplitLayout.Orientation.VERTICAL,
                splitLayout.getOrientation());
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
