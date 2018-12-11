package com.vaadin.flow.component.progrid;

import com.vaadin.flow.component.AttachEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProGridTest {

    private ProGrid systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new ProGrid();
    }

    @Test
    public void onAttach_init() {
        systemUnderTest.addAttachListener(e -> new AttachEvent(systemUnderTest, true));

        Assert.assertTrue(true);
    }
}
