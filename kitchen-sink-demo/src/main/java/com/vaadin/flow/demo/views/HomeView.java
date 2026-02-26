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
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Home view for the Kitchen Sink Demo application.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Vaadin Kitchen Sink")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("Vaadin Kitchen Sink Demo");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Paragraph intro = new Paragraph(
                "Welcome to the Vaadin Kitchen Sink Demo! This application showcases all Vaadin Flow " +
                "components and their features. Use the navigation menu on the left to explore different " +
                "components and see them in action.");
        intro.addClassNames(LumoUtility.FontSize.LARGE);

        H2 categoriesTitle = new H2("Component Categories");

        Div categories = new Div();
        categories.addClassNames(LumoUtility.Display.GRID, LumoUtility.Gap.MEDIUM);
        categories.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(250px, 1fr))");

        categories.add(
            createCategoryCard("Input Components",
                "Buttons, text fields, checkboxes, select boxes, date/time pickers, and more."),
            createCategoryCard("Layout Components",
                "Vertical/horizontal layouts, form layouts, split layouts, tabs, accordion, and cards."),
            createCategoryCard("Data Components",
                "Grid, Grid Pro, Virtual List, List Box, and CRUD components for displaying data."),
            createCategoryCard("Visualization",
                "Avatars, badges, icons, progress bars, and charts for visual representation."),
            createCategoryCard("Interaction",
                "Dialogs, notifications, context menus, menu bars, popovers, and login forms."),
            createCategoryCard("Navigation",
                "Side navigation and related navigation components."),
            createCategoryCard("Advanced",
                "Rich text editor, markdown, messages, board, dashboard, and master-detail layouts.")
        );

        add(title, intro, categoriesTitle, categories);
    }

    private Div createCategoryCard(String title, String description) {
        Div card = new Div();
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Padding.MEDIUM);

        H2 cardTitle = new H2(title);
        cardTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE,
                LumoUtility.Margin.Bottom.SMALL);

        Paragraph cardDesc = new Paragraph(description);
        cardDesc.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.SECONDARY);

        card.add(cardTitle, cardDesc);
        return card;
    }
}
