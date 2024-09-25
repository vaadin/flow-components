/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.splitlayout.tests;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
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
