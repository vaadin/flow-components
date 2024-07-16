/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/combo-box-in-lit-template-page")
public class ComboBoxInLitTemplatePage extends FlexLayout {

    public ComboBoxInLitTemplatePage() {
        setWidthFull();
        add(new ComboBoxInLitTemplate());
    }
}
