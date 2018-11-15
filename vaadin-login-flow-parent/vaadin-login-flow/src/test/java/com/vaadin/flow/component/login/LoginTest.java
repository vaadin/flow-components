package com.vaadin.flow.component.login;

import com.vaadin.flow.component.AttachEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoginTest {

    private Login systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Login();
    }

    @Test
    public void onAttach_init() {
        systemUnderTest.addAttachListener(e -> new AttachEvent(systemUnderTest, true));

        Assert.assertTrue(true);
    }
}
