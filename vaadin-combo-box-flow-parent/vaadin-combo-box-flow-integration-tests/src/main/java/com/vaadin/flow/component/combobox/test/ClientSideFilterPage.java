/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clientside-filter")
public class ClientSideFilterPage extends Div {

    public ClientSideFilterPage() {
        ComboBox<String> cb = new ComboBox<>("Choose option", "Option 2",
                "Option 3", "Option 4", "Option 5");
        this.add(cb);
        cb.focus();

        this.add(new Hr());

        ComboBox<String> testBox = new ComboBox<>("Browsers");
        testBox.setItems("Google Chrome", "Mozilla Firefox", "Opera",
                "Apple Safari", "Microsoft Edge");
        this.add(testBox);

    }
}
