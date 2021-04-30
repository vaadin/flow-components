/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.splitlayout.demo;

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.splitlayout.GeneratedVaadinSplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link SplitLayout} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-split-layout")
public class SplitLayoutView extends DemoView {

    @Override
    public void initView() {
        addHorizontalLayout();
        addVerticalLayout();
        addLayoutCombination();
        addResizeNotificationLayout();
        addInitialSplitterPositionLayout();
        addMinMaxWidthLayout();
        addComponentWithThemeVariant();
    }

    private void addComponentWithThemeVariant() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        SplitLayout layout = new SplitLayout(
                new Label("First content component"),
                new Label("Second content component"));
        layout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);

        add(layout);
        // end-source-example

        addVariantsDemo(() -> {
            return layout;
        }, GeneratedVaadinSplitLayout::addThemeVariants,
                GeneratedVaadinSplitLayout::removeThemeVariants,
                SplitLayoutVariant::getVariantName,
                SplitLayoutVariant.LUMO_SMALL);
    }

    private void addHorizontalLayout() {
        // begin-source-example
        // source-example-heading: Horizontal Split Layout (Default)

        SplitLayout layout = new SplitLayout(
                new Label("First content component"),
                new Label("Second content component"));

        add(layout);
        // end-source-example

        setMinHeightForLayout(layout);
        addCard("Horizontal Split Layout (Default)", layout);
    }

    private void addVerticalLayout() {
        // begin-source-example
        // source-example-heading: Vertical Split Layout
        SplitLayout layout = new SplitLayout();
        layout.setOrientation(Orientation.VERTICAL);
        layout.addToPrimary(new Label("Top content component"));
        layout.addToSecondary(new Label("Bottom content component"));

        add(layout);
        // end-source-example

        setMinHeightForLayout(layout);
        addCard("Vertical Split Layout", layout);
    }

    private void addLayoutCombination() {
        // begin-source-example
        // source-example-heading: Layout Combination
        Label firstLabel = new Label("First content component");
        Label secondLabel = new Label("Second content component");
        Label thirdLabel = new Label("Third content component");

        SplitLayout innerLayout = new SplitLayout();
        innerLayout.setOrientation(Orientation.VERTICAL);
        innerLayout.addToPrimary(secondLabel);
        innerLayout.addToSecondary(thirdLabel);

        SplitLayout layout = new SplitLayout();
        layout.addToPrimary(firstLabel);
        layout.addToSecondary(innerLayout);

        add(layout);
        // end-source-example

        layout.getPrimaryComponent().setId("first-component");
        layout.getSecondaryComponent().setId("nested-layout");
        innerLayout.getPrimaryComponent().setId("second-component");
        innerLayout.getSecondaryComponent().setId("third-component");
        setMinHeightForLayout(layout);
        addCard("Layout Combination", layout);
    }

    private void addResizeNotificationLayout() {
        // begin-source-example
        // source-example-heading: Resize Event
        SplitLayout layout = new SplitLayout();
        layout.addToPrimary(new Label("First content component"));
        layout.addToSecondary(new Label("Second content component"));

        Label message = new Label("Drag and drop the splitter");
        AtomicInteger resizeCounter = new AtomicInteger();
        layout.addSplitterDragendListener(event -> message.setText(
                "SplitLayout Resized " + resizeCounter.incrementAndGet() + " times."));

        add(layout, message);
        // end-source-example

        message.setId("resize-message");
        setMinHeightForLayout(layout);
        addCard("Resize Event", layout, message);
    }

    private void addInitialSplitterPositionLayout() {
        // begin-source-example
        // source-example-heading: Split Layout with Initial Splitter Position
        Label firstLabel = new Label("First content component");
        Label secondLabel = new Label("Second content component");

        SplitLayout layout = new SplitLayout(firstLabel, secondLabel);
        layout.setSplitterPosition(80);

        add(layout);
        // end-source-example

        layout.getPrimaryComponent().setId("initial-sp-first-component");
        layout.getSecondaryComponent().setId("initial-sp-second-component");
        setMinHeightForLayout(layout);
        addCard("Split Layout with Initial Splitter Position", layout);
    }

    private void addMinMaxWidthLayout() {
        // begin-source-example
        // source-example-heading: Split Layout with Minimum and Maximum Widths
        SplitLayout layout = new SplitLayout();
        layout.addToPrimary(new Label("First content component"));
        layout.addToSecondary(new Label("Second content component"));

        layout.setPrimaryStyle("minWidth", "100px");
        layout.setPrimaryStyle("maxWidth", "150px");
        layout.setPrimaryStyle("background", "salmon");

        add(layout);
        // end-source-example

        layout.getPrimaryComponent().setId("min-max-first-component");
        setMinHeightForLayout(layout);
        addCard("Split Layout with Minimum and Maximum Widths", layout);
    }

    private void setMinHeightForLayout(HasStyle layout) {
        layout.getStyle().set("minHeight", "100px");
    }
}
