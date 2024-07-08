/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import org.junit.Test;

import com.vaadin.flow.component.menubar.demo.MenuBarView;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.tests.ComponentDemoTest;

/**
 * Integration tests for the {@link MenuBarView}.
 */
public class MenuBarDemoIT extends ComponentDemoTest {

    @Test
    public void testMenuBarDemo() {
        MenuBarElement menuBar = $(MenuBarElement.class).first();
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-menu-bar");
    }
}
