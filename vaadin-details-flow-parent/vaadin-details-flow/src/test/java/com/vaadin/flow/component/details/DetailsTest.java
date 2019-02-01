package com.vaadin.flow.component.details;

import com.vaadin.flow.component.html.Span;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DetailsTest {

    private Details details;

    @Before
    public void setUp() {
        details = new Details();
    }

    @Test
    public void initContent() {
        details.setContent(new Span());
        details.addContent(new Span());
        Assert.assertEquals(2, details.getContent().count());

        details.setContent(new Span());
        Assert.assertEquals(1, details.getContent().count());
    }
}
