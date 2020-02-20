package com.vaadin.flow.component.icons;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * View integration tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-icons")
public class IconView extends VerticalLayout {

    public IconView() {
        super();
        createBasicIconsView();
        createStyledIconView();
        createClickableIconsView();
        createAllIconsView();
    }

    private void createBasicIconsView() {
        Icon edit = new Icon(VaadinIcon.EDIT);
        Icon close = VaadinIcon.CLOSE.create();

        edit.getStyle().set("marginRight", "5px");
        add(new HorizontalLayout(edit, close));

        edit.setId("edit-icon");
        close.setId("close-icon");
    }

    private void createStyledIconView() {
        Icon logo = new Icon(VaadinIcon.VAADIN_H);
        logo.setSize("100px");
        logo.setColor("orange");

        add(logo);

        logo.setId("logo-icon");
    }

    private void createClickableIconsView() {
        Div message = new Div();
        Icon logoV = new Icon(VaadinIcon.VAADIN_V);
        logoV.getStyle().set("cursor", "pointer");
        logoV.addClickListener(
                event -> message.setText("The VAADIN_V icon was clicked!"));

        Icon logoH = new Icon(VaadinIcon.VAADIN_H);
        logoH.getStyle().set("cursor", "pointer");
        logoH.addClickListener(
                event -> message.setText("The VAADIN_H icon was clicked!"));

        add(new HorizontalLayout(logoV, logoH), message);

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
        add(iconLayout);
    }
}
