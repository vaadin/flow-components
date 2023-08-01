package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-text-area/dirty-state")
public class TextAreaDirtyStatePage extends AbstractDirtyStatePage<TextArea> {
    @Override
    protected TextArea createTestField() {
        return new TextArea();
    }
}
