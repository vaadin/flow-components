
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
