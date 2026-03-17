/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.shared.HasThemeVariant;

class AvatarTest {

    private Avatar avatar;

    @BeforeEach
    void setup() {
        avatar = new Avatar();
    }

    @Test
    void shouldCreateEmptyAvatarWithDefaultState() {
        Assertions.assertNull(avatar.getName(), "Initial name is null");
        Assertions.assertNull(avatar.getAbbreviation(),
                "Initial abbreviation is null");
        Assertions.assertNull(avatar.getImage(), "Initial image is null");
    }

    @Test
    void setName_getName() {
        avatar.setName("foo");
        Assertions.assertEquals("foo", avatar.getName());
    }

    @Test
    void setAbbr_getAbbr() {
        avatar.setAbbreviation("fb");
        Assertions.assertEquals("fb", avatar.getAbbreviation());
    }

    @Test
    void setImgLink_getImgLink() {
        avatar.setImage("https://vaadin.com/");
        Assertions.assertEquals("https://vaadin.com/", avatar.getImage());
    }

    @Test
    void constructAvatarWithName() {
        Avatar avatar = new Avatar("foo");
        Assertions.assertEquals("foo", avatar.getName());
    }

    @Test
    void constructAvatarWithNameAndImage() {
        Avatar avatar = new Avatar("foo", "https://vaadin.com/");

        Assertions.assertEquals("foo", avatar.getName());
        Assertions.assertEquals("https://vaadin.com/", avatar.getImage());
    }

    @Test
    void setI18n() {
        Avatar.AvatarI18n i18n = new Avatar.AvatarI18n()
                .setAnonymous("anonyymi");

        avatar.setI18n(i18n);
        Assertions.assertEquals(i18n, avatar.getI18n());
    }

    @Test
    void setTooltipEnabled_isTooltipEnabled() {
        avatar.setTooltipEnabled(true);
        Assertions.assertEquals(true, avatar.isTooltipEnabled());
        Assertions.assertTrue(
                avatar.getElement().getProperty("withTooltip", false));

        avatar.setTooltipEnabled(false);
        Assertions.assertEquals(false, avatar.isTooltipEnabled());
        Assertions.assertFalse(
                avatar.getElement().getProperty("withTooltip", false));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Avatar.class));
    }
}
