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
package com.vaadin.flow.component.icon.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasTooltip;

class IconTest {

    @Test
    void implementsHasTooltip() {
        Icon icon = new Icon();
        Assertions.assertTrue(icon instanceof HasTooltip);
    }

    @Test
    void emptyIconIsEmpty() {
        Assertions.assertNull(new Icon().getIcon());
    }

    @Test
    void usesVaadinCollectionByDefault() {
        Assertions.assertEquals("vaadin:foo", new Icon("foo").getIcon());
    }

    @Test
    void canDefineCollectionInConstructor() {
        Assertions.assertEquals("bar:foo", new Icon("bar:foo").getIcon());
    }

    @Test
    void canDefineCollectionInSetter() {
        Icon icon = new Icon();
        icon.setIcon("bar:foo");
        Assertions.assertEquals("bar:foo", icon.getIcon());
    }

    @Test
    void setterUsesCurrentCollection() {
        Icon icon = new Icon("bar:foo");
        icon.setIcon("baz");
        Assertions.assertEquals("bar:baz", icon.getIcon());
    }

    @Test
    void canSetNewVaadinIcon() {
        Icon icon = new Icon("bar:foo");
        icon.setIcon(VaadinIcon.ABSOLUTE_POSITION);
        Assertions.assertEquals("vaadin:absolute-position", icon.getIcon());
    }
}
