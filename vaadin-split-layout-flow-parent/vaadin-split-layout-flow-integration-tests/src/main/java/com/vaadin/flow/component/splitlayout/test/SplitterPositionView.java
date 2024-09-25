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
package com.vaadin.flow.component.splitlayout.test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-split-layout/splitter-position")
public class SplitterPositionView extends Div {

    public static final double INITIAL_POSITION = 70;
    public static final double FINAL_POSITION = 30;

    public SplitterPositionView() {
        NativeButton buttonJava = new NativeButton(
                "Create layout with java API", e -> createLayoutJavaApi());
        buttonJava.setId("createLayoutJavaApi");
        NativeButton buttonElement = new NativeButton(
                "Create layout with element API",
                e -> createLayoutElementApi());
        buttonElement.setId("createLayoutElementApi");
        NativeButton buttonLayoutComponent = new NativeButton(
                "Create layout component", e -> add(new LayoutComponent()));
        buttonLayoutComponent.setId("createLayoutComponent");
        NativeButton nestedSplitLayoutButton = new NativeButton(
                "nested split layout", e -> createNestedSplitLayout());
        nestedSplitLayoutButton.setId("nested-split-layout-button");
        add(buttonJava, buttonElement, buttonLayoutComponent,
                nestedSplitLayoutButton);
    }

    private void createLayoutJavaApi() {
        Span primary = new Span("Primary");
        primary.setId("primaryJavaApi");
        Span secondary = new Span("Secondary");
        secondary.setId("secondaryJavaApi");
        SplitLayout layout = new SplitLayout(primary, secondary);
        layout.setSplitterPosition(INITIAL_POSITION);
        layout.setId("splitLayoutJavaApi");
        NativeButton setSplitterPosition = new NativeButton(
                "set splitter position",
                event -> layout.setSplitterPosition(FINAL_POSITION));
        setSplitterPosition.setId("setSplitPositionJavaApi");

        Span showSplitterPosition = new Span();
        showSplitterPosition.setId("showSplitterPositionJavaApi");
        layout.addSplitterDragendListener(e -> showSplitterPosition
                .setText(String.valueOf(e.getSource().getSplitterPosition())));
        add(setSplitterPosition, layout, showSplitterPosition);
    }

    private void createLayoutElementApi() {
        Span primary = new Span("Primary");
        primary.setId("primaryElementApi");
        Span secondary = new Span("Secondary");
        secondary.setId("secondaryElementApi");
        SplitLayout layout = new SplitLayout();
        layout.setSplitterPosition(INITIAL_POSITION);
        layout.setId("splitLayoutElementApi");
        layout.getElement().appendChild(primary.getElement(),
                secondary.getElement());

        NativeButton setSplitterPosition = new NativeButton(
                "set splitter position",
                event -> layout.setSplitterPosition(FINAL_POSITION));
        setSplitterPosition.setId("setSplitPositionElementApi");

        Span showSplitterPosition = new Span();
        showSplitterPosition.setId("showSplitterPositionElementApi");
        layout.addSplitterDragendListener(e -> showSplitterPosition
                .setText(String.valueOf(e.getSource().getSplitterPosition())));
        add(setSplitterPosition, layout, showSplitterPosition);
    }

    private void createNestedSplitLayout() {
        Span childPrimary = new Span("child-primary");
        Span childSecondary = new Span("child-secondary");
        Div parentSecondary = new Div();

        SplitLayout childLayout = new SplitLayout(childPrimary, childSecondary);
        childLayout.setId("child-layout");
        SplitLayout parentLayout = new SplitLayout(childLayout, parentSecondary,
                SplitLayout.Orientation.VERTICAL);
        parentLayout.setId("parent-layout");
        parentLayout.setHeight("500px");

        Span splitterPosition = new Span();
        splitterPosition.setId("splitter-position");
        NativeButton parentSplitterPositionButton = new NativeButton(
                "parent splitter position", e -> splitterPosition
                        .setText("" + parentLayout.getSplitterPosition()));
        parentSplitterPositionButton.setId("splitter-position-button");
        parentSecondary.add(parentSplitterPositionButton, splitterPosition);

        add(parentLayout);
    }
}
