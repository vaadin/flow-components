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
package com.vaadin.flow.theme.lumo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-lumo-theme/lumo-icon-view")
public class LumoIconView extends Div {

    public LumoIconView() {
        createAllLumoIconsView();
    }

    private void createAllLumoIconsView() {
        HorizontalLayout iconLayout = new HorizontalLayout();
        iconLayout.getStyle().set("flexWrap", "wrap");

        iconLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        for (LumoIcon icon : LumoIcon.values()) {
            iconLayout.add(iconWithName(icon, icon.name()));
        }

        iconLayout.setId("all-lumo-icons");
        addCard("All available Lumo icons", iconLayout);
    }

    private VerticalLayout iconWithName(IconFactory iconFactory, String name) {
        Icon iconComponent = iconFactory.create();
        iconComponent.setSize("50px");
        iconComponent.getStyle().set("color", "#00b4f0").set("marginBottom",
                "3px");
        VerticalLayout iconWithName = new VerticalLayout(iconComponent,
                new Label(name));
        iconWithName.setSizeUndefined();
        iconWithName.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        iconWithName.getStyle().set("margin", "5px").set("width", "140px")
                .set("fontSize", "12px");
        return iconWithName;
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
