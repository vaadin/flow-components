/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.card.tests;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;

public class CardTest {

    private Card card;

    @Test
    public void emptyCtor() {
        card = new Card();
    }

    @Test
    public void addThemeVariants_themeNamesAreAdded() {
        card = new Card();

        // Test each variant individually
        card.addThemeVariants(CardVariant.LUMO_ELEVATED);
        assertThemeVariants(card, CardVariant.LUMO_ELEVATED);

        card.addThemeVariants(CardVariant.LUMO_OUTLINED);
        assertThemeVariants(card, CardVariant.LUMO_ELEVATED, CardVariant.LUMO_OUTLINED);

        card.addThemeVariants(CardVariant.MATERIAL_ELEVATED);
        assertThemeVariants(card, CardVariant.LUMO_ELEVATED, CardVariant.LUMO_OUTLINED,
                CardVariant.MATERIAL_ELEVATED);
    }

    @Test
    public void addAllVariants_allThemeNamesAreAdded() {
        card = new Card();
        card.addThemeVariants(CardVariant.values());
        assertThemeVariants(card,
                CardVariant.LUMO_ELEVATED,
                CardVariant.LUMO_OUTLINED,
                CardVariant.MATERIAL_ELEVATED,
                CardVariant.MATERIAL_OUTLINED,
                CardVariant.HORIZONTAL,
                CardVariant.STRETCH_MEDIA,
                CardVariant.COVER_MEDIA);
    }

    @Test
    public void removeThemeVariants_themeNamesAreRemoved() {
        card = new Card();
        // Add all variants
        card.addThemeVariants(CardVariant.values());

        // Remove some variants
        card.removeThemeVariants(CardVariant.LUMO_ELEVATED, CardVariant.HORIZONTAL);

        // Assert remaining variants
        assertThemeVariants(card,
                CardVariant.LUMO_OUTLINED,
                CardVariant.MATERIAL_ELEVATED,
                CardVariant.MATERIAL_OUTLINED,
                CardVariant.STRETCH_MEDIA,
                CardVariant.COVER_MEDIA);
    }

    private void assertThemeVariants(Card card, CardVariant... variants) {
        String themeNames = card.getElement().getAttribute("theme");
        for (CardVariant variant : variants) {
            Assert.assertTrue("Theme name '" + variant.getVariantName()
                    + "' not found in theme attribute '" + themeNames + "'",
                    themeNames.contains(variant.getVariantName()));
        }
    }
}
