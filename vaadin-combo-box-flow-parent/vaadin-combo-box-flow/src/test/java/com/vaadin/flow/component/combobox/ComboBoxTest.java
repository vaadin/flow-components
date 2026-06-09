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
package com.vaadin.flow.component.combobox;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.ObjectNode;

class ComboBoxTest extends ComboBoxBaseTest {

    private enum Category {
        CATEGORY_1, CATEGORY_2, CATEGORY_3;
    }

    private static class Bean {
        Category category;

        public Category getCategory() {
            return category;
        }

        void setCategory(Category category) {
            this.category = category;
        }
    }

    @Override
    protected <TItem> ComboBoxBase<?, TItem, ?> createComboBox(
            Class<TItem> itemClass) {
        return new ComboBox<>();
    }

    @Test
    void initialValue() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assertions.assertNull(comboBox.getValue());
    }

    @Test
    void initialPropertyValue() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assertions.assertEquals("", comboBox.getElement().getProperty("value"));
    }

    @Test
    void setValue() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo");

        Assertions.assertEquals("foo", comboBox.getValue());
        // should refresh web components selectedItem property
        ObjectNode jsonObject = (ObjectNode) comboBox.getElement()
                .getPropertyRaw("selectedItem");
        Assertions.assertNotNull(jsonObject);
    }

    @Test
    void setValueNull_setsNullValue() {
        ComboBox<String> comboBox = new ComboBox<>("1", "2");
        comboBox.setValue("1");
        comboBox.setValue(null);

        Assertions.assertNull(comboBox.getValue());
        Assertions.assertNull(
                comboBox.getElement().getPropertyRaw("selectedItem"),
                "The selectedItem property must be null when there's no value. "
                        + "Otherwise the 'clear value'-button will be shown.");
    }

    @Test
    void setValue_updateDataProvider_valueIsReset() {
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue("bar");
        Assertions.assertEquals("bar", comboBox.getValue());
        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assertions.assertNull(comboBox.getValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void setValue_triggersValueChangeListener() {
        ComboBox<String> comboBox = new ComboBox<>();
        HasValue.ValueChangeListener listener = Mockito
                .mock(HasValue.ValueChangeListener.class);
        ArgumentCaptor<HasValue.ValueChangeEvent> eventCaptor = ArgumentCaptor
                .forClass(HasValue.ValueChangeEvent.class);

        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.addValueChangeListener(listener);
        comboBox.setValue("foo");

        Mockito.verify(listener, Mockito.times(1))
                .valueChanged(eventCaptor.capture());
        Assertions.assertEquals("foo", eventCaptor.getValue().getValue());
        Assertions.assertFalse(eventCaptor.getValue().isFromClient());
    }

    @Test
    void setValueWithoutItems_throw() {
        ComboBox<String> combo = new ComboBox<>();
        Assertions.assertThrows(IllegalStateException.class,
                () -> combo.setValue("foo"));
    }

    // https://github.com/vaadin/vaadin-flow-components/issues/391
    @Test
    void setValueWithLazyItems_doesntThrow() {
        final ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(query -> Stream.of("foo", "bar"));
        comboBox.setValue("foo");

        Assertions.assertEquals("foo", comboBox.getValue());
    }

    @Test
    void setValue_disableComboBox_getValue() {
        ComboBox<String> comboBox = new ComboBox<>("foo", "bar", "paa");
        comboBox.setValue("bar");
        comboBox.setEnabled(false);
        Assertions.assertEquals("bar", comboBox.getValue());
    }

    @Test
    void boxWithBinderAndEnums_readBean_valueIsUpdated() {
        ComboBox<Category> combo = new ComboBox<>();
        combo.setItemLabelGenerator(Category::name);
        combo.setItems(Category.values());
        Assertions.assertNull(combo.getValue());

        Binder<Bean> binder = new Binder<>();
        binder.forField(combo).bind(Bean::getCategory, Bean::setCategory);

        Bean bean = new Bean();
        bean.setCategory(Category.CATEGORY_2);
        binder.readBean(bean);
        Assertions.assertEquals(Category.CATEGORY_2, combo.getValue());

        Assertions.assertFalse(binder.hasChanges());

        bean.setCategory(Category.CATEGORY_3);
        binder.readBean(bean);
        Assertions.assertEquals(Category.CATEGORY_3, combo.getValue());
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-combo-box");
        element.setProperty("value", "foo");

        Instantiator instantiator = Mockito.mock(Instantiator.class);
        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(ComboBox.class))
                .thenAnswer(invocation -> new ComboBox());
        ComboBox field = Component.from(element, ComboBox.class);
        Assertions.assertEquals("foo",
                field.getElement().getPropertyRaw("value"));
    }

    @Test
    void setPrefix_hasPrefix() {
        ComboBox<String> comboBox = new ComboBox<>();
        TestPrefix prefix = new TestPrefix();

        comboBox.setPrefixComponent(prefix);

        Assertions.assertEquals(prefix, comboBox.getPrefixComponent());
    }

    @Test
    void setTextAsPrefix_throws() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> comboBox.setPrefixComponent(new Text("Prefix")));
    }

    @Test
    void implementsInputField() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assertions.assertTrue(
                comboBox instanceof InputField<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>, String>);
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(ComboBox.class));
    }

    @Test
    void setOverlayWidth() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setOverlayWidth(null);
        Assertions.assertNull(
                comboBox.getStyle().get("--vaadin-combo-box-overlay-width"));
        comboBox.setOverlayWidth("30em");
        Assertions.assertEquals("30em",
                comboBox.getStyle().get("--vaadin-combo-box-overlay-width"));
        comboBox.setOverlayWidth(-1, Unit.EM);
        Assertions.assertNull(
                comboBox.getStyle().get("--vaadin-combo-box-overlay-width"));
        comboBox.setOverlayWidth(100, Unit.PIXELS);
        Assertions.assertEquals("100.0px",
                comboBox.getStyle().get("--vaadin-combo-box-overlay-width"));
    }

    @Test
    void focusSelectedItem_defaultsToFalse() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assertions.assertFalse(comboBox.isFocusSelectedItem());
    }

    @Test
    void setFocusSelectedItem_isFocusSelectedItem() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setFocusSelectedItem(true);
        Assertions.assertTrue(comboBox.isFocusSelectedItem());
        comboBox.setFocusSelectedItem(false);
        Assertions.assertFalse(comboBox.isFocusSelectedItem());
    }

    @Test
    void focusSelectedItem_convertedFilterListDataProvider_scrollsToSelectedItem() {
        // withConvertedFilter wraps the ListDataProvider in a provider that is
        // in-memory but not a ListDataProvider. Opening the dropdown must
        // resolve the selected item's index without failing.
        ComboBox<String> comboBox = new ComboBox<>();
        ui.add(comboBox);
        ListDataProvider<String> dataProvider = DataProvider
                .ofCollection(List.of("A", "B", "C", "D"));
        comboBox.setItems(dataProvider.withConvertedFilter(
                filterText -> item -> item.contains(filterText)));
        comboBox.setFocusSelectedItem(true);
        comboBox.setValue("C");
        comboBox.setOpened(true);

        var invocations = ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("scrollToIndex"))
                .toList();
        Assertions.assertEquals(1, invocations.size());
        // Parameter 0 is the target element, parameter 1 is the item index
        Assertions.assertEquals(2, invocations.get(0).getParameters().get(1));
    }

    @Test
    void setFilterTimeout_getFilterTimeout() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assertions.assertEquals(500, comboBox.getFilterTimeout());
        Assertions.assertEquals(500,
                comboBox.getElement().getProperty("_filterTimeout", 0));

        comboBox.setFilterTimeout(750);
        Assertions.assertEquals(750, comboBox.getFilterTimeout());
        Assertions.assertEquals(750,
                comboBox.getElement().getProperty("_filterTimeout", 0));
    }

    @Tag("div")
    private static class TestPrefix extends Component {
    }
}
