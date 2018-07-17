package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppLayoutTest {

    private AppLayout sut;

    @Before
    public void setUp() {
        sut = new AppLayout();
    }

    @Test
    public void onAttach_noMenuItem() {
        sut.onAttach(new AttachEvent(sut, true));

        long menuCount = sut.getChildren()
                .map(Component::getElement)
                .filter(e -> e.getAttribute("slot").equals("menu"))
                .count();

        Assert.assertEquals(1, menuCount);
        Assert.assertNull(sut.getSelectedMenuItem());
    }

    @Test
    public void onAttach_withMenuItems() {
        sut.addMenuItem(new ActionMenuItem("Go offline"));
        sut.addMenuItem(new RoutingMenuItem("Logout", "Logout"));
        sut.onAttach(new AttachEvent(sut, true));

        // A menu item is selected by default.
        Assert.assertNotNull(sut.getSelectedMenuItem());
    }

    @Test
    public void setBranding() {
        Element branding = new H2("Vaadin").getElement();
        sut.setBranding(branding);

        // Verify that branding goes to the branding slot.
        long brandingCount = sut.getElement().getChildren()
                .filter(e -> e.getAttribute("slot").equals("branding"))
                .count();
        Assert.assertEquals(1, brandingCount);
    }

    @Test
    public void removeBranding() {
        Element branding = new H2("Vaadin").getElement();
        sut.setBranding(branding);

        sut.removeBranding();

        Assert.assertTrue(sut.getElement().getChildren()
                .noneMatch(e -> e.getAttribute("slot").equals("branding")));
    }

    @Test
    public void setMenuItems() {
        Assert.assertEquals(0, getMenu().getChildCount());

        sut.addMenuItem(new RoutingMenuItem("Home", ""));

        MenuItem[] newMenuItems = Stream
                .generate(() -> new RoutingMenuItem("Route", "route"))
                .limit(3)
                .toArray(MenuItem[]::new);

        sut.setMenuItems(newMenuItems);

        Assert.assertEquals(newMenuItems.length, getMenu().getChildCount());
    }

    @Test
    public void addMenuItem() {
        Assert.assertEquals(0, getMenu().getChildCount());

        sut.addMenuItem(new RoutingMenuItem("Home", ""));
        Assert.assertEquals(1, getMenu().getChildCount());
    }

    private Element getMenu() {
        return sut.getChildren()
                .map(Component::getElement)
                .filter(e -> e.getAttribute("slot").equals("menu"))
                .findFirst()
                .get();
    }

    @Test
    public void removeMenuItem() {
        RoutingMenuItem home = new RoutingMenuItem("Home", "");
        sut.addMenuItem(home);

        sut.removeMenuItem(home);
        Assert.assertEquals(0, getMenu().getChildCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeMenuItem_invalidItem() {
        RoutingMenuItem home = new RoutingMenuItem("Home", "");
        sut.addMenuItem(home);

        sut.removeMenuItem(new RoutingMenuItem("Profile", "profile"));
    }

    @Test
    public void getMenuItemTargetingRoute() {
        RoutingMenuItem home = new RoutingMenuItem("Home", "");
        sut.addMenuItem(home);

        RoutingMenuItem profile = new RoutingMenuItem("Profile", "profile");
        sut.addMenuItem(profile);

        RoutingMenuItem settings = new RoutingMenuItem("Settings", "settings");
        sut.addMenuItem(settings);

        Assert.assertEquals(profile, sut.getMenuItemTargetingRoute("profile").get());
    }

    @Test
    public void getMenuItemTargetingRoute_none() {
        RoutingMenuItem home = new RoutingMenuItem("Home", "");
        sut.addMenuItem(home);

        RoutingMenuItem profile = new RoutingMenuItem("Profile", "profile");
        sut.addMenuItem(profile);

        Assert.assertFalse(sut.getMenuItemTargetingRoute("dashboard").isPresent());
    }

    @Test
    public void getMenuItemTargetingRoute_duplicate() {
        RoutingMenuItem profile = new RoutingMenuItem("Profile", "profile");
        sut.addMenuItem(profile);

        RoutingMenuItem settings = new RoutingMenuItem("Settings", "profile");
        sut.addMenuItem(settings);

        Assert.assertEquals(profile, sut.getMenuItemTargetingRoute("profile").get());
    }

    @Test
    public void selectMenuItem() {
        RoutingMenuItem home = new RoutingMenuItem("Home", "");
        sut.addMenuItem(home);

        RoutingMenuItem profile = new RoutingMenuItem("Profile", "profile");
        sut.addMenuItem(profile);

        ActionMenuItem logout = new ActionMenuItem("Logout");
        sut.addMenuItem(logout);

        sut.onAttach(new AttachEvent(sut, false));

        Assert.assertFalse(sut.getMenu().getElement().hasProperty("selected"));

        sut.selectMenuItem(profile);

        // Behavior of an ActionMenuItem selection cannot be unit-tested
        // because Tabs for Flow doesn't trigger the selection server-side.
        Assert.assertEquals("1", sut.getMenu().getElement().getProperty("selected"));
    }

    @Test
    public void setContent() {
        Element content = new Div().getElement();
        sut.setContent(content);

        Assert.assertEquals("content", content.getAttribute("role"));

        List<Element> children = sut.getElement().getChildren().collect(Collectors.toList());
        Assert.assertTrue(children.contains(content));
    }

    @Test
    public void removeContent() {
        sut.removeContent(); // No NPE.

        Element content = new Div().getElement();
        sut.setContent(content);

        sut.removeContent();

        List<Element> children = sut.getElement().getChildren().collect(Collectors.toList());
        Assert.assertFalse(children.contains(content));
    }
}