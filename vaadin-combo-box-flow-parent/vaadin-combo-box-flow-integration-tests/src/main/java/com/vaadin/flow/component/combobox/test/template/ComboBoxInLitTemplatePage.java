
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
