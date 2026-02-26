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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for SplitLayout component.
 */
@Route(value = "split-layout", layout = MainLayout.class)
@PageTitle("Split Layout | Vaadin Kitchen Sink")
public class SplitLayoutDemoView extends VerticalLayout {

    public SplitLayoutDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Split Layout Component"));
        add(new Paragraph("SplitLayout divides content into two resizable areas."));

        // Horizontal split
        SplitLayout horizontal = new SplitLayout();
        horizontal.addToPrimary(createPane("Primary (Left)"));
        horizontal.addToSecondary(createPane("Secondary (Right)"));
        horizontal.setHeight("200px");
        horizontal.setWidthFull();
        addSection("Horizontal Split (Default)", horizontal);

        // Vertical split
        SplitLayout vertical = new SplitLayout();
        vertical.setOrientation(SplitLayout.Orientation.VERTICAL);
        vertical.addToPrimary(createPane("Primary (Top)"));
        vertical.addToSecondary(createPane("Secondary (Bottom)"));
        vertical.setHeight("300px");
        vertical.setWidthFull();
        addSection("Vertical Split", vertical);

        // Custom split position
        SplitLayout customPosition = new SplitLayout();
        customPosition.addToPrimary(createPane("30%"));
        customPosition.addToSecondary(createPane("70%"));
        customPosition.setSplitterPosition(30);
        customPosition.setHeight("200px");
        customPosition.setWidthFull();
        addSection("Custom Split Position (30/70)", customPosition);

        // Minimal variant
        SplitLayout minimal = new SplitLayout();
        minimal.addThemeVariants(SplitLayoutVariant.LUMO_MINIMAL);
        minimal.addToPrimary(createPane("Primary"));
        minimal.addToSecondary(createPane("Secondary"));
        minimal.setHeight("200px");
        minimal.setWidthFull();
        addSection("Minimal Variant", minimal);

        // Small variant
        SplitLayout small = new SplitLayout();
        small.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
        small.addToPrimary(createPane("Primary"));
        small.addToSecondary(createPane("Secondary"));
        small.setHeight("200px");
        small.setWidthFull();
        addSection("Small Variant", small);

        // Nested splits
        SplitLayout nested = new SplitLayout();
        nested.addToPrimary(createPane("Left"));
        SplitLayout innerSplit = new SplitLayout();
        innerSplit.setOrientation(SplitLayout.Orientation.VERTICAL);
        innerSplit.addToPrimary(createPane("Top Right"));
        innerSplit.addToSecondary(createPane("Bottom Right"));
        nested.addToSecondary(innerSplit);
        nested.setHeight("300px");
        nested.setWidthFull();
        addSection("Nested Split Layouts", nested);
    }

    private Div createPane(String text) {
        Div pane = new Div();
        pane.setText(text);
        pane.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER);
        pane.setSizeFull();
        return pane;
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
