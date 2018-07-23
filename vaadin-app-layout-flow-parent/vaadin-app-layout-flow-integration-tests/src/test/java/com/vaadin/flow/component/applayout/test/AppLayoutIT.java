package com.vaadin.flow.component.applayout.test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.component.applayout.testbench.MenuItemElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppLayoutIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void branding() {
        Assert.assertEquals("Vaadin",
                $(AppLayoutElement.class).waitForFirst().getBranding().getText());
    }

    @Test
    public void content() {
        Assert.assertEquals("Welcome home",
                $(AppLayoutElement.class).waitForFirst().getContent().getText());
    }

    @Test
    public void countMenuItems() {
        Assert.assertEquals(6,
                $(AppLayoutElement.class).waitForFirst().countMenuItems());
    }

    @Test
    public void menuItemsByIndex() {
        MenuItemElement menuItem =
                $(AppLayoutElement.class).waitForFirst().getMenuItemAt(1);
        Assert.assertEquals("Action 2", menuItem.getTitle());
        Assert.assertEquals("vaadin:safe-lock", menuItem.getIcon().getAttribute("icon"));
    }

    @Test
    public void menuItemsByTitle() {
        MenuItemElement menuItem =
                $(AppLayoutElement.class).waitForFirst().getMenuItemWithTitle("Page 1");
        Assert.assertEquals("Page 1", menuItem.getTitle());
        Assert.assertEquals("vaadin:location-arrow", menuItem.getIcon().getAttribute("icon"));
    }

    @Test
    public void selectedMenuItem() {
        MenuItemElement selectedMenuItem =
                $(AppLayoutElement.class).waitForFirst().getSelectedMenuItem();
        Assert.assertEquals("Home", selectedMenuItem.getTitle());
    }

    @Test
    public void actionMenuItems_executeAction() {
        MenuItemElement action1 =
                $(AppLayoutElement.class).waitForFirst().getMenuItemWithTitle("Action 1");
        action1.click();
        Assert.assertEquals("Action 1 executed!",
                $(NotificationElement.class).first().getText().trim());

        MenuItemElement action2 =
                $(AppLayoutElement.class).waitForFirst().getMenuItemWithTitle("Action 2");
        action2.click();
        Assert.assertEquals("Action 2 executed!",
                $(NotificationElement.class).last().getText().trim());
    }

    @Test
    public void actionMenuItems_doNotUpdateSelectedMenuItem() {
        String initialSelectionTitle = $(AppLayoutElement.class)
                .waitForFirst().getSelectedMenuItem().getTitle();

        $(AppLayoutElement.class).waitForFirst()
                .getMenuItemWithTitle("Action 1").click();

        String selectionTitleAfterActionClick = $(AppLayoutElement.class)
                .waitForFirst().getSelectedMenuItem().getTitle();

        Assert.assertEquals(initialSelectionTitle, selectionTitleAfterActionClick);
    }

    @Test
    public void routingMenuItems() {
        String initialSelectionTitle = $(AppLayoutElement.class)
                .waitForFirst().getSelectedMenuItem().getTitle();

        Assert.assertEquals("Home", initialSelectionTitle);
        Assert.assertEquals("Welcome home",
                $(AppLayoutElement.class).waitForFirst().getContent().getText());

        $(AppLayoutElement.class).waitForFirst()
                .getMenuItemWithTitle("Page 2").click();

        String selectionTitleAfterActionClick = $(AppLayoutElement.class)
                .waitForFirst().getSelectedMenuItem().getTitle();

        Assert.assertEquals("Page 2", selectionTitleAfterActionClick);
        Assert.assertEquals("This is Page 2",
                $(AppLayoutElement.class).waitForFirst().getContent().getText());

        Assert.assertNotEquals(initialSelectionTitle, selectionTitleAfterActionClick);
    }
}
