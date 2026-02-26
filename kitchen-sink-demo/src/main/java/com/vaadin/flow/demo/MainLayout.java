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
package com.vaadin.flow.demo;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.demo.views.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Main layout for the Kitchen Sink Demo application.
 * Uses AppLayout with a side navigation drawer.
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 title = new H1("Vaadin Kitchen Sink");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Header header = new Header(new DrawerToggle(), title);
        header.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX,
                LumoUtility.Gap.MEDIUM, LumoUtility.Padding.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        // Home
        nav.addItem(new SideNavItem("Home", HomeView.class, VaadinIcon.HOME.create()));

        // Input Components
        SideNavItem inputSection = new SideNavItem("Input Components");
        inputSection.setPrefixComponent(VaadinIcon.KEYBOARD.create());
        inputSection.addItem(new SideNavItem("Button", ButtonDemoView.class));
        inputSection.addItem(new SideNavItem("Text Field", TextFieldDemoView.class));
        inputSection.addItem(new SideNavItem("Text Area", TextAreaDemoView.class));
        inputSection.addItem(new SideNavItem("Number Field", NumberFieldDemoView.class));
        inputSection.addItem(new SideNavItem("Password Field", PasswordFieldDemoView.class));
        inputSection.addItem(new SideNavItem("Email Field", EmailFieldDemoView.class));
        inputSection.addItem(new SideNavItem("Checkbox", CheckboxDemoView.class));
        inputSection.addItem(new SideNavItem("Radio Button", RadioButtonDemoView.class));
        inputSection.addItem(new SideNavItem("Select", SelectDemoView.class));
        inputSection.addItem(new SideNavItem("Combo Box", ComboBoxDemoView.class));
        inputSection.addItem(new SideNavItem("Multi-Select Combo Box", MultiSelectComboBoxDemoView.class));
        inputSection.addItem(new SideNavItem("Date Picker", DatePickerDemoView.class));
        inputSection.addItem(new SideNavItem("Time Picker", TimePickerDemoView.class));
        inputSection.addItem(new SideNavItem("Date Time Picker", DateTimePickerDemoView.class));
        inputSection.addItem(new SideNavItem("Upload", UploadDemoView.class));
        inputSection.addItem(new SideNavItem("Slider", SliderDemoView.class));
        nav.addItem(inputSection);

        // Layout Components
        SideNavItem layoutSection = new SideNavItem("Layout Components");
        layoutSection.setPrefixComponent(VaadinIcon.LAYOUT.create());
        layoutSection.addItem(new SideNavItem("Vertical Layout", VerticalLayoutDemoView.class));
        layoutSection.addItem(new SideNavItem("Horizontal Layout", HorizontalLayoutDemoView.class));
        layoutSection.addItem(new SideNavItem("Form Layout", FormLayoutDemoView.class));
        layoutSection.addItem(new SideNavItem("Split Layout", SplitLayoutDemoView.class));
        layoutSection.addItem(new SideNavItem("App Layout", AppLayoutDemoView.class));
        layoutSection.addItem(new SideNavItem("Accordion", AccordionDemoView.class));
        layoutSection.addItem(new SideNavItem("Details", DetailsDemoView.class));
        layoutSection.addItem(new SideNavItem("Tabs", TabsDemoView.class));
        layoutSection.addItem(new SideNavItem("Card", CardDemoView.class));
        nav.addItem(layoutSection);

        // Data Components
        SideNavItem dataSection = new SideNavItem("Data Components");
        dataSection.setPrefixComponent(VaadinIcon.TABLE.create());
        dataSection.addItem(new SideNavItem("Grid", GridDemoView.class));
        dataSection.addItem(new SideNavItem("Grid Pro", GridProDemoView.class));
        dataSection.addItem(new SideNavItem("Virtual List", VirtualListDemoView.class));
        dataSection.addItem(new SideNavItem("List Box", ListBoxDemoView.class));
        dataSection.addItem(new SideNavItem("CRUD", CrudDemoView.class));
        nav.addItem(dataSection);

        // Visualization
        SideNavItem vizSection = new SideNavItem("Visualization");
        vizSection.setPrefixComponent(VaadinIcon.CHART.create());
        vizSection.addItem(new SideNavItem("Avatar", AvatarDemoView.class));
        vizSection.addItem(new SideNavItem("Badge", BadgeDemoView.class));
        vizSection.addItem(new SideNavItem("Icons", IconsDemoView.class));
        vizSection.addItem(new SideNavItem("Progress Bar", ProgressBarDemoView.class));
        vizSection.addItem(new SideNavItem("Charts", ChartsDemoView.class));
        nav.addItem(vizSection);

        // Interaction Components
        SideNavItem interactionSection = new SideNavItem("Interaction");
        interactionSection.setPrefixComponent(VaadinIcon.HAND.create());
        interactionSection.addItem(new SideNavItem("Dialog", DialogDemoView.class));
        interactionSection.addItem(new SideNavItem("Confirm Dialog", ConfirmDialogDemoView.class));
        interactionSection.addItem(new SideNavItem("Notification", NotificationDemoView.class));
        interactionSection.addItem(new SideNavItem("Context Menu", ContextMenuDemoView.class));
        interactionSection.addItem(new SideNavItem("Menu Bar", MenuBarDemoView.class));
        interactionSection.addItem(new SideNavItem("Popover", PopoverDemoView.class));
        interactionSection.addItem(new SideNavItem("Login", LoginDemoView.class));
        nav.addItem(interactionSection);

        // Navigation Components
        SideNavItem navSection = new SideNavItem("Navigation");
        navSection.setPrefixComponent(VaadinIcon.SITEMAP.create());
        navSection.addItem(new SideNavItem("Side Nav", SideNavDemoView.class));
        nav.addItem(navSection);

        // Advanced Components
        SideNavItem advancedSection = new SideNavItem("Advanced");
        advancedSection.setPrefixComponent(VaadinIcon.COGS.create());
        advancedSection.addItem(new SideNavItem("Rich Text Editor", RichTextEditorDemoView.class));
        advancedSection.addItem(new SideNavItem("Markdown", MarkdownDemoView.class));
        advancedSection.addItem(new SideNavItem("Messages", MessagesDemoView.class));
        advancedSection.addItem(new SideNavItem("Board", BoardDemoView.class));
        advancedSection.addItem(new SideNavItem("Dashboard", DashboardDemoView.class));
        advancedSection.addItem(new SideNavItem("Master Detail", MasterDetailDemoView.class));
        nav.addItem(advancedSection);

        Scroller scroller = new Scroller(nav);
        scroller.addClassNames(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }
}
