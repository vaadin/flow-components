/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-button/detach-reattach-disable-on-click-button")
public class DetachReattachDisableOnClickButtonPage extends Div {

    public DetachReattachDisableOnClickButtonPage() {
        Button disableOnClickButton = new Button("Disable on click");
        disableOnClickButton.setId("disable-on-click");
        disableOnClickButton.setDisableOnClick(true);
        disableOnClickButton.addClickListener(event -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                event.getSource().setEnabled(true);
            }
        });

        Button removeFromViewButton = new Button("Remove from view",
                event -> remove(disableOnClickButton));
        removeFromViewButton.setId("remove-from-view");
        Button addToViewButton = new Button("Add to view",
                event -> add(disableOnClickButton));
        addToViewButton.setId("add-to-view");

        add(removeFromViewButton, addToViewButton, disableOnClickButton);
    }
}
