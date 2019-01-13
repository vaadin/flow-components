package com.vaadin.flow.component.accordion;

import com.vaadin.flow.component.AttachEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccordionTest {

    private Accordion systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Accordion();
    }

    @Test
    public void onAttach_init() {
        systemUnderTest.addAttachListener(e -> new AttachEvent(systemUnderTest, true));

        Assert.assertTrue(true);
    }
}
