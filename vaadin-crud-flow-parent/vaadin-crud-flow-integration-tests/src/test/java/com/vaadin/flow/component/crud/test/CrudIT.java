package com.vaadin.flow.component.crud.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CrudIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void pass() {
        Assert.assertEquals("Hello Crud!", $("h1").first().getText());
    }
}
