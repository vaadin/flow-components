
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
