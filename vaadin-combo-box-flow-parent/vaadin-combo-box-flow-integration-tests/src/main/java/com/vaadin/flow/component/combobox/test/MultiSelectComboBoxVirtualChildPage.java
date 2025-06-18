/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/virtual-child")
public class MultiSelectComboBoxVirtualChildPage extends Div {
    public MultiSelectComboBoxVirtualChildPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        // Add the component as a virtual child, which will initialize the
        // connector, but does not add the element to the DOM, and does not
        // finalize the Polymer element.
        // This covers scenarios like adding the component to an unopened
        // dialog, or rendering it with a Polymer or Lit template.
        getElement().appendVirtualChild(comboBox.getElement());
    }
}
