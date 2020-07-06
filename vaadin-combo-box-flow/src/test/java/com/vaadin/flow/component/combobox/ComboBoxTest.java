/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.bean.TestItem;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

public class ComboBoxTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static class TestComboBox extends ComboBox<String> {

        private List<String> items;

        @Override
        public void setDataProvider(DataProvider<String, ?> dataProvider) {
            super.setDataProvider(dataProvider);
            items = new ArrayList<>(
                    ((ListDataProvider<String>) dataProvider).getItems());
        }

        @Override
        void runBeforeClientResponse(SerializableConsumer<UI> command) {
            command.accept(new UI());
        }
    }

    private enum Category {
        CATEGORY_1, CATEGORY_2, CATEGORY_3;
    }

    private static class Bean {
        Category category;

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }
    }

    @Test
    public void setItems_jsonItemsAreSet() {
        TestComboBox comboBox = new TestComboBox();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assert.assertEquals(2, comboBox.items.size());
        assertItem(comboBox, 0, "foo");
        assertItem(comboBox, 1, "bar");
    }

    @Test
    public void setInvalidValue_throw() {
        expectIllegalArgumentException(
                "The provided value is not part of ComboBox: invalid");
        TestComboBox comboBox = new TestComboBox();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue("invalid");
    }

    @Test
    public void updateDataProvider_valueIsReset() {
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue("bar");
        Assert.assertEquals("bar", comboBox.getValue());
        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assert.assertNull(comboBox.getValue());
    }

    @Test
    public void setValue_disableComboBox_getValue() {
        ComboBox<String> comboBox = new ComboBox<>("foo", "bar", "paa");
        comboBox.setValue("bar");
        comboBox.setEnabled(false);
        assertEquals("bar", comboBox.getValue());
    }

    @Test
    public void setNull_thrownException() {
        expectNullPointerException("The data provider can not be null");
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setDataProvider(null);
    }

    @Test
    public void nullItemGenerator_throw() {
        expectNullPointerException("The item label generator can not be null");
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setItemLabelGenerator(null);
    }

    @Test
    public void labelItemGeneratorReturnsNull_throw() {
        expectIllegalStateException(
                "Got 'null' as a label value for the item 'foo'. 'ItemLabelGenerator' instance may not return 'null' values");
        TestComboBox comboBox = new TestComboBox();
        comboBox.setItemLabelGenerator(obj -> null);
        comboBox.setItems(Arrays.asList("foo", "bar"));
    }

    @Test
    public void boxWithBinderAndEnums_readBean_valueIsUpdated() {
        ComboBox<Category> combo = new ComboBox<>();
        combo.setItemLabelGenerator(Category::name);
        combo.setItems(Category.values());
        Assert.assertNull(combo.getValue());

        Binder<Bean> binder = new Binder<>();
        binder.forField(combo).bind(Bean::getCategory, Bean::setCategory);

        Bean bean = new Bean();
        bean.setCategory(Category.CATEGORY_2);
        binder.readBean(bean);
        Assert.assertEquals(Category.CATEGORY_2, combo.getValue());

        Assert.assertFalse(binder.hasChanges());

        bean.setCategory(Category.CATEGORY_3);
        binder.readBean(bean);
        Assert.assertEquals(Category.CATEGORY_3, combo.getValue());
    }

    @Test
    public void setValue_selectionPropertySet_listenersAreNotified() {
        ComboBox<Category> combo = new ComboBox<>();
        combo.setItems(Category.values());

        AtomicReference<Category> selected = new AtomicReference<>();
        combo.addValueChangeListener(event -> selected.set(combo.getValue()));

        combo.setValue(Category.CATEGORY_2);

        Assert.assertEquals(Category.CATEGORY_2, selected.get());
    }

    // Ensure https://github.com/vaadin/vaadin-combo-box-flow/issues/36 does not
    // reoccur
    @Test
    public void ensureComboBoxIsFocusable() {
        Assert.assertTrue("ComboBox should be focusable",
                Focusable.class.isAssignableFrom(ComboBox.class));
    }

    @Test
    public void setEnabled() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEnabled(true);
        Assert.assertTrue(comboBox.isEnabled());
        comboBox.setEnabled(false);
        Assert.assertFalse(comboBox.isEnabled());
    }

    @Test
    public void addCustomValueSetListener_customValueIsAllowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.addCustomValueSetListener(e -> {
        });
        Assert.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_removeListener_customValueIsDisallowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        registration.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_disableCustomValue_customValueIsDisallowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });

        comboBox.setAllowCustomValue(false);
        Assert.assertFalse(comboBox.isAllowCustomValue());

        // nothing has changed when the listener is removed
        registration.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_addTwoListeners_removeListenerSeveralTimes_customValueIsAllowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        comboBox.addCustomValueSetListener(e -> {
        });
        // remove the first listener
        registration.remove();
        Assert.assertTrue(comboBox.isAllowCustomValue());

        // removes the fist listener one more time which is no-op
        registration.remove();
        Assert.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_addTwoListeners_removeListeners_customValueIsDisallowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration1 = comboBox.addCustomValueSetListener(e -> {
        });
        Registration registration2 = comboBox.addCustomValueSetListener(e -> {
        });
        // remove the first listener
        registration1.remove();
        Assert.assertTrue(comboBox.isAllowCustomValue());

        // removes the second listener
        registration2.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void setValue_nonMatchingId_IllegalArgumentException() {
        List<TestItem> list = Arrays.asList(new TestItem(1, "a", "First"),
                new TestItem(2, "b", "Second"), new TestItem(3, "c", "Third"));

        expectIllegalArgumentException(
                "The provided value is not part of ComboBox:");
        ComboBox comboBox = new ComboBox();
        comboBox.setDataProvider(new ListDataProvider<TestItem>(list) {
            @Override
            public Object getId(TestItem item) {
                return item.getId();
            }
        });
        comboBox.setValue(new TestItem(0, "b", ""));
    }

    @Test
    public void setValue_nonExistingObject_IllegalArgumentException() {
        List<TestItem> list = Arrays.asList(new TestItem(1, "a", "First"),
                new TestItem(2, "b", "Second"), new TestItem(3, "c", "Third"));

        expectIllegalArgumentException(
                "The provided value is not part of ComboBox:");
        ComboBox comboBox = new ComboBox();
        comboBox.setItems(list);
        comboBox.setValue(new TestItem(2, "bbb", ""));
    }

    private void assertItem(TestComboBox comboBox, int index, String caption) {
        String value1 = comboBox.items.get(index);
        Assert.assertEquals(caption, value1);
    }

    private void expectIllegalArgumentException(String expectedMessage) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(expectedMessage);
    }

    private void expectNullPointerException(String expectedMessage) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(expectedMessage);
    }

    private void expectIllegalStateException(String expectedMessage) {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(expectedMessage);
    }
}
