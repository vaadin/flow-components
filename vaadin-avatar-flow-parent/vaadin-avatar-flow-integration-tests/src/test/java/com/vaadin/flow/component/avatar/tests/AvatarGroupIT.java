/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.avatar.tests;

import com.vaadin.flow.component.avatar.testbench.AvatarGroupElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.startsWith;

/**
 * Integration tests for the {@link AvatarGroupPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-avatar/avatar-group-test")
public class AvatarGroupIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setItems_updateItemProperties_avatarsUpdated() {
        WebElement updateItems = findElement(By.id("update-items"));

        Assert.assertEquals("YY", getAvatarAbbr(0));
        Assert.assertEquals("SK", getAvatarAbbr(1));

        updateItems.click();

        Assert.assertEquals("FF", getAvatarAbbr(0));
        Assert.assertEquals("FF", getAvatarAbbr(1));
    }

    @Test
    public void avatarGroupAttached_setItemsWithImageResource_imageLoaded() {
        clickElementWithJs("set-items-with-resource");
        String imageUrl = $(AvatarGroupElement.class).first()
                .getAvatarElement(0).getPropertyString("img");
        Assert.assertThat(imageUrl, startsWith("VAADIN/dynamic"));
        checkLogsForErrors(); // would fail if the image wasn't hosted
    }

    private String getAvatarAbbr(int index) {
        return $(AvatarGroupElement.class).waitForFirst()
                .getAvatarElement(index).getAbbr();
    }
}
