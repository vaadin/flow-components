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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Card component.
 */
@Route(value = "card", layout = MainLayout.class)
@PageTitle("Card | Vaadin Kitchen Sink")
public class CardDemoView extends VerticalLayout {

    public CardDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Card Component"));
        add(new Paragraph("Card provides a container with visual hierarchy for grouping related content."));

        // Basic card
        Card basic = new Card();
        basic.add(new H3("Basic Card"));
        basic.add(new Paragraph("This is a simple card with some text content."));
        addSection("Basic Card", basic);

        // Card with title and description
        Card withTitle = new Card();
        withTitle.setTitle("Card Title");
        withTitle.setSubtitle("Card subtitle or description");
        withTitle.add(new Paragraph("Main content of the card goes here."));
        addSection("With Title and Subtitle", withTitle);

        // Product card example
        Card product = new Card();
        product.setTitle("Premium Widget");
        product.setSubtitle("Best seller");

        Span price = new Span("$99.99");
        price.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

        Paragraph description = new Paragraph("High-quality widget with premium features. Perfect for professional use.");

        HorizontalLayout actions = new HorizontalLayout();
        Button addToCart = new Button("Add to Cart", e -> Notification.show("Added to cart!"));
        addToCart.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button wishlist = new Button("Wishlist", e -> Notification.show("Added to wishlist!"));
        actions.add(addToCart, wishlist);

        product.add(price, description, actions);
        addSection("Product Card Example", product);

        // Info card
        Card info = new Card();
        info.setTitle("System Status");
        info.setSubtitle("Last updated: Just now");

        VerticalLayout statusList = new VerticalLayout();
        statusList.setPadding(false);
        statusList.setSpacing(false);
        statusList.add(createStatusRow("API", "Operational"));
        statusList.add(createStatusRow("Database", "Operational"));
        statusList.add(createStatusRow("CDN", "Operational"));

        info.add(statusList);
        addSection("Status Card", info);

        // User profile card
        Card profile = new Card();
        profile.setTitle("John Doe");
        profile.setSubtitle("Software Engineer");

        VerticalLayout profileInfo = new VerticalLayout();
        profileInfo.setPadding(false);
        profileInfo.add(new Paragraph("Email: john.doe@example.com"));
        profileInfo.add(new Paragraph("Location: San Francisco, CA"));
        profileInfo.add(new Paragraph("Member since: January 2024"));

        profile.add(profileInfo);
        addSection("Profile Card", profile);

        // Stats cards grid
        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setSpacing(true);
        statsGrid.setWidthFull();

        statsGrid.add(createStatCard("Users", "1,234", "+12%"));
        statsGrid.add(createStatCard("Revenue", "$45,678", "+8%"));
        statsGrid.add(createStatCard("Orders", "567", "+23%"));

        addSection("Stats Cards", statsGrid);

        // Card with media placeholder
        Card withMedia = new Card();
        Div mediaPlaceholder = new Div();
        mediaPlaceholder.setText("Image/Media Area");
        mediaPlaceholder.addClassNames(LumoUtility.Background.CONTRAST_10,
                LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER);
        mediaPlaceholder.setHeight("150px");
        mediaPlaceholder.setWidthFull();

        withMedia.add(mediaPlaceholder);
        withMedia.setTitle("Article Title");
        withMedia.add(new Paragraph("Article preview text goes here. This would typically show a brief excerpt from the full article content."));
        withMedia.add(new Button("Read More"));
        addSection("Card with Media Area", withMedia);
    }

    private HorizontalLayout createStatusRow(String service, String status) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Span serviceName = new Span(service);
        Span statusBadge = new Span(status);
        statusBadge.addClassNames(LumoUtility.Background.SUCCESS_10,
                LumoUtility.TextColor.SUCCESS, LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.BorderRadius.SMALL);

        row.add(serviceName, statusBadge);
        return row;
    }

    private Card createStatCard(String title, String value, String change) {
        Card card = new Card();

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.TextColor.SECONDARY);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);

        Span changeSpan = new Span(change);
        changeSpan.addClassNames(LumoUtility.TextColor.SUCCESS, LumoUtility.FontSize.SMALL);

        card.add(titleSpan, valueSpan, changeSpan);
        card.setWidth("200px");
        return card;
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
