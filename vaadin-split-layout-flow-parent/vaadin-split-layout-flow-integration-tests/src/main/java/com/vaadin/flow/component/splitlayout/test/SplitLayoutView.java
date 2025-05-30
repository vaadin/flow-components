/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.splitlayout.test;

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.router.Route;

/**
 * View for {@link SplitLayout}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-split-layout/split-layout")
public class SplitLayoutView extends Div {

    public SplitLayoutView() {
        addLayoutCombination();
        addResizeNotificationLayout();
        addInitialSplitterPositionLayout();
        addMinMaxWidthLayout();
    }

    private void addLayoutCombination() {
        Span first = new Span("First content component");
        Span second = new Span("Second content component");
        Span third = new Span("Third content component");

        SplitLayout innerLayout = new SplitLayout(second, third,
                Orientation.VERTICAL);

        SplitLayout layout = new SplitLayout(first, innerLayout);

        layout.setId("split-layout-combination");
        layout.getPrimaryComponent().setId("first-component");
        layout.getSecondaryComponent().setId("nested-layout");
        innerLayout.getPrimaryComponent().setId("second-component");
        innerLayout.getSecondaryComponent().setId("third-component");
        setMinHeightForLayout(layout);
        addCard("Layout Combination", layout);
    }

    private void addResizeNotificationLayout() {
        SplitLayout layout = new SplitLayout();
        layout.setId("split-layout-resize");
        layout.addToPrimary(new Span("First content component"));
        layout.addToSecondary(new Span("Second content component"));

        Span message = new Span("Drag and drop the splitter");
        AtomicInteger resizeCounter = new AtomicInteger();
        layout.addSplitterDragendListener(
                event -> message.setText("SplitLayout Resized "
                        + resizeCounter.incrementAndGet() + " times."));

        message.setId("resize-message");
        setMinHeightForLayout(layout);
        addCard("Resize Event", layout, message);
    }

    private void addInitialSplitterPositionLayout() {
        Span first = new Span("First content component");
        Span second = new Span("Second content component");

        SplitLayout layout = new SplitLayout(first, second);
        layout.setSplitterPosition(80);

        layout.getPrimaryComponent().setId("initial-sp-first-component");
        layout.getSecondaryComponent().setId("initial-sp-second-component");
        setMinHeightForLayout(layout);
        addCard("Split Layout with Initial Splitter Position", layout);
    }

    private void addMinMaxWidthLayout() {
        SplitLayout layout = new SplitLayout();
        layout.setId("split-layout-min-max");
        layout.addToPrimary(new Span("First content component"));
        layout.addToSecondary(new Span("Second content component"));

        layout.setPrimaryStyle("minWidth", "100px");
        layout.setPrimaryStyle("maxWidth", "150px");
        layout.setPrimaryStyle("background", "salmon");

        layout.getPrimaryComponent().setId("min-max-first-component");
        setMinHeightForLayout(layout);
        addCard("Split Layout with Minimum and Maximum Widths", layout);
    }

    private void setMinHeightForLayout(HasStyle layout) {
        layout.getStyle().set("minHeight", "100px");
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
