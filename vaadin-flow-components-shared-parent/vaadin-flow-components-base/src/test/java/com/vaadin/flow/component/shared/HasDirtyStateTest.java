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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasDirtyStateTest {
    @Tag("test-component")
    private static class TestComponent extends Component implements HasDirtyState {}

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void notDirtyByDefault() {
        Assert.assertFalse(component.isDirty());
        Assert.assertFalse(component.getElement().getProperty("dirty", false));
    }

    @Test
    public void setDirty_isDirty() {
        component.setDirty(true);
        Assert.assertTrue(component.isDirty());
        Assert.assertTrue(component.getElement().getProperty("dirty", false));

        component.setDirty(false);
        Assert.assertFalse(component.isDirty());
        Assert.assertFalse(component.getElement().getProperty("dirty", false));
    }
}
