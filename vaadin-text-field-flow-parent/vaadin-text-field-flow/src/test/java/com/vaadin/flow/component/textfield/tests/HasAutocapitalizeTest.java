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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.HasAutocapitalize;
import org.junit.Assert;
import org.junit.Test;

public class HasAutocapitalizeTest {

    @Tag("div")
    public static class HasAutocapitalizeComponent extends Component
            implements HasAutocapitalize {

    }

    @Test
    public void defaultValue() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        Autocapitalize autocapitalize = c.getAutocapitalize();
        Assert.assertNull(autocapitalize);
    }

    @Test
    public void emptyValue() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.getElement().setAttribute("autocapitalize", "");
        Autocapitalize autocapitalize = c.getAutocapitalize();
        Assert.assertEquals(Autocapitalize.SENTENCES, autocapitalize);
    }

    @Test
    public void noCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.NONE);
        Assert.assertEquals(Autocapitalize.NONE, c.getAutocapitalize());
    }

    @Test
    public void sentencesCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.SENTENCES);
        Assert.assertEquals(Autocapitalize.SENTENCES, c.getAutocapitalize());
    }

    @Test
    public void wordsCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.WORDS);
        Assert.assertEquals(Autocapitalize.WORDS, c.getAutocapitalize());
    }

    @Test
    public void charsCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.CHARACTERS);
        Assert.assertEquals(Autocapitalize.CHARACTERS, c.getAutocapitalize());
    }
}
