package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppLayoutTest {

    private AppLayout systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new AppLayout();
    }

    @Test
    public void setBranding_Element() {
        Element branding = new H2("Vaadin").getElement();
        systemUnderTest.setBranding(branding);

        // Verify that branding goes to the branding slot.
        long brandingCount = systemUnderTest.getElement().getChildren()
            .filter(e -> e.getAttribute("slot").equals("branding")).count();
        Assert.assertEquals(1, brandingCount);
    }

    @Test
    public void setBranding_Component() {
        Component branding = new Div();
        Assert.assertNull(branding.getElement().getAttribute("slot"));
        systemUnderTest.setBranding(branding);
        Assert.assertEquals("branding",
            branding.getElement().getAttribute("slot"));
        Assert.assertTrue(
            systemUnderTest.getChildren().anyMatch(branding::equals));
    }

    @Test
    public void removeBranding() {
        Element branding = new H2("Vaadin").getElement();
        systemUnderTest.setBranding(branding);

        systemUnderTest.removeBranding();

        Assert.assertTrue(systemUnderTest.getElement().getChildren()
            .noneMatch(e -> e.getAttribute("slot").equals("branding")));
    }

    @Test
    public void setMenuItems() {
        Assert.assertEquals(0, getMenu().getChildCount());

        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Home", ""));

        AppLayoutMenuItem[] newMenuItems = Stream
            .generate(() -> new AppLayoutMenuItem("Route", "route")).limit(3)
            .toArray(AppLayoutMenuItem[]::new);

        systemUnderTest.setMenuItems(newMenuItems);

        Assert.assertEquals(newMenuItems.length, getMenu().getChildCount());
    }

    @Test
    public void addMenuItems() {
        Assert.assertEquals(0, getMenu().getChildCount());

        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Home", ""));
        Assert.assertEquals(1, getMenu().getChildCount());
    }

    private Element getMenu() {
        return systemUnderTest.getChildren().map(Component::getElement)
            .filter(e -> e.getAttribute("slot").equals("menu")).findFirst()
            .get();
    }

    @Test
    public void removeMenuItem() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItems(home);
        Assert.assertEquals(1, getMenu().getChildCount());

        systemUnderTest.removeMenuItem(home);
        Assert.assertEquals(0, getMenu().getChildCount());
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

        final Tabs tabs = (Tabs) systemUnderTest.getMenu().getElement()
            .getComponent().get();
        ComponentUtil.fireEvent(tabs, new AttachEvent(tabs, true));
        Assert.assertNull(tabs.getSelectedTab());

        systemUnderTest.selectMenuItem(profile);

        Assert.assertEquals(profile, tabs.getSelectedTab());
    }

    @Test
    public void setContent() {
        Element content = new Div().getElement();
        systemUnderTest.setContent(content);

        List<Element> children = systemUnderTest.getElement().getChildren()
            .collect(Collectors.toList());
        Assert.assertTrue(children.contains(content));
    }

    @Test
    public void removeContent() {
        systemUnderTest.removeContent(); // No NPE.

        Element content = new Div().getElement();
        systemUnderTest.setContent(content);

        systemUnderTest.removeContent();

        List<Element> children = systemUnderTest.getElement().getChildren()
            .collect(Collectors.toList());
        Assert.assertFalse(children.contains(content));
        Assert.assertNull(systemUnderTest.getContent());
    }

    @Test
    public void setMenuItems_after_calling_addMenuItems() {
        systemUnderTest.addMenuItems(new AppLayoutMenuItem("Action1"));
        systemUnderTest.setMenuItems(new AppLayoutMenuItem("Action2"),
            new AppLayoutMenuItem("Action3"));
        Assert.assertArrayEquals(new Object[] { "Action2", "Action3" },
            systemUnderTest.getMenu().getElement().getChildren()
                .map(e -> (AppLayoutMenuItem) e.getComponent().get())
                .map(AppLayoutMenuItem::getTitle).toArray());
    }

    @Test
    public void clearMenuItems() {
        Assert.assertEquals(0,
            systemUnderTest.getMenu().getElement().getChildCount());
        //No exception on clearing already empty.
        systemUnderTest.clearMenuItems();
        systemUnderTest.setMenuItems(new AppLayoutMenuItem("Action1"),
            new AppLayoutMenuItem("Action2"));
        Assert.assertEquals(2,
            systemUnderTest.getMenu().getElement().getChildCount());

        systemUnderTest.clearMenuItems();
        Assert.assertEquals(0,
            systemUnderTest.getMenu().getElement().getChildCount());

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
