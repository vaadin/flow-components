/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.card;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/**
 * Unit tests for the {@link Card} component.
 */
public class CardTest {

    @Test
    public void titleNullByDefault() {
        var card = new Card();
        Assert.assertNull(card.getTitle());
    }

    @Test
    public void titleUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getTitle, Card::setTitle);
    }

    @Test
    public void setTitle_slotAttributeSet() {
        setSlotContent_slotContentIsSet(Card::setTitle, Card.TITLE_SLOT_NAME);
    }

    @Test
    public void subtitleNullByDefault() {
        var card = new Card();
        Assert.assertNull(card.getSubtitle());
    }

    @Test
    public void subtitleUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getSubtitle, Card::setSubtitle);
    }

    @Test
    public void setSubtitle_slotAttributeSet() {
        setSlotContent_slotContentIsSet(Card::setSubtitle,
                Card.SUBTITLE_SLOT_NAME);
    }

    @Test
    public void mediaNullByDefault() {
        var card = new Card();
        Assert.assertNull(card.getMedia());
    }

    @Test
    public void mediaUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getMedia, Card::setMedia);
    }

    @Test
    public void setMedia_slotAttributeSet() {
        setSlotContent_slotContentIsSet(Card::setMedia, Card.MEDIA_SLOT_NAME);
    }

    @Test
    public void headerNullByDefault() {
        var card = new Card();
        Assert.assertNull(card.getHeader());
    }

    @Test
    public void headerUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeader, Card::setHeader);
    }

    @Test
    public void setHeader_slotAttributeSet() {
        setSlotContent_slotContentIsSet(Card::setHeader, Card.HEADER_SLOT_NAME);
    }

    @Test
    public void headerPrefixNullByDefault() {
        var card = new Card();
        Assert.assertNull(card.getHeaderPrefix());
    }

    @Test
    public void headerPrefixUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeaderPrefix,
                Card::setHeaderPrefix);
    }

    @Test
    public void setHeaderPrefix_slotAttributeSet() {
        setSlotContent_slotContentIsSet(Card::setHeaderPrefix,
                Card.HEADER_PREFIX_SLOT_NAME);
    }

    @Test
    public void headerSuffixNullByDefault() {
        var card = new Card();
        Assert.assertNull(card.getHeaderSuffix());
    }

    @Test
    public void headerSuffixUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeaderSuffix,
                Card::setHeaderSuffix);
    }

    @Test
    public void setHeaderSuffix_slotAttributeSet() {
        setSlotContent_slotContentIsSet(Card::setHeaderSuffix,
                Card.HEADER_SUFFIX_SLOT_NAME);
    }

    @Test
    public void hasNoFooterComponentsByDefault() {
        var card = new Card();
        Assert.assertEquals(0, card.getFooterComponents().length);
    }

    @Test
    public void addToFooterInArray_footerUpdated() {
        var card = new Card();
        var firstFooterContent = new Div();
        var secondFooterContent = new Div();
        card.addToFooter(firstFooterContent, secondFooterContent);
        var footerComponents = card.getFooterComponents();
        Assert.assertEquals(2, footerComponents.length);
        Assert.assertEquals(firstFooterContent, footerComponents[0]);
        Assert.assertEquals(secondFooterContent, footerComponents[1]);
    }

    @Test
    public void addToFooterSeparately_footerUpdated() {
        var card = new Card();
        var firstFooterContent = new Div();
        var secondFooterContent = new Div();
        card.addToFooter(firstFooterContent);
        card.addToFooter(secondFooterContent);
        var footerComponents = card.getFooterComponents();
        Assert.assertEquals(2, footerComponents.length);
        Assert.assertEquals(firstFooterContent, footerComponents[0]);
        Assert.assertEquals(secondFooterContent, footerComponents[1]);
    }

    @Test
    public void addToFooter_slotAttributeSet() {
        var card = new Card();
        var footerComponents = List.of(new Div(), new Span());
        footerComponents.forEach(card::addToFooter);
        footerComponents.forEach(footerComponent -> {
            var slotElement = footerComponent.getElement().getParent();
            Assert.assertNotNull(slotElement);
            Assert.assertEquals(Card.FOOTER_SLOT_NAME,
                    slotElement.getAttribute("slot"));
        });
    }

    @Test
    public void cardHasNoChildrenByDefault() {
        var card = new Card();
        Assert.assertTrue(card.getChildren().findAny().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void addNullCollection_throwsNullPointerException() {
        var card = new Card();
        card.add((Collection<Component>) null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullArray_throwsNullPointerException() {
        var card = new Card();
        card.add((Component[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullComponentInArray_throwsNullPointerException() {
        var card = new Card();
        card.add(new Div(), null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullComponentInCollection_throwsNullPointerException() {
        var card = new Card();
        card.add(Arrays.asList(new Div(), null));
    }

    @Test
    public void addNullComponentInCollection_childrenNotUpdated() {
        var card = new Card();
        try {
            card.add(new Div(), null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        Assert.assertTrue(card.getChildren().findAny().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeContentWithAnotherParent_throwsIllegalArgumentException() {
        var contentWithAnotherParent = new Span();
        var otherParent = new Div();
        otherParent.add(contentWithAnotherParent);
        var card = new Card();
        var content = new Div();
        card.add(content);
        card.remove(List.of(contentWithAnotherParent));
    }

    @Test
    public void removeContentWithNoParent_childrenNotUpdated() {
        var contentWithNoParent = new Span();
        var card = new Card();
        var content = new Div();
        card.add(content);
        card.remove(List.of(contentWithNoParent));
        Assert.assertEquals(List.of(content), card.getChildren().toList());
    }

    @Test(expected = NullPointerException.class)
    public void cardWithContent_removeNull_throwsNullPointerException() {
        var card = new Card();
        card.add(new Span());
        card.remove(Collections.singletonList(null));
    }

    @Test(expected = NullPointerException.class)
    public void cardWithoutContent_removeNull_throwsNullPointerException() {
        var card = new Card();
        card.remove(Collections.singletonList(null));
    }

    @Test
    public void removeAll_allChildrenRemoved() {
        var card = new Card();
        card.add(new Div(), new Div());
        card.removeAll();
        Assert.assertTrue(card.getChildren().findAny().isEmpty());
    }

    @Test
    public void emptyCard_addComponentAtIndex_componentAddedAtCorrectIndex() {
        var card = new Card();
        var component = new Div();
        card.addComponentAtIndex(0, component);
        var firstComponent = card.getChildren().findFirst();
        Assert.assertTrue(firstComponent.isPresent());
        Assert.assertEquals(component, firstComponent.get());
    }

    @Test
    public void addComponentAtNextIndex_componentAddedAtCorrectIndex() {
        var card = new Card();
        card.add(new Span());
        var component = new Div();
        var initialCount = (int) card.getChildren().count();
        card.addComponentAtIndex(initialCount, component);
        var firstComponent = card.getChildren().skip(initialCount).findFirst();
        Assert.assertTrue(firstComponent.isPresent());
        Assert.assertEquals(component, firstComponent.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtOutOfBoundsIndex_throwsIllegalArgumentException() {
        var card = new Card();
        card.add(new Span());
        var component = new Div();
        card.addComponentAtIndex((int) card.getChildren().count() + 1,
                component);
    }

    @Test
    public void addComponentAtIndex_componentsAddedAtCorrectIndexes() {
        var card = new Card();
        card.add(new Span());
        var component = new Div();
        card.addComponentAtIndex(0, component);
        var firstComponent = card.getChildren().findFirst();
        Assert.assertTrue(firstComponent.isPresent());
        Assert.assertEquals(component, firstComponent.get());
    }

    @Test
    public void cardThemeVariantsEmptyByDefault() {
        var card = new Card();
        Assert.assertTrue(card.getThemeNames().isEmpty());
    }

    @Test
    public void ariaRoleEmptyByDefault() {
        var card = new Card();
        Assert.assertTrue(card.getAriaRole().isEmpty());
    }

    @Test
    public void setAriaRole_ariaRoleUpdated() {
        var ariaRole = "custom-role";
        var card = new Card();
        card.setAriaRole(ariaRole);
        Assert.assertTrue(card.getAriaRole().isPresent());
        Assert.assertEquals(ariaRole, card.getAriaRole().get());
    }

    @Test
    public void setAriaRoleNull_ariaRoleUpdated() {
        var card = new Card();
        card.setAriaRole("custom-role");
        card.setAriaRole(null);
        Assert.assertTrue(card.getAriaRole().isEmpty());
    }

    private static void setSlotContent_slotContentIsSet(
            BiConsumer<Card, Component> setter, String slotName) {
        var card = new Card();
        var slotContent = new Div();
        setter.accept(card, slotContent);
        var slotElement = slotContent.getElement().getParent();
        Assert.assertNotNull(slotElement);
        Assert.assertEquals(slotName, slotElement.getAttribute("slot"));
    }

    private static void slotBasedFieldUpdatedCorrectly(
            Function<Card, Component> getter,
            BiConsumer<Card, Component> setter) {
        var card = new Card();
        var component = new Span("Text");
        setter.accept(card, component);
        Assert.assertEquals(component, getter.apply(card));
        var anotherComponent = new Div("New Text");
        setter.accept(card, anotherComponent);
        Assert.assertEquals(anotherComponent, getter.apply(card));
        setter.accept(card, null);
        Assert.assertNull(getter.apply(card));
    }
}
