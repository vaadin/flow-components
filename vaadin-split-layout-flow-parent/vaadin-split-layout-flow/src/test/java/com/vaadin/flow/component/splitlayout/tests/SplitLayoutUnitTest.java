/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.SplitterDragEndEvent;

class SplitLayoutUnitTest {

    @Test
    void testGetOrientation_nothingSet_defaultReturnsHORIZONTAL() {
        SplitLayout splitLayout = new SplitLayout();
        Assertions.assertEquals(SplitLayout.Orientation.HORIZONTAL,
                splitLayout.getOrientation(), "Invalid default orientation");
    }

    @Test
    void testAddingSeveralComponents_slotspresent_wrapsInDiv() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addToPrimary(new Span("1"), new Span("2"), new Span("3"));
        splitLayout.addToSecondary(new Span("4"), new Span("5"));

        Component primaryComponent = splitLayout.getPrimaryComponent();

        Assertions.assertEquals("primary",
                primaryComponent.getElement().getAttribute("slot"),
                "The slot doesn't contain the primary slot.");
        Assertions.assertEquals("div", primaryComponent.getElement().getTag(),
                "No wrapper div");
        Assertions.assertEquals(3, primaryComponent.getChildren().count(),
                "Wrong number of children");

        Component secondaryComponent = splitLayout.getSecondaryComponent();
        Assertions.assertEquals("secondary",
                secondaryComponent.getElement().getAttribute("slot"),
                "The slot doesn't contain the secondary slot.");
        Assertions.assertEquals("div", secondaryComponent.getElement().getTag(),
                "No wrapper div");
        Assertions.assertEquals(2, secondaryComponent.getChildren().count(),
                "Wrong number of children");
    }

    @Test
    void splitLayoutWithPrimaryComponent_secondComponentAdded_primaryIsNotDetached() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var primaryComponent = new Div();
        var detachCounter = new AtomicInteger();
        primaryComponent
                .addDetachListener(event -> detachCounter.incrementAndGet());

        splitLayout.addToPrimary(primaryComponent);
        splitLayout.addToSecondary(new Div());
        Assertions.assertEquals(0, detachCounter.get());
    }

    @Test
    void splitLayoutWithSecondaryComponent_primaryComponentAdded_secondaryIsNotDetached() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var secondaryComponent = new Div();
        var detachCounter = new AtomicInteger();
        secondaryComponent
                .addDetachListener(event -> detachCounter.incrementAndGet());

        splitLayout.addToSecondary(secondaryComponent);
        splitLayout.addToPrimary(new Div());
        Assertions.assertEquals(0, detachCounter.get());
    }

    @Test
    void splitLayoutWithPrimaryComponent_primaryComponentRemoved_referenceUpdated() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var primaryComponent = new Div();
        splitLayout.addToPrimary(primaryComponent);
        Assertions.assertEquals(primaryComponent,
                splitLayout.getPrimaryComponent());

        splitLayout.remove(primaryComponent);
        Assertions.assertNull(splitLayout.getPrimaryComponent());
    }

    @Test
    void splitLayoutWithSecondaryComponent_secondaryComponentRemoved_referenceUpdated() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        var secondaryComponent = new Div();
        splitLayout.addToSecondary(secondaryComponent);
        Assertions.assertEquals(secondaryComponent,
                splitLayout.getSecondaryComponent());

        splitLayout.remove(secondaryComponent);
        Assertions.assertNull(splitLayout.getSecondaryComponent());
    }

    @Test
    void splitLayoutTwoComponents_removeAll_bothReferencesUpdated() {
        var ui = new UI();
        var splitLayout = new SplitLayout();
        ui.add(splitLayout);

        splitLayout.addToPrimary(new Div());
        splitLayout.addToSecondary(new Div());

        splitLayout.removeAll();
        Assertions.assertNull(splitLayout.getPrimaryComponent());
        Assertions.assertNull(splitLayout.getSecondaryComponent());
    }

    @Test
    void testGetSplitterPosition() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;

        splitLayout.setSplitterPosition(splitterPosition);

        Assertions.assertEquals(splitterPosition,
                splitLayout.getSplitterPosition(), 0.01);
    }

    @Test
    void testUpdateSplitterPosition_dragEndEvent_widthInPixels() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragEndEvent(
                splitLayout, true, "432.68px", "267.32px"));

        Assertions.assertEquals(61.81, splitLayout.getSplitterPosition(), 0.01);
    }

    @Test
    void testUpdateSplitterPosition_dragEndEvent_widthInPercentage() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragEndEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assertions.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
    }

    @Test
    void constructorDefault() {
        SplitLayout splitLayout = new SplitLayout();

        Assertions.assertEquals(SplitLayout.Orientation.HORIZONTAL,
                splitLayout.getOrientation());
    }

    @Test
    void constructorWithOrientation() {
        SplitLayout splitLayout = new SplitLayout(
                SplitLayout.Orientation.VERTICAL);
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragEndEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assertions.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
        Assertions.assertEquals(SplitLayout.Orientation.VERTICAL,
                splitLayout.getOrientation());
    }

    @Test
    void constructorWithComponents() {
        var primaryComponent = new Div();
        var secondaryComponent = new Div();
        SplitLayout splitLayout = new SplitLayout(primaryComponent,
                secondaryComponent);
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragEndEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assertions.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
        Assertions.assertEquals(primaryComponent,
                splitLayout.getPrimaryComponent());
        Assertions.assertEquals(secondaryComponent,
                splitLayout.getSecondaryComponent());
        Assertions.assertEquals(SplitLayout.Orientation.HORIZONTAL,
                splitLayout.getOrientation());
    }

    @Test
    void constructorWithComponentsAndOrientation() {
        var primaryComponent = new Div();
        var secondaryComponent = new Div();
        SplitLayout splitLayout = new SplitLayout(primaryComponent,
                secondaryComponent, SplitLayout.Orientation.VERTICAL);
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout, new SplitterDragEndEvent(
                splitLayout, true, "58.23%", "41.77%"));

        Assertions.assertEquals(58.23, splitLayout.getSplitterPosition(), 0);
        Assertions.assertEquals(primaryComponent,
                splitLayout.getPrimaryComponent());
        Assertions.assertEquals(secondaryComponent,
                splitLayout.getSecondaryComponent());
        Assertions.assertEquals(SplitLayout.Orientation.VERTICAL,
                splitLayout.getOrientation());
    }

    @Test
    void testUpdateSplitterPosition_dragEndEvent_primaryWidthNull() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragEndEvent(splitLayout, true, null, "41.77%"));

        Assertions.assertEquals(45.66, splitLayout.getSplitterPosition(), 0);
    }

    @Test
    void testUpdateSplitterPosition_dragEndEvent_secondaryWidthNull() {
        SplitLayout splitLayout = new SplitLayout();
        double splitterPosition = 45.66;
        splitLayout.setSplitterPosition(splitterPosition);

        ComponentUtil.fireEvent(splitLayout,
                new SplitterDragEndEvent(splitLayout, true, "41.77%", null));

        Assertions.assertEquals(45.66, splitLayout.getSplitterPosition(), 0);
    }

    @Test
    void testUpdateSplitterPosition_noInitialPosition() {
        SplitLayout splitLayout = new SplitLayout();

        ComponentUtil.fireEvent(splitLayout, new SplitterDragEndEvent(
                splitLayout, true, "432.68px", "267.32px"));

        Assertions.assertEquals(61.81, splitLayout.getSplitterPosition(), 0.01);
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(SplitLayout.class));
    }
}
