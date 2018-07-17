package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuItemTest {

    private MenuItem sut;

    @Before
    public void setUp() {
        sut = new RoutingMenuItem("Home", "");
    }

    @Test
    public void setIcon() {
        Icon icon = new Icon();
        sut.setIcon(icon);
        Assert.assertEquals("icon", icon.getElement().getAttribute("role"));

        List<Component> children = sut.getChildren().collect(Collectors.toList());

        Assert.assertEquals(2, children.size());
        Assert.assertTrue(children.get(0) instanceof Icon);
        Assert.assertEquals("Home", ((Span) children.get(1)).getText());
    }

    @Test
    public void setIcon_null() {
        sut.setIcon(new Icon());
        sut.setIcon(null);

        List<Component> children = sut.getChildren().collect(Collectors.toList());

        Assert.assertEquals(1, children.size());
        Assert.assertEquals("Home", ((Span) children.get(0)).getText());
    }

    @Test
    public void setTitle() {
        sut.setTitle("Logout");

        Assert.assertEquals("Logout", sut.getElement().getAttribute("title"));

        List<Component> children = sut.getChildren().collect(Collectors.toList());

        Assert.assertEquals(1, children.size());
        Assert.assertEquals("Logout", ((Span) children.get(0)).getText());
    }

    @Test(expected = NullPointerException.class)
    public void setTitle_null() {
        sut.setTitle(null);
    }

    @Test
    public void setListener() {
        ComponentEventListener<MenuItemClickEvent> defaultListener = sut.getListener();
        List<String> actionResults = new ArrayList<>();
        sut.setListener(e -> actionResults.add("Executed"));

        Assert.assertNotEquals(defaultListener, sut.getListener());

        click(sut);
        Assert.assertEquals(1, actionResults.size());
        Assert.assertEquals("Executed", actionResults.get(0));
    }

    @Test
    public void setListener_null() {
        ComponentEventListener<MenuItemClickEvent> defaultListener = sut.getListener();
        sut.setListener(null);
        Assert.assertNotEquals(defaultListener, sut.getListener());

        // No NullPointerException
        click(sut);
    }

    private void click(MenuItem menuItem) {
        menuItem.getListener().onComponentEvent(new MenuItemClickEvent(menuItem, false));
    }
}
