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
package com.vaadin.flow.component.icon.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasTooltip;

public class IconTest {

    @Test
    public void implementsHasTooltip() {
        Icon icon = new Icon();
        Assert.assertTrue(icon instanceof HasTooltip);
    }

    @Test
    public void emptyIconIsEmpty() {
        Assert.assertNull(new Icon().getIcon());
    }

    @Test
    public void usesVaadinCollectionByDefault() {
        Assert.assertEquals("vaadin:foo", new Icon("foo").getIcon());
    }

    @Test
    public void canDefineCollectionInConstructor() {
        Assert.assertEquals("bar:foo", new Icon("bar:foo").getIcon());
    }

    @Test
    public void canDefineCollectionInSetter() {
        Icon icon = new Icon();
        icon.setIcon("bar:foo");
        Assert.assertEquals("bar:foo", icon.getIcon());
    }

    @Test
    public void setterUsesCurrentCollection() {
        Icon icon = new Icon("bar:foo");
        icon.setIcon("baz");
        Assert.assertEquals("bar:baz", icon.getIcon());
    }

    @Test
    public void canSetNewVaadinIcon() {
        Icon icon = new Icon("bar:foo");
        icon.setIcon(VaadinIcon.ABSOLUTE_POSITION);
        Assert.assertEquals("vaadin:absolute-position", icon.getIcon());
    }
}
