package com.vaadin.flow.component.details;

import com.vaadin.flow.component.AttachEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DetailsTest {

    private Details systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Details();
    }

    @Test
    public void onAttach_init() {
        systemUnderTest.addAttachListener(e -> new AttachEvent(systemUnderTest, true));

        Assert.assertTrue(true);
    }
}
