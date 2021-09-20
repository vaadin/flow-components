package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/combo-box-in-template-initial-value")
public class ComboBoxTemplateInitialValuePage extends FlexLayout {

    public ComboBoxTemplateInitialValuePage() {
        setWidthFull();
        add(new ComboBoxTemplateInitialValue());
    }
}
