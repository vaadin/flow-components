
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;

@Route("vaadin-button/icon-button")
public class IconForButtonPage extends Div {

    public IconForButtonPage() {
        Button button = new Button("Button On SlotPrefix");
        Icon icon = new Icon(VaadinIcon.BULLSEYE);
        icon.getElement().setAttribute("slot", "prefix");
        button.setIcon(icon);

        button.addClickListener(even -> button.setText("Updated text"));

        add(button);
    }
}
