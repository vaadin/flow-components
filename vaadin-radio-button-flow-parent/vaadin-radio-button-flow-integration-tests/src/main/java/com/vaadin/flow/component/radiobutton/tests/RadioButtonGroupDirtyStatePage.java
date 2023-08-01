package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-radio-button-group/dirty-state")
public class RadioButtonGroupDirtyStatePage extends AbstractDirtyStatePage<RadioButtonGroup<String>> {
    @Override
    protected RadioButtonGroup<String> createTestField() {
        return new RadioButtonGroup<>();
    }
}
