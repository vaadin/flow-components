package com.vaadin.flow.component.crud;

import com.vaadin.flow.component.AttachEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CrudTest {

    private Crud systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Crud();
    }

    @Test
    public void onAttach_init() {
        systemUnderTest.onAttach(new AttachEvent(systemUnderTest, true));

        Assert.assertTrue(true);
    }
}