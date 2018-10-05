package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.stream.Stream;

public class AppLayoutMenuTest {
    private AppLayoutMenu systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new AppLayoutMenu();
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


    @Test
    public void setMenuItems() {
        Assert.assertEquals(0, systemUnderTest.getElement().getChildCount());

        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Home", ""));

        AppLayoutMenuItem[] newMenuItems = Stream
            .generate(() -> new AppLayoutMenuItem("Route", "route")).limit(3)
            .toArray(AppLayoutMenuItem[]::new);

        systemUnderTest.setMenuItems(newMenuItems);

        Assert.assertEquals(newMenuItems.length, systemUnderTest.getElement().getChildCount());
    }

    @Test
    public void addMenuItems() {
        Assert.assertEquals(0, systemUnderTest.getElement().getChildCount());

        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Home", ""));
        Assert.assertEquals(1, systemUnderTest.getElement().getChildCount());
    }

    @Test
    public void removeMenuItem() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItems(home);
        Assert.assertEquals(1, systemUnderTest.getElement().getChildCount());

        systemUnderTest.removeMenuItem(home);
        Assert.assertEquals(0, systemUnderTest.getElement().getChildCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeMenuItem_invalidItem() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItems(home);

        Tabs otherTabs = new Tabs();
        AppLayoutMenuItem otherMenuItem = new AppLayoutMenuItem("Profile",
            "profile");
        otherTabs.add(otherMenuItem);

        systemUnderTest.removeMenuItem(otherMenuItem);
    }

    @Test
    public void getMenuItemTargetingRoute() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        AppLayoutMenuItem settings = new AppLayoutMenuItem("Settings",
            "settings");
        systemUnderTest.addMenuItems(home, profile, settings);

        Assert.assertEquals(profile,
            systemUnderTest.getMenuItemTargetingRoute("profile").get());
    }

    @Test
    public void getMenuItemTargetingRoute_none() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        systemUnderTest.addMenuItems(home, profile);

        Assert.assertFalse(
            systemUnderTest.getMenuItemTargetingRoute("dashboard").isPresent());
    }

    @Test
    public void getMenuItemTargetingRoute_duplicate() {
        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        AppLayoutMenuItem settings = new AppLayoutMenuItem("Settings",
            "profile");
        systemUnderTest.addMenuItems(profile, settings);

        Assert.assertEquals(profile,
            systemUnderTest.getMenuItemTargetingRoute("profile").get());
    }

    @Test
    public void selectMenuItem() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        AppLayoutMenuItem logout = new AppLayoutMenuItem("Logout");
        systemUnderTest.addMenuItems(home, profile, logout);

        final Tabs tabs = (Tabs) systemUnderTest.getElement()
            .getComponent().get();
        ComponentUtil.fireEvent(tabs, new AttachEvent(tabs, true));
        Assert.assertNull(tabs.getSelectedTab());

        systemUnderTest.selectMenuItem(profile);

        Assert.assertEquals(profile, tabs.getSelectedTab());
    }

    @Test
    public void setMenuItems_after_calling_addMenuItems() {
        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Action1"));
        systemUnderTest.setMenuItems(new AppLayoutMenuItem("Action2"),
            new AppLayoutMenuItem("Action3"));
        Assert.assertArrayEquals(new Object[] { "Action2", "Action3" },
            systemUnderTest.getElement().getChildren()
                .map(e -> (AppLayoutMenuItem) e.getComponent().get())
                .map(AppLayoutMenuItem::getTitle).toArray());
    }

    @Test
    public void clearMenuItems() {
        Assert.assertEquals(0,
            systemUnderTest.getElement().getChildCount());
        //No exception on clearing already empty.
        systemUnderTest.clearMenuItems();
        systemUnderTest.setMenuItems(new AppLayoutMenuItem("Action1"),
            new AppLayoutMenuItem("Action2"));
        Assert.assertEquals(2,
            systemUnderTest.getElement().getChildCount());

        systemUnderTest.clearMenuItems();
        Assert.assertEquals(0,
            systemUnderTest.getElement().getChildCount());

    }

    @Test
    public void addMenuItem_title() {
        final String title = "Title";
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest
            .addMenuItem(title);
        Assert.assertEquals(title, appLayoutMenuItem.getTitle());
    }

    @Test
    public void addMenuItem_icon() {
        final Component icon = new Div();
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest.addMenuItem(icon);
        Assert.assertEquals(icon, appLayoutMenuItem.getIcon());
    }

    @Test
    public void addMenuItem_icon_and_title() {
        final Component icon = new Div();
        final String title = "Title";
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest
            .addMenuItem(icon, title);
        Assert.assertEquals(icon, appLayoutMenuItem.getIcon());
        Assert.assertEquals(title, appLayoutMenuItem.getTitle());
    }

    @Test
    public void addMenuItem_icon_title_and_route() {
        final Component icon = new Div();
        final String title = "Title";
        final String route = "route";
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest
            .addMenuItem(icon, title, route);
        Assert.assertEquals(icon, appLayoutMenuItem.getIcon());
        Assert.assertEquals(title, appLayoutMenuItem.getTitle());
        Assert.assertEquals(route, appLayoutMenuItem.getRoute());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addMenuItem_icon_listener() {
        final Component icon = new Div();
        final ComponentEventListener<MenuItemClickEvent> listener = (ComponentEventListener<MenuItemClickEvent>) Mockito
            .mock(ComponentEventListener.class);
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest
            .addMenuItem(icon, listener);
        Assert.assertEquals(icon, appLayoutMenuItem.getIcon());
        appLayoutMenuItem.fireMenuItemClickEvent();
        Mockito.verify(listener).onComponentEvent(Mockito.any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addMenuItem_title_listener() {
        final String title = "Title";
        final ComponentEventListener<MenuItemClickEvent> listener = (ComponentEventListener<MenuItemClickEvent>) Mockito
            .mock(ComponentEventListener.class);
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest
            .addMenuItem(title, listener);
        Assert.assertEquals(title, appLayoutMenuItem.getTitle());
        appLayoutMenuItem.fireMenuItemClickEvent();
        Mockito.verify(listener).onComponentEvent(Mockito.any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addMenuItem_icon_title_and_listener() {
        final Component icon = new Div();
        final String title = "Title";
        final ComponentEventListener<MenuItemClickEvent> listener = (ComponentEventListener<MenuItemClickEvent>) Mockito
            .mock(ComponentEventListener.class);
        AppLayoutMenuItem appLayoutMenuItem = systemUnderTest
            .addMenuItem(icon, title, listener);
        Assert.assertEquals(icon, appLayoutMenuItem.getIcon());
        Assert.assertEquals(title, appLayoutMenuItem.getTitle());
        appLayoutMenuItem.fireMenuItemClickEvent();
        Mockito.verify(listener).onComponentEvent(Mockito.any());
    }

}
