package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-multi-select-combo-box/dirty-state")
public class MultiSelectComboBoxDirtyStatePage extends AbstractDirtyStatePage<MultiSelectComboBox<String>> {
    @Override
    protected MultiSelectComboBox<String> createTestField() {
        return new MultiSelectComboBox<>();
    }
}
