package com.vaadin.flow.component.applayout.test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.component.applayout.testbench.MenuItemElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.orderedlayout.testbench.HorizontalLayoutElement;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.testbench.TestBenchElement;
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
                $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement().countMenuItems());
    }

    @Test
    public void menuItemsByIndex() {
        MenuItemElement menuItem =
                $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement().getMenuItemAt(1);
        Assert.assertEquals("Action 2", menuItem.getTitle());
        Assert.assertEquals("vaadin:safe-lock", menuItem.getIcon().getAttribute("icon"));
    }

    @Test
    public void menuItemsByTitle() {
        MenuItemElement menuItem =
                $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement().getMenuItemWithTitle("Page 1");
        Assert.assertEquals("Page 1", menuItem.getTitle());
        Assert.assertEquals("vaadin:location-arrow", menuItem.getIcon().getAttribute("icon"));
    }

    @Test
    public void selectedMenuItem() {
        MenuItemElement selectedMenuItem =
                $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement().getSelectedMenuItem();
        Assert.assertEquals("Home", selectedMenuItem.getTitle());
    }

    @Test
    public void actionMenuItems_executeAction() {
        MenuItemElement action1 =
                $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement().getMenuItemWithTitle("Action 1");
        action1.click();
        Assert.assertEquals("Action 1 executed!",
                 $(NotificationElement.class).waitForFirst().getText().trim());

        MenuItemElement action2 =
                $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement().getMenuItemWithTitle("Action 2");
        action2.click();
        waitUntil(e -> $(NotificationElement.class).all().size() == 2);
        Assert.assertEquals("Action 2 executed!",
                $(NotificationElement.class).last().getText().trim());
    }

    @Test
    public void actionMenuItems_doNotUpdateSelectedMenuItem() {
        String initialSelectionTitle = $(AppLayoutElement.class)
                .waitForFirst().getAppLayoutMenuElement().getSelectedMenuItem().getTitle();

        $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement()
                .getMenuItemWithTitle("Action 1").click();

        String selectionTitleAfterActionClick = $(AppLayoutElement.class)
                .waitForFirst().getAppLayoutMenuElement().getSelectedMenuItem().getTitle();

        Assert.assertEquals(initialSelectionTitle, selectionTitleAfterActionClick);
    }

    @Test
    public void routingMenuItems() {
        String initialSelectionTitle = $(AppLayoutElement.class)
                .waitForFirst().getAppLayoutMenuElement().getSelectedMenuItem().getTitle();

        Assert.assertEquals("Home", initialSelectionTitle);
        Assert.assertEquals("Welcome home",
                $(AppLayoutElement.class).waitForFirst().getContent().getText());

        $(AppLayoutElement.class).waitForFirst().getAppLayoutMenuElement()
                .getMenuItemWithTitle("Page 2").click();

        String selectionTitleAfterActionClick = $(AppLayoutElement.class)
                .waitForFirst().getAppLayoutMenuElement().getSelectedMenuItem().getTitle();

        Assert.assertEquals("Page 2", selectionTitleAfterActionClick);
        Assert.assertEquals("This is Page 2",
                $(AppLayoutElement.class).waitForFirst().getContent().getText());

        Assert.assertNotEquals(initialSelectionTitle, selectionTitleAfterActionClick);
    }

    @Test
    public void navigateToNotFound() {
        getDriver().get(getBaseURL() + "/nonexistingpage");
        Assert.assertTrue($(AppLayoutElement.class).waitForFirst().getContent()
                .getText().contains("Could not navigate to"));

        Assert.assertFalse($(AppLayoutElement.class).first().getAppLayoutMenuElement().$(MenuItemElement.class)
                .attribute("selected", "").exists());
    }

    @Test
    public void menuWithRoutingLinksWorks() {
        getDriver().get(getBaseURL() + "/Page3");
        Assert.assertEquals("This is Page 3",
            $(AppLayoutElement.class).waitForFirst().getContent().getText());

        TestBenchElement linkPage4 = $(AppLayoutElement.class).waitForFirst().getMenu(TestBenchElement.class).$("a").get(1);
        Assert.assertEquals("Page 4",linkPage4.getText());
        linkPage4.click();
        Assert.assertEquals("This is Page 4",
            $(AppLayoutElement.class).waitForFirst().getContent().getText());

    }
}
