/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.radiobutton.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.testutil.ClassesSerializableTest;
import com.vaadin.tests.MockUIExtension;

class RadioButtonSerializableTest extends ClassesSerializableTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    @SuppressWarnings("unchecked")
    void setItems_addToUI_serializeAndDeserialize_itemCountChangeEventFiresOnCopy()
            throws Throwable {
        // Use a UI without a session, as the mocked session is not
        // serializable. Removing the session from a UI detaches the UI, and a
        // detached UI hides
        // https://github.com/vaadin/flow-components/issues/6555
        var serializableUi = new UI();
        var group = new RadioButtonGroup<String>();
        group.setItems("Item 1", "Item 2");
        serializableUi.add(group);

        Object[] copies = serializeAndDeserialize(
                new Object[] { serializableUi, group });
        var uiCopy = (UI) copies[0];
        var groupCopy = (RadioButtonGroup<String>) copies[1];
        // Running the pending event needs a session
        uiCopy.getInternals().setSession(ui.getSession());
        List<Integer> itemCounts = new ArrayList<>();
        groupCopy.getGenericDataView().addItemCountChangeListener(
                event -> itemCounts.add(event.getItemCount()));
        uiCopy.getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();

        Assertions.assertEquals(List.of(2), itemCounts);
    }
}
