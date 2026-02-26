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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for AppLayout component.
 */
@Route(value = "app-layout", layout = MainLayout.class)
@PageTitle("App Layout | Vaadin Kitchen Sink")
public class AppLayoutDemoView extends VerticalLayout {

    public AppLayoutDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("App Layout Component"));
        add(new Paragraph("AppLayout provides a responsive layout with a header, sidebar drawer, and main content area."));
        add(new Paragraph("Note: This demo app itself uses AppLayout as its main layout. The examples below show the component's features."));

        // AppLayout features explanation
        Div features = new Div();
        features.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM);

        H3 featuresTitle = new H3("Key Features");
        Paragraph featuresList = new Paragraph(
            "- Navbar: Fixed header area for branding and navigation\n" +
            "- Drawer: Collapsible sidebar for navigation menus\n" +
            "- DrawerToggle: Button to open/close the drawer\n" +
            "- Touch-friendly: Supports touch gestures for drawer\n" +
            "- Primary/Secondary sections: Organize navbar content"
        );
        featuresList.getStyle().set("white-space", "pre-line");

        features.add(featuresTitle, featuresList);
        add(features);

        // Drawer modes explanation
        Div modes = new Div();
        modes.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM);

        H3 modesTitle = new H3("Drawer Modes");
        Paragraph modesList = new Paragraph(
            "1. DRAWER: Drawer overlays content (mobile-friendly)\n" +
            "2. OVERLAY: Drawer slides over content\n" +
            "- Use setPrimarySection() to configure which side is primary"
        );
        modesList.getStyle().set("white-space", "pre-line");

        modes.add(modesTitle, modesList);
        add(modes);

        // Usage example
        Div example = new Div();
        example.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM);

        H3 exampleTitle = new H3("Usage Example");
        Paragraph code = new Paragraph(
            "public class MainLayout extends AppLayout {\n" +
            "    public MainLayout() {\n" +
            "        H1 title = new H1(\"My App\");\n" +
            "        addToNavbar(new DrawerToggle(), title);\n" +
            "        \n" +
            "        SideNav nav = new SideNav();\n" +
            "        nav.addItem(new SideNavItem(\"Home\", HomeView.class));\n" +
            "        addToDrawer(nav);\n" +
            "    }\n" +
            "}"
        );
        code.getStyle()
            .set("white-space", "pre")
            .set("font-family", "monospace")
            .set("font-size", "0.875rem")
            .set("background", "var(--lumo-contrast-10pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        example.add(exampleTitle, code);
        add(example);

        // Tips
        Div tips = new Div();
        tips.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM);

        H3 tipsTitle = new H3("Tips");
        Paragraph tipsList = new Paragraph(
            "- Combine with SideNav for navigation menus\n" +
            "- Use Scroller in drawer for long navigation lists\n" +
            "- Add a footer to the navbar for user info/logout\n" +
            "- Consider responsive behavior on different screen sizes"
        );
        tipsList.getStyle().set("white-space", "pre-line");

        tips.add(tipsTitle, tipsList);
        add(tips);
    }
}
