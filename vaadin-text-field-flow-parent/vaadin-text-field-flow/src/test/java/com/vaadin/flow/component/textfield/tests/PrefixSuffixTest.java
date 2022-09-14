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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for setting prefix and suffix components for {@link TextField}.
 */
public class PrefixSuffixTest {

    @Test
    public void setPrefix_replacesPrefix() {
        TextField field = new TextField();
        Assert.assertNull("There should be no prefix component by default",
                field.getPrefixComponent());

        setAndAssertPrefix(field, new Span());
        setAndAssertPrefix(field, new H1());
    }

    @Test
    public void setPrefix_setPrefixNull_prefixRemoved() {
        TextField field = new TextField();
        field.setPrefixComponent(new Span());
        field.setPrefixComponent(null);

        Assert.assertNull(field.getPrefixComponent());
        Assert.assertEquals(
                "Setting prefix component to null should remove all children in the prefix-slot",
                0, getNumOfChildrenInSlot(field, "prefix"));
    }

    @Test
    public void setSuffix_replacesSuffix() {
        TextField field = new TextField();
        Assert.assertNull("There should be no suffix component by default",
                field.getSuffixComponent());

        setAndAssertSuffix(field, new Span());
        setAndAssertSuffix(field, new H1());
    }

    @Test
    public void setSuffix_setSuffixNull_suffixRemoved() {
        TextField field = new TextField();
        field.setSuffixComponent(new Span());
        field.setSuffixComponent(null);

        Assert.assertNull(field.getSuffixComponent());
        Assert.assertEquals(
                "Setting suffix component to null should remove all children in the suffix-slot",
                0, getNumOfChildrenInSlot(field, "suffix"));
    }

    private void setAndAssertPrefix(TextField field, Component prefix) {
        field.setPrefixComponent(prefix);
        Assert.assertEquals(
                "Setting a prefix component should remove existing prefix components",
                1, getNumOfChildrenInSlot(field, "prefix"));
        Assert.assertEquals("getPrefixComponent did not return set value",
                prefix, field.getPrefixComponent());
    }

    private void setAndAssertSuffix(TextField field, Component suffix) {
        field.setSuffixComponent(suffix);
        Assert.assertEquals(
                "Setting a suffix component should remove existing suffix components",
                1, getNumOfChildrenInSlot(field, "suffix"));
        Assert.assertEquals("getSuffixComponent did not return set value",
                suffix, field.getSuffixComponent());
    }

    private int getNumOfChildrenInSlot(Component component, String slot) {
        return (int) component.getElement().getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .count();
    }
}
