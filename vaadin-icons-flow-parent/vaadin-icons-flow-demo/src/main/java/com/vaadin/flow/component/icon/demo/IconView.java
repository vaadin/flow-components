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
package com.vaadin.flow.component.icon.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Icon} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-icons")
public class IconView extends DemoView {

    @Override
    public void initView() {
        createBasicIconsView();
        createStyledIconView();
        createClickableIconsView();
        createAllIconsView();
    }

    private void createBasicIconsView() {
        // begin-source-example
        // source-example-heading: Creating a new icon
        // Creating an icon from the Vaadin icons collection
        Icon close = new Icon(VaadinIcon.CLOSE);

        // Creating an icon from the Lumo icons collection
        IronIcon clock = new IronIcon("lumo", "clock");

        // end-source-example

        close.getStyle().set("marginRight", "5px");
        addCard("Creating a new icon",
                new HorizontalLayout(close, clock));

        close.setId("close-icon");
        clock.setId("clock-icon");
    }

    private void createStyledIconView() {
        // begin-source-example
        // source-example-heading: Styling an icon
        Icon logo = new Icon(VaadinIcon.VAADIN_H);
        logo.setSize("100px");
        logo.setColor("orange");
        // end-source-example

        addCard("Styling an icon", logo);

        logo.setId("logo-icon");
    }

    private void createClickableIconsView() {
        // begin-source-example
        // source-example-heading: Clickable icons
        Div message = new Div();
        Icon logoV = new Icon(VaadinIcon.VAADIN_V);
        logoV.getStyle().set("cursor", "pointer");
        logoV.addClickListener(
                event -> message.setText("The VAADIN_V icon was clicked!"));

        Icon logoH = new Icon(VaadinIcon.VAADIN_H);
        logoH.getStyle().set("cursor", "pointer");
        logoH.addClickListener(
                event -> message.setText("The VAADIN_H icon was clicked!"));
        // end-source-example

        addCard("Clickable icons", new HorizontalLayout(logoV, logoH), message);

        logoV.setId("clickable-v-icon");
        logoH.setId("clickable-h-icon");
        message.setId("clickable-message");
    }

    private void createAllIconsView() {
        HorizontalLayout iconLayout = new HorizontalLayout();
        iconLayout.getStyle().set("flexWrap", "wrap");

        iconLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        for (VaadinIcon icon : VaadinIcon.values()) {
            Icon iconComponent = icon.create();
            iconComponent.setSize("50px");
            iconComponent.getStyle().set("color", "#00b4f0").set("marginBottom",
                    "3px");
            VerticalLayout iconWithName = new VerticalLayout(iconComponent,
                    new Label(icon.name()));
            iconWithName.setSizeUndefined();
            iconWithName
                    .setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            iconWithName.getStyle().set("margin", "5px").set("width", "140px")
                    .set("fontSize", "12px");
            iconLayout.add(iconWithName);
        }

        iconLayout.setId("all-icons");
        addCard("All available icons", iconLayout);
    }
}
