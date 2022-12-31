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
