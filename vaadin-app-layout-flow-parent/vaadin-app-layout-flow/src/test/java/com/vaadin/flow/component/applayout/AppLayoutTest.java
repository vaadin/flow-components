package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

        systemUnderTest.addMenuItem(new AppLayoutMenuItem("Home", ""));

        AppLayoutMenuItem[] newMenuItems = Stream
            .generate(() -> new AppLayoutMenuItem("Route", "route")).limit(3)
            .toArray(AppLayoutMenuItem[]::new);

        systemUnderTest.setMenuItems(newMenuItems);

        Assert.assertEquals(newMenuItems.length, getMenu().getChildCount());
    }

    @Test
    public void addMenuItem() {
        Assert.assertEquals(0, getMenu().getChildCount());

        systemUnderTest.addMenuItem(new AppLayoutMenuItem("Home", ""));
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
        systemUnderTest.addMenuItem(home);

        systemUnderTest.removeMenuItem(home);
        Assert.assertEquals(0, getMenu().getChildCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeMenuItem_invalidItem() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItem(home);

        Tabs otherTabs = new Tabs();
        AppLayoutMenuItem otherMenuItem = new AppLayoutMenuItem("Profile",
            "profile");
        otherTabs.add(otherMenuItem);

        systemUnderTest.removeMenuItem(otherMenuItem);
    }

    @Test
    public void getMenuItemTargetingRoute() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItem(home);

        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        systemUnderTest.addMenuItem(profile);

        AppLayoutMenuItem settings = new AppLayoutMenuItem("Settings",
            "settings");
        systemUnderTest.addMenuItem(settings);

        Assert.assertEquals(profile,
            systemUnderTest.getMenuItemTargetingRoute("profile").get());
    }

    @Test
    public void getMenuItemTargetingRoute_none() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItem(home);

        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        systemUnderTest.addMenuItem(profile);

        Assert.assertFalse(
            systemUnderTest.getMenuItemTargetingRoute("dashboard").isPresent());
    }

    @Test
    public void getMenuItemTargetingRoute_duplicate() {
        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        systemUnderTest.addMenuItem(profile);

        AppLayoutMenuItem settings = new AppLayoutMenuItem("Settings",
            "profile");
        systemUnderTest.addMenuItem(settings);

        Assert.assertEquals(profile,
            systemUnderTest.getMenuItemTargetingRoute("profile").get());
    }

    @Test
    public void selectMenuItem() {
        AppLayoutMenuItem home = new AppLayoutMenuItem("Home", "");
        systemUnderTest.addMenuItem(home);

        AppLayoutMenuItem profile = new AppLayoutMenuItem("Profile", "profile");
        systemUnderTest.addMenuItem(profile);

        AppLayoutMenuItem logout = new AppLayoutMenuItem("Logout");
        systemUnderTest.addMenuItem(logout);

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
    public void setMenuItems_after_calling_addMenuItem() {
        systemUnderTest.addMenuItem(new AppLayoutMenuItem("Action1"));
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

}
