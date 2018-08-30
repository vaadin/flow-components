package com.vaadin.flow.component.crud;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CrudTest {

    private Crud systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Crud(CrudTest.class, new CrudEditor<CrudTest>() {
            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public boolean isDirty() {
                return false;
            }
        });
    }

    @Test
    public void onAttach_init() {
        Assert.assertTrue(true);
    }
}