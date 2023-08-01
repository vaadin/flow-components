package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-combo-box/dirty-state")
public class ComboBoxDirtyStatePage extends AbstractDirtyStatePage<ComboBox<String>> {
    @Override
    protected ComboBox<String> createTestField() {
        return new ComboBox<>();
    }
}
