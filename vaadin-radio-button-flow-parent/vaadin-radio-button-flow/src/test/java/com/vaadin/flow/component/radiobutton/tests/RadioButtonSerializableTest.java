/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class RadioButtonSerializableTest extends ClassesSerializableTest {
    @Test
    public void setItems_addToUI_radioButtonGroupIsSerializable()
            throws Throwable {
        var group = new RadioButtonGroup<>();
        group.setItems("Item 1", "Item 2");

        var ui = new UI();
        UI.setCurrent(ui);
        ui.add(group);

        serializeAndDeserialize(ui);
    }
}
