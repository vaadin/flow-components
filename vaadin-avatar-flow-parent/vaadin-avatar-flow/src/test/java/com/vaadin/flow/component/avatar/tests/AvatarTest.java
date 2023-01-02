/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.component.avatar.tests;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class AvatarTest {

    private Avatar avatar;

    @Before
    public void setup() {
        avatar = new Avatar();
    }

    @Test
    public void shouldCreateEmptyAvatarWithDefaultState() {
        Assert.assertNull("Initial name is null", avatar.getName());
        Assert.assertNull("Initial abbreviation is null",
                avatar.getAbbreviation());
        Assert.assertNull("Initial image is null", avatar.getImage());
    }

    @Test
    public void setName_getName() {
        avatar.setName("foo");
        Assert.assertEquals(avatar.getName(), "foo");
    }

    @Test
    public void setAbbr_getAbbr() {
        avatar.setAbbreviation("fb");
        Assert.assertEquals(avatar.getAbbreviation(), "fb");
    }

    @Test
    public void setImgLink_getImgLink() {
        avatar.setImage("https://vaadin.com/");
        Assert.assertEquals(avatar.getImage(), "https://vaadin.com/");
    }

    @Test
    public void constructAvatarWithName() {
        Avatar avatar = new Avatar("foo");
        Assert.assertEquals(avatar.getName(), "foo");
    }

    @Test
    public void constructAvatarWithNameAndImage() {
        Avatar avatar = new Avatar("foo", "https://vaadin.com/");

        Assert.assertEquals(avatar.getName(), "foo");
        Assert.assertEquals(avatar.getImage(), "https://vaadin.com/");
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        avatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        Set<String> themeNames = avatar.getThemeNames();
        Assert.assertTrue(
                themeNames.contains(AvatarVariant.LUMO_LARGE.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_doesNotContainThemeVariant() {
        avatar.addThemeVariants(AvatarVariant.LUMO_LARGE);
        avatar.removeThemeVariants(AvatarVariant.LUMO_LARGE);

        Set<String> themeNames = avatar.getThemeNames();
        Assert.assertFalse(
                themeNames.contains(AvatarVariant.LUMO_LARGE.getVariantName()));
    }

    @Test
    public void setI18n() {
        Avatar.AvatarI18n i18n = new Avatar.AvatarI18n()
                .setAnonymous("anonyymi");

        avatar.setI18n(i18n);
        Assert.assertEquals(i18n, avatar.getI18n());
    }

    @Test
    public void setTooltipEnabled_isTooltipEnabled() {
        avatar.setTooltipEnabled(true);
        Assert.assertEquals(avatar.isTooltipEnabled(), true);
        Assert.assertTrue(
                avatar.getElement().getProperty("withTooltip", false));

        avatar.setTooltipEnabled(false);
        Assert.assertEquals(avatar.isTooltipEnabled(), false);
        Assert.assertFalse(
                avatar.getElement().getProperty("withTooltip", false));
    }
}
