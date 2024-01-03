/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.stream.Stream;

public class ComboBoxTest extends ComboBoxBaseTest {
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

    @Override
    protected <TItem> ComboBoxBase<?, TItem, ?> createComboBox(
            Class<TItem> itemClass) {
        return new ComboBox<>();
    }

    @Test
    public void initialValue() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assert.assertNull(comboBox.getValue());
    }

    @Test
    public void setValue() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo");

        Assert.assertEquals("foo", comboBox.getValue());
        // should refresh web components selectedItem property
        JsonObject jsonObject = (JsonObject) comboBox.getElement()
                .getPropertyRaw("selectedItem");
        Assert.assertNotNull(jsonObject);
    }

    @Test
    public void setValueNull_setsNullValue() {
        ComboBox<String> comboBox = new ComboBox<>("1", "2");
        comboBox.setValue("1");
        comboBox.setValue(null);

        Assert.assertNull(comboBox.getValue());
        Assert.assertNull(
                "The selectedItem property must be null when there's no value. "
                        + "Otherwise the 'clear value'-button will be shown.",
                comboBox.getElement().getPropertyRaw("selectedItem"));
    }

    @Test
    public void setValue_updateDataProvider_valueIsReset() {
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue("bar");
        Assert.assertEquals("bar", comboBox.getValue());
        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assert.assertNull(comboBox.getValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void setValue_triggersValueChangeListener() {
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
        Assert.assertEquals("foo", eventCaptor.getValue().getValue());
        Assert.assertFalse(eventCaptor.getValue().isFromClient());
    }

    @Test(expected = IllegalStateException.class)
    public void setValueWithoutItems_throw() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setValue("foo");
    }

    // https://github.com/vaadin/vaadin-flow-components/issues/391
    @Test
    public void setValueWithLazyItems_doesntThrow() {
        final ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(query -> Stream.of("foo", "bar"));
        comboBox.setValue("foo");

        Assert.assertEquals("foo", comboBox.getValue());
    }

    @Test
    public void setValue_disableComboBox_getValue() {
        ComboBox<String> comboBox = new ComboBox<>("foo", "bar", "paa");
        comboBox.setValue("bar");
        comboBox.setEnabled(false);
        Assert.assertEquals("bar", comboBox.getValue());
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
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-combo-box");
        element.setProperty("value", "foo");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(ComboBox.class))
                .thenAnswer(invocation -> new ComboBox());
        ComboBox field = Component.from(element, ComboBox.class);
        Assert.assertEquals("foo", field.getElement().getPropertyRaw("value"));
    }

    @Test
    public void setPrefix_hasPrefix() {
        ComboBox<String> comboBox = new ComboBox<>();
        TestPrefix prefix = new TestPrefix();

        comboBox.setPrefixComponent(prefix);

        Assert.assertEquals(prefix, comboBox.getPrefixComponent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTextAsPrefix_throws() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefixComponent(new Text("Prefix"));
    }

    @Test
    public void implementsInputField() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assert.assertTrue(
                comboBox instanceof InputField<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>, String>);
    }

    @Tag("div")
    private static class TestPrefix extends Component {
    }
}
