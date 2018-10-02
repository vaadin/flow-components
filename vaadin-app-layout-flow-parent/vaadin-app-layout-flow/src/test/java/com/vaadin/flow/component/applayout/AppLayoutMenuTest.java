package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.tabs.Tabs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppLayoutMenuTest {
    private AppLayoutMenu systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new AppLayoutMenu();
    }

    @Test
    public void construction_elementIsInMenuSlot() {
        Assert.assertEquals("menu",
            systemUnderTest.getElement().getAttribute("slot"));
    }

    @Test
    public void onAttach_withMenuItems() {
        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Logout", "Logout"),
            new AppLayoutMenuItem("Go offline"));
        final Tabs tabs = (Tabs) systemUnderTest.getElement().getComponent()
            .get();
        ComponentUtil.fireEvent(tabs, new AttachEvent(tabs, true));

        // No menu item is selected by default.
        Assert.assertNull(systemUnderTest.getSelectedMenuItem());
    }

    @Test
    public void onAttach_withMenuItems_explicit_selection() {
        final AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "Home");
        systemUnderTest.addMenuItems(home, new AppLayoutMenuItem("Go offline"));
        systemUnderTest.selectMenuItem(home);
        final Tabs tabs = (Tabs) systemUnderTest.getElement().getComponent()
            .get();
        ComponentUtil.fireEvent(tabs, new AttachEvent(tabs, true));

        // No menu item is selected by default.
        Assert.assertEquals(home, systemUnderTest.getSelectedMenuItem());
    }

}
