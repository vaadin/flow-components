/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class ComboboxSerializableTest extends ClassesSerializableTest {
    @Test
    public void setItems_callSetRequestedRange_comboBoxSerializable()
            throws Throwable {
        final ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(List.of("Item 1", "Item 2"));
        callSetRequestedRange(comboBox, 0, 2, "");
        serializeAndDeserialize(comboBox);
    }

    private void callSetRequestedRange(ComboBox<String> comboBox, int start,
            int length, String filter)
            throws NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method method = ComboBoxBase.class.getDeclaredMethod(
                "setRequestedRange", int.class, int.class, String.class);
        method.setAccessible(true);
        method.invoke(comboBox, start, length, filter);
    }
}
