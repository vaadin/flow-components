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

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroupVariant;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Avatar component.
 */
@Route(value = "avatar", layout = MainLayout.class)
@PageTitle("Avatar | Vaadin Kitchen Sink")
public class AvatarDemoView extends VerticalLayout {

    public AvatarDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Avatar Component"));
        add(new Paragraph("Avatar displays user profile images or initials."));

        // Basic avatar with name
        Avatar basic = new Avatar("John Doe");
        addSection("Basic Avatar (Initials)", basic);

        // Size variants
        HorizontalLayout sizes = new HorizontalLayout();
        sizes.setAlignItems(Alignment.CENTER);

        Avatar xlarge = new Avatar("XL");
        xlarge.addThemeVariants(AvatarVariant.LUMO_XLARGE);

        Avatar large = new Avatar("LG");
        large.addThemeVariants(AvatarVariant.LUMO_LARGE);

        Avatar medium = new Avatar("MD");

        Avatar small = new Avatar("SM");
        small.addThemeVariants(AvatarVariant.LUMO_SMALL);

        Avatar xsmall = new Avatar("XS");
        xsmall.addThemeVariants(AvatarVariant.LUMO_XSMALL);

        sizes.add(xlarge, large, medium, small, xsmall);
        addSection("Size Variants", sizes);

        // With abbreviation
        Avatar abbr = new Avatar();
        abbr.setName("Jane Smith");
        abbr.setAbbreviation("JS");
        addSection("Custom Abbreviation", abbr);

        // Different color indices
        HorizontalLayout colors = new HorizontalLayout();
        colors.setAlignItems(Alignment.CENTER);
        for (int i = 0; i < 7; i++) {
            Avatar colorAvatar = new Avatar("User " + (i + 1));
            colorAvatar.setColorIndex(i);
            colors.add(colorAvatar);
        }
        addSection("Color Variations", colors);

        // Avatar Group
        AvatarGroup group = new AvatarGroup();
        group.add(
            new AvatarGroup.AvatarGroupItem("John Doe"),
            new AvatarGroup.AvatarGroupItem("Jane Smith"),
            new AvatarGroup.AvatarGroupItem("Bob Johnson"),
            new AvatarGroup.AvatarGroupItem("Alice Williams"),
            new AvatarGroup.AvatarGroupItem("Charlie Brown")
        );
        addSection("Avatar Group", group);

        // Avatar Group with max items
        AvatarGroup limitedGroup = new AvatarGroup();
        limitedGroup.setMaxItemsVisible(3);
        limitedGroup.add(
            new AvatarGroup.AvatarGroupItem("Person 1"),
            new AvatarGroup.AvatarGroupItem("Person 2"),
            new AvatarGroup.AvatarGroupItem("Person 3"),
            new AvatarGroup.AvatarGroupItem("Person 4"),
            new AvatarGroup.AvatarGroupItem("Person 5"),
            new AvatarGroup.AvatarGroupItem("Person 6")
        );
        addSection("Avatar Group (Max 3 Visible)", limitedGroup);

        // Small Avatar Group
        AvatarGroup smallGroup = new AvatarGroup();
        smallGroup.addThemeVariants(AvatarGroupVariant.LUMO_SMALL);
        smallGroup.add(
            new AvatarGroup.AvatarGroupItem("Alice"),
            new AvatarGroup.AvatarGroupItem("Bob"),
            new AvatarGroup.AvatarGroupItem("Charlie")
        );
        addSection("Small Avatar Group", smallGroup);

        // Large Avatar Group
        AvatarGroup largeGroup = new AvatarGroup();
        largeGroup.addThemeVariants(AvatarGroupVariant.LUMO_LARGE);
        largeGroup.add(
            new AvatarGroup.AvatarGroupItem("Diana"),
            new AvatarGroup.AvatarGroupItem("Edward"),
            new AvatarGroup.AvatarGroupItem("Fiona")
        );
        addSection("Large Avatar Group", largeGroup);
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
