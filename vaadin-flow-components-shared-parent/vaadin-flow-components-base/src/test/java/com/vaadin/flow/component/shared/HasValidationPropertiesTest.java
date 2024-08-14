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
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasValidationPropertiesTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialErrorMessage() {
        Assert.assertEquals(component.getErrorMessage(), null);
    }

    @Test
    public void changeErrorMessage() {
        component.setErrorMessage("This field is required");
        Assert.assertEquals(component.getElement().getProperty("errorMessage"),
                "This field is required");

        component.setErrorMessage(null);
        Assert.assertEquals(component.getElement().getProperty("errorMessage"),
                "");
    }

    @Test
    public void initialInvalid() {
        Assert.assertFalse(component.isInvalid());
    }

    @Test
    public void changeInvalid() {
        component.setInvalid(true);
        Assert.assertTrue(component.isInvalid());
        Assert.assertTrue(component.getElement().getProperty("invalid", false));

        component.setInvalid(false);
        Assert.assertFalse(component.isInvalid());
        Assert.assertFalse(
                component.getElement().getProperty("invalid", false));
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasValidationProperties {
    }
}
