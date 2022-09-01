/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for setting prefix and suffix components for {@link HasPrefixAndSuffix}.
 */
public class PrefixSuffixTest {

    @Test
    public void setPrefix_replacesPrefix() {
        TestComponent component = new TestComponent();
        Assert.assertNull("There should be no prefix component by default",
                component.getPrefixComponent());

        setAndAssertPrefix(component, new Span());
        setAndAssertPrefix(component, new H1());
    }

    @Test
    public void setPrefix_setPrefixNull_prefixRemoved() {
        TestComponent component = new TestComponent();
        component.setPrefixComponent(new Span());
        component.setPrefixComponent(null);

        Assert.assertNull(component.getPrefixComponent());
        Assert.assertEquals(
                "Setting prefix component to null should remove all children in the prefix-slot",
                0, getNumOfChildrenInSlot(component, "prefix"));
    }

    @Test
    public void setSuffix_replacesSuffix() {
        TestComponent component = new TestComponent();
        Assert.assertNull("There should be no suffix component by default",
                component.getSuffixComponent());

        setAndAssertSuffix(component, new Span());
        setAndAssertSuffix(component, new H1());
    }

    @Test
    public void setSuffix_setSuffixNull_suffixRemoved() {
        TestComponent component = new TestComponent();
        component.setSuffixComponent(new Span());
        component.setSuffixComponent(null);

        Assert.assertNull(component.getSuffixComponent());
        Assert.assertEquals(
                "Setting suffix component to null should remove all children in the suffix-slot",
                0, getNumOfChildrenInSlot(component, "suffix"));
    }

    private void setAndAssertPrefix(TestComponent component, Component prefix) {
        component.setPrefixComponent(prefix);
        Assert.assertEquals(
                "Setting a prefix component should remove existing prefix components",
                1, getNumOfChildrenInSlot(component, "prefix"));
        Assert.assertEquals("getPrefixComponent did not return set value",
                prefix, component.getPrefixComponent());
    }

    private void setAndAssertSuffix(TestComponent component, Component suffix) {
        component.setSuffixComponent(suffix);
        Assert.assertEquals(
                "Setting a suffix component should remove existing suffix components",
                1, getNumOfChildrenInSlot(component, "suffix"));
        Assert.assertEquals("getSuffixComponent did not return set value",
                suffix, component.getSuffixComponent());
    }

    private int getNumOfChildrenInSlot(Component component, String slot) {
        return (int) component.getElement().getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .count();
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasPrefixAndSuffix {
    }
}
