/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import org.junit.Test;

import java.util.Set;

/**
 * @author Vaadin Ltd.
 */
public class AvatarTest {

    private Avatar avatar = new Avatar();
    private Avatar constructedAvatar;
    String name = "foo bar";
    String abbr = "fb";
    String url = "https://vaadin.com/";

    @Test
    public void shouldCreateEmptyAvatarWithDefaultState() {
        Assert.assertNull("Initial name is null", avatar.getName());
        Assert.assertNull("Initial abbreviation is null",
                avatar.getAbbreviation());
        Assert.assertNull("Initial image is null", avatar.getImage());
    }

    @Test
    public void setName_getName() {
        avatar.setName(name);
        Assert.assertEquals(avatar.getName(), name);
    }

    @Test
    public void setAbbr_getAbbr() {
        avatar.setAbbreviation(abbr);
        Assert.assertEquals(avatar.getAbbreviation(), abbr);
    }

    @Test
    public void setImgLink_getImgLink() {
        avatar.setImage(url);
        Assert.assertEquals(avatar.getImage(), url);
    }

    @Test
    public void constructAvatarWithName() {
        constructedAvatar = new Avatar(name);
        Assert.assertEquals(constructedAvatar.getName(), name);
    }

    @Test
    public void constructAvatarWithNameAndImage() {
        constructedAvatar = new Avatar(name, url);

        Assert.assertEquals(constructedAvatar.getName(), name);
        Assert.assertEquals(constructedAvatar.getImage(), url);
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        avatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        Set<String> themeNames = avatar.getThemeNames();
        Assert.assertTrue(
                themeNames.contains(AvatarVariant.LUMO_LARGE.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeTheme_doesNotContainThemeVariant() {
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

}
