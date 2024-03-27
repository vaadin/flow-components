/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
