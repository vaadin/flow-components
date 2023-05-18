/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.*;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InputFieldTest {

    private TestComponent component;
    private UI ui;

    @Before
    public void setup() {
        component = new TestComponent();
        ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void default_doesNotHaveTooltipElement() {
        Assert.assertFalse(getTooltipElement(component).isPresent());
    }

    @Test
    public void getTooltip_hasTooltipElement() {
        var tooltip = component.getTooltip();
        Assert.assertNotNull(tooltip);
        Assert.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    public void getTooltip_hasNoText() {
        var tooltip = component.getTooltip();
        Assert.assertNull(tooltip.getText());
    }

    @Test
    public void setTooltipText_hasTooltipElement() {
        component.setTooltipText("foo");
        Assert.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    public void setTooltipText_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        Assert.assertEquals(tooltip, component.getTooltip());
    }

    @Test
    public void setTooltipTextAgain_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        var tooltip2 = component.setTooltipText("bar");
        Assert.assertEquals(tooltip, tooltip2);
        Assert.assertEquals("bar", component.getTooltip().getText());
    }

    @Test
    public void setTooltipTextNull_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        var tooltip2 = component.setTooltipText(null);
        Assert.assertEquals(tooltip, tooltip2);
        Assert.assertEquals(null, component.getTooltip().getText());
    }

    @Test
    public void setTooltipText_tooltipHasText() {
        component.setTooltipText("foo");
        Assert.assertEquals("foo",
                getTooltipElement(component).get().getProperty("text"));
    }

    @Test
    public void setTooltipText_tooltipHasSlot() {
        component.setTooltipText("foo");
        Assert.assertEquals("tooltip",
                getTooltipElement(component).get().getAttribute("slot"));
    }

    @Test
    public void setTooltipTextAgain_hasOneTooltipElement() {
        component.setTooltipText("foo");
        component.setTooltipText("bar");
        Assert.assertEquals(1, getTooltipElements(component).count());
    }

    private Optional<Element> getTooltipElement(HasTooltip component) {
        return getTooltipElements(component).findFirst();
    }

    private Stream<Element> getTooltipElements(HasTooltip component) {
        return component.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

    @Test
    public void enabledComponent_isEnabledReturnsTrue() {
        TestComponent component = new TestComponent();

        Assert.assertTrue(component.isEnabled());
    }

    @Test
    public void explicitlyDisabledComponent_isEnabledReturnsFalse() {
        TestComponent component = new TestComponent();
        component.setEnabled(false);

        Assert.assertFalse(component.isEnabled());
    }

    @Test
    public void implicitlyDisabledComponent_isEnabledReturnsFalse() {
        TestComponent component = new TestComponent();

        TestComponent parent = new TestComponent();
        parent.setEnabled(false);

        parent.add(component);

        Assert.assertFalse(component.isEnabled());
    }

    @Test
    public void implicitlyDisabledComponent_detach_componentBecomesEnabled() {
        TestComponent component = new TestComponent();

        TestComponent parent = new TestComponent();
        parent.add(component);

        parent.setEnabled(false);

        parent.remove(component);

        Assert.assertTrue(component.isEnabled());
    }

    @Test
    public void explicitlyDisabledComponent_enableParent_componentRemainsDisabled() {
        TestComponent component = new TestComponent();
        component.setEnabled(false);

        TestComponent parent = new TestComponent();
        parent.add(component);

        parent.setEnabled(false);

        Assert.assertFalse(component.isEnabled());

        parent.setEnabled(true);

        Assert.assertFalse(component.isEnabled());
    }

    @Test
    public void setWidth() {
        TestComponent c = new TestComponent();
        c.setWidth("100px");
        Assert.assertEquals("100px", c.getWidth());
    }

    @Test
    public void setMinWidth() {
        TestComponent c = new TestComponent();
        c.setMinWidth("100px");
        Assert.assertEquals("100px", c.getMinWidth());
    }

    @Test
    public void setMaxWidth() {
        TestComponent c = new TestComponent();
        c.setMaxWidth("100px");
        Assert.assertEquals("100px", c.getMaxWidth());
    }

    @Test
    public void removeWidth() {
        TestComponent c = new TestComponent();
        c.setWidth("100px");
        Assert.assertEquals("100px", c.getWidth());

        c.setWidth(null);
        Assert.assertNull(c.getWidth());
    }

    @Test
    public void removeMinWidth() {
        TestComponent c = new TestComponent();
        c.setMinWidth("100px");
        Assert.assertEquals("100px", c.getMinWidth());

        c.setMinWidth(null);
        Assert.assertNull(c.getMinWidth());
    }

    @Test
    public void removeMaxWidth() {
        TestComponent c = new TestComponent();
        c.setMaxWidth("100px");
        Assert.assertEquals("100px", c.getMaxWidth());

        c.setMaxWidth(null);
        Assert.assertNull(c.getMaxWidth());
    }

    @Test
    public void setHeight() {
        TestComponent c = new TestComponent();
        c.setHeight("100px");
        Assert.assertEquals("100px", c.getHeight());
    }

    @Test
    public void setMinHeight() {
        TestComponent c = new TestComponent();
        c.setMinHeight("100px");
        Assert.assertEquals("100px", c.getMinHeight());
    }

    @Test
    public void setMaxHeight() {
        TestComponent c = new TestComponent();
        c.setMaxHeight("100px");
        Assert.assertEquals("100px", c.getMaxHeight());
    }

    @Test
    public void removeHeight() {
        TestComponent c = new TestComponent();
        c.setHeight("100px");
        Assert.assertEquals("100px", c.getHeight());

        c.setHeight(null);
        Assert.assertNull(c.getHeight());
    }

    @Test
    public void removeMinHeight() {
        TestComponent c = new TestComponent();
        c.setMinHeight("100px");
        Assert.assertEquals("100px", c.getMinHeight());

        c.setMinHeight(null);
        Assert.assertNull(c.getMinHeight());
    }

    @Test
    public void removeMaxHeight() {
        TestComponent c = new TestComponent();
        c.setMaxHeight("100px");
        Assert.assertEquals("100px", c.getMaxHeight());

        c.setMaxHeight(null);
        Assert.assertNull(c.getMaxHeight());
    }

    @Test
    public void setSizeFull() {
        TestComponent component = new TestComponent();
        component.setSizeFull();

        Assert.assertEquals("100%", component.getWidth());
        Assert.assertEquals("100%", component.getHeight());
    }

    @Test
    public void setWidthFull() {
        TestComponent component = new TestComponent();
        component.setWidthFull();

        Assert.assertEquals("100%", component.getWidth());
    }

    @Test
    public void setHeightFull() {
        TestComponent component = new TestComponent();
        component.setHeightFull();

        Assert.assertEquals("100%", component.getHeight());
    }

    @Test
    public void setSizeUndefined() {
        TestComponent component = new TestComponent();
        component.setWidth("10px");
        component.setHeight("5em");

        component.setSizeUndefined();

        Assert.assertNull(component.getWidth());
        Assert.assertNull(component.getHeight());
    }

    @Test
    public void getWidthUnit() {
        TestComponent component = new TestComponent();
        Assert.assertFalse(component.getWidthUnit().isPresent());

        component.setWidth("10px");
        Assert.assertTrue(component.getWidthUnit().isPresent());
        Assert.assertEquals(Unit.PIXELS, component.getWidthUnit().get());

        component.setSizeUndefined();
        Assert.assertFalse(component.getWidthUnit().isPresent());
    }

    @Test
    public void getHeightUnit() {
        TestComponent component = new TestComponent();
        Assert.assertFalse(component.getHeightUnit().isPresent());

        component.setHeight("10%");
        Assert.assertTrue(component.getHeightUnit().isPresent());
        Assert.assertEquals(Unit.PERCENTAGE, component.getHeightUnit().get());

        component.setSizeUndefined();
        Assert.assertFalse(component.getHeightUnit().isPresent());
    }

    @Test
    public void withoutLabelComponent_getLabelReturnsNull() {
        TestComponent component = new TestComponent();

        assertNull(component.getLabel());
    }

    @Test
    public void withNullLabel_getLabelReturnsNull() {
        TestComponent component = new TestComponent();
        component.setLabel(null);
        assertNull(component.getLabel());
    }

    @Test
    public void withEmptyLabel_getLabelReturnsEmptyString() {
        TestComponent component = new TestComponent();
        component.setLabel("");
        assertEquals("", component.getLabel());
    }

    @Test
    public void setLabel() {
        TestComponent component = new TestComponent();
        component.setLabel("test label");

        assertEquals("test label", component.getLabel());
    }

    @Test
    public void addClassName() {
        TestComponent component = new TestComponent();
        component.addClassName("foo");
        assertClasses(component, "foo");
        component.addClassName("bar");
        assertClasses(component, "foo", "bar");

        // use ClassList

        component.getClassNames().add("baz");
        assertClasses(component, "foo", "bar", "baz");
    }

    @Test
    public void setClassName_useClassList() {
        TestComponent component = new TestComponent();
        component.setClassName("foo bar");

        component.getClassNames().set("bar", false);
        assertClasses(component, "foo");
    }

    @Test
    public void removeClassName() {
        TestComponent component = new TestComponent();
        component.setClassName("foo Bar baz");
        component.removeClassName("foo");
        assertClasses(component, "Bar", "baz");
        component.removeClassName("bar");
        assertClasses(component, "Bar", "baz");
        component.removeClassName("Bar");
        assertClasses(component, "baz");
        component.removeClassName("baz");
        assertClasses(component);

        // use ClassList
        component.setClassName("foo");

        component.getClassNames().remove("foo");
        assertClasses(component);
    }

    @Test
    public void setClassName() {
        TestComponent component = new TestComponent();
        component.setClassName("foo");
        assertClasses(component, "foo");
        component.setClassName("bar");
        assertClasses(component, "bar");
        component.setClassName("bar foo");
        assertClasses(component, "bar", "foo");
        component.setClassName(" ");
        assertClasses(component);
        component.setClassName("");
        assertClasses(component);

        component.setClassName("removeMe");
        // setting null to classname should remove class name attribute
        component.setClassName(null);
        assertClasses(component);
    }

    @Test
    public void getClassName() {
        TestComponent component = new TestComponent();
        component.setClassName("foo");
        Assert.assertEquals("foo", component.getClassName());
        component.setClassName(" ");
        Assert.assertNull(component.getClassName());
    }

    @Test
    public void setClassNameToggle() {
        TestComponent component = new TestComponent();
        component.setClassName("foo", false);
        assertClasses(component);
        component.setClassName("foo", true);
        assertClasses(component, "foo");
        component.setClassName("foo", false);
        assertClasses(component);
        component.setClassName("foo", true);
        component.setClassName("bar", true);
        component.setClassName("baz", true);
        assertClasses(component, "foo", "bar", "baz");
        component.setClassName("baz", false);
        assertClasses(component, "foo", "bar");

    }

    @Test
    public void hasClassName() {
        TestComponent component = new TestComponent();
        Assert.assertFalse(component.hasClassName("foo"));
        component.setClassName("foo");
        Assert.assertTrue(component.hasClassName("foo"));
        Assert.assertFalse(component.hasClassName("fo"));
        component.setClassName("foo bar");
        Assert.assertTrue(component.hasClassName("foo"));
        Assert.assertTrue(component.hasClassName("bar"));

    }

    @Test
    public void getClassList_elementClassList() {
        TestComponent component = new TestComponent();

        Assert.assertEquals(component.getElement().getClassList(),
                component.getClassNames());
    }

    @Test
    public void testAddClassNames() {
        TestComponent component = new TestComponent();
        component.addClassNames();
        assertClasses(component);
        component.addClassNames("foo", "bar");
        assertClasses(component, "foo", "bar");

        component.removeClassNames("foo bar");
        assertClasses(component);

        component.addClassNames("foo bar");
        assertClasses(component, "foo", "bar");

        component.addClassNames("baz1", "baz2");
        assertClasses(component, "foo", "bar", "baz1", "baz2");
    }

    @Test
    public void testRemoveClassNames() {
        TestComponent component = new TestComponent();
        component.setClassName("foo bar baz1 baz2 foo2 bar1");

        component.removeClassNames();
        assertClasses(component, "foo", "bar", "baz1", "baz2", "foo2", "bar1");

        component.removeClassNames("baz2");
        assertClasses(component, "foo", "bar", "baz1", "foo2", "bar1");

        component.removeClassNames("bar", "foo2", "foo");
        assertClasses(component, "baz1", "bar1");
    }

    @Test
    public void addClassNames_extraSpacesBetweenAndAroundClassNames_validationPasses() {
        TestComponent component = new TestComponent();
        component.addClassNames("   foo  bar    baz");

        Assert.assertEquals(
                "Unexpected component's class names count after adding 3 class names",
                3, component.getClassNames().size());

        Assert.assertTrue(component.getClassNames().contains("foo"));
        Assert.assertTrue(component.getClassNames().contains("bar"));
        Assert.assertTrue(component.getClassNames().contains("baz"));
    }

    @Test
    public void removeClassNames_extraSpacesBetweenAndAroundClassNames_validationPasses() {
        TestComponent component = new TestComponent();
        component.addClassNames("foo", "bar", "baz");
        component.removeClassNames("   foo  bar    baz");

        Assert.assertEquals(
                "Unexpected component's class names count after removing all class names",
                0, component.getClassNames().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addClassNames_addEmptyClassName_throws() {
        TestComponent component = new TestComponent();
        component.addClassNames(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addClassNames_addNullClassName_throws() {
        TestComponent component = new TestComponent();
        component.addClassNames(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeClassNames_removeEmptyClassName_throws() {
        TestComponent component = new TestComponent();
        component.addClassNames(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeClassNames_removeNullClassName_throws() {
        TestComponent component = new TestComponent();
        component.addClassNames(null, null);
    }

    private void assertClasses(TestComponent c, String... expectedClasses) {
        Set<String> actual = c.getClassNames();
        Set<String> expected = new HashSet<>(Arrays.asList(expectedClasses));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void setValue_getValue() {
        TestComponent component = new TestComponent();
        component.setValue("TestValue");
        Assert.assertEquals(component.getValue(), "TestValue");
    }

    @Test
    public void clear() {
        TestComponent component = new TestComponent();
        component.setValue("TestValue");
        component.clear();
        Assert.assertEquals(component.getValue(),"");
    }

    @Test
    public void setReadOnly() {
        TestComponent component = new TestComponent();
        component.setReadOnly(true);
        Assert.assertEquals(component.getElement().getProperty("readonly"), "true");
    }

    @Test
    public void setRequiredIndicatorVisible() {
        TestComponent component = new TestComponent();
        component.setRequiredIndicatorVisible(true);
        Assert.assertEquals(component.getElement().getProperty("required"), "true");
    }

    @Test
    public void addValueChangeListener() {
        TestComponent component = new TestComponent();
        AtomicReference<String> eventReceived = new AtomicReference<>("");
        component.addValueChangeListener(event -> eventReceived.set("OK"));
        component.setValue("New Value");
        Assert.assertEquals(eventReceived.get(), "OK");
    }

    @Tag("test")
    private static class TestComponent extends AbstractSinglePropertyField<TestComponent, String>
            implements InputField<AbstractField.ComponentValueChangeEvent<TestComponent, String>, String>, HasComponents {

        private static final SerializableFunction<String, String> PARSER = valueFromClient -> {
            return valueFromClient;
        };

        private static final SerializableFunction<String, String> FORMATTER = valueFromModel -> {
            return valueFromModel;
        };

        public TestComponent() {
            super("value", "", String.class, PARSER, FORMATTER);

        }

    }
}
