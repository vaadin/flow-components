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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for VerticalLayout component.
 */
@Route(value = "vertical-layout", layout = MainLayout.class)
@PageTitle("Vertical Layout | Vaadin Kitchen Sink")
public class VerticalLayoutDemoView extends VerticalLayout {

    public VerticalLayoutDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Vertical Layout Component"));
        add(new Paragraph("VerticalLayout arranges components in a vertical column."));

        // Basic vertical layout
        VerticalLayout basic = new VerticalLayout();
        basic.add(createBox("Item 1"), createBox("Item 2"), createBox("Item 3"));
        basic.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Basic Vertical Layout", basic);

        // With spacing
        VerticalLayout withSpacing = new VerticalLayout();
        withSpacing.setSpacing(true);
        withSpacing.add(createBox("Spaced 1"), createBox("Spaced 2"), createBox("Spaced 3"));
        withSpacing.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("With Spacing", withSpacing);

        // With padding
        VerticalLayout withPadding = new VerticalLayout();
        withPadding.setPadding(true);
        withPadding.setSpacing(true);
        withPadding.add(createBox("Padded 1"), createBox("Padded 2"), createBox("Padded 3"));
        withPadding.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("With Padding", withPadding);

        // Alignment start
        VerticalLayout alignStart = new VerticalLayout();
        alignStart.setAlignItems(FlexComponent.Alignment.START);
        alignStart.add(createBox("Start"), createBox("Align"), createBox("Left"));
        alignStart.setHeight("200px");
        alignStart.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Align Items: Start", alignStart);

        // Alignment center
        VerticalLayout alignCenter = new VerticalLayout();
        alignCenter.setAlignItems(FlexComponent.Alignment.CENTER);
        alignCenter.add(createBox("Center"), createBox("Aligned"), createBox("Items"));
        alignCenter.setHeight("200px");
        alignCenter.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Align Items: Center", alignCenter);

        // Alignment end
        VerticalLayout alignEnd = new VerticalLayout();
        alignEnd.setAlignItems(FlexComponent.Alignment.END);
        alignEnd.add(createBox("End"), createBox("Align"), createBox("Right"));
        alignEnd.setHeight("200px");
        alignEnd.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Align Items: End", alignEnd);

        // Justify content center
        VerticalLayout justifyCenter = new VerticalLayout();
        justifyCenter.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        justifyCenter.add(createBox("Centered"), createBox("Vertically"));
        justifyCenter.setHeight("300px");
        justifyCenter.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Justify Content: Center", justifyCenter);

        // Justify content between
        VerticalLayout justifyBetween = new VerticalLayout();
        justifyBetween.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        justifyBetween.add(createBox("Top"), createBox("Middle"), createBox("Bottom"));
        justifyBetween.setHeight("300px");
        justifyBetween.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Justify Content: Space Between", justifyBetween);

        // Individual alignment
        VerticalLayout individualAlign = new VerticalLayout();
        individualAlign.setAlignItems(FlexComponent.Alignment.START);
        Div selfCenter = createBox("Self Center");
        individualAlign.setAlignSelf(FlexComponent.Alignment.CENTER, selfCenter);
        Div selfEnd = createBox("Self End");
        individualAlign.setAlignSelf(FlexComponent.Alignment.END, selfEnd);
        individualAlign.add(createBox("Default (Start)"), selfCenter, selfEnd);
        individualAlign.setHeight("200px");
        individualAlign.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("Individual Alignment", individualAlign);

        // Expand
        VerticalLayout withExpand = new VerticalLayout();
        Div expandItem = createBox("Expanded Item");
        withExpand.setFlexGrow(1, expandItem);
        withExpand.add(createBox("Normal"), expandItem, createBox("Normal"));
        withExpand.setHeight("300px");
        withExpand.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        addSection("With Expand", withExpand);
    }

    private Div createBox(String text) {
        Div box = new Div();
        box.setText(text);
        box.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.MEDIUM);
        return box;
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
