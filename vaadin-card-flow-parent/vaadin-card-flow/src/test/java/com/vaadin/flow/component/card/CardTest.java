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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/**
 * Unit tests for the {@link Card} component.
 */
public class CardTest {

    private Card card;

    @Before
    public void setup() {
        var ui = new UI();
        UI.setCurrent(ui);
        card = new Card();
        card.setFeatureFlagEnabled();
        ui.add(card);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void titleNullByDefault() {
        Assert.assertNull(card.getTitle());
    }

    @Test
    public void titleUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getTitle, Card::setTitle);
    }

    @Test
    public void setTitle_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setTitle, "title");
    }

    @Test
    public void stringTitleIsEmptyByDefault() {
        Assert.assertEquals("", card.getTitleAsText());
    }

    @Test
    public void setStringTitle_titleIsSet() {
        var title = "Some Title";
        card.setTitle(title);
        Assert.assertEquals(title, card.getTitleAsText());
        Assert.assertEquals(title, card.getElement().getProperty("cardTitle"));
        title = "Other Title";
        card.setTitle(title, 2);
        Assert.assertEquals(title, card.getTitleAsText());
        Assert.assertEquals(title, card.getElement().getProperty("cardTitle"));
    }

    @Test
    public void setStringTitle_setNullStringTitle_titleCleared() {
        card.setTitle("Some Title");
        card.setTitle((String) null);
        Assert.assertEquals("", card.getTitleAsText());
    }

    @Test
    public void setTitleHeadingLevel_throwsIllegalArgumentExceptionOnlyForOutOfRange() {
        card.setTitleHeadingLevel(null);
        card.setTitleHeadingLevel(1);
        card.setTitleHeadingLevel(6);
        Assert.assertThrows(IllegalArgumentException.class,
                () -> card.setTitleHeadingLevel(7));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> card.setTitleHeadingLevel(0));

    @Test
    public void setStringTitle_setComponentTitle_stringTitleIsRemoved() {
        card.setTitle("Some Title");
        card.setTitle(new Div("Other Title"));
        Assert.assertEquals("", card.getTitleAsText());
    }

    @Test
    public void setComponentTitle_setStringTitle_componentTitleIsRemoved() {
        card.setTitle(new Div("Other Title"));
        card.setTitle("Some Title");
        Assert.assertNull(card.getTitle());
    }

    @Test
    public void subtitleNullByDefault() {
        Assert.assertNull(card.getSubtitle());
    }

    @Test
    public void subtitleUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getSubtitle, Card::setSubtitle);
    }

    @Test
    public void setSubtitle_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setSubtitle, "subtitle");
    }

    @Test
    public void mediaNullByDefault() {
        Assert.assertNull(card.getMedia());
    }

    @Test
    public void mediaUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getMedia, Card::setMedia);
    }

    @Test
    public void setMedia_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setMedia, "media");
    }

    @Test
    public void headerNullByDefault() {
        Assert.assertNull(card.getHeader());
    }

    @Test
    public void headerUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeader, Card::setHeader);
    }

    @Test
    public void setHeader_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setHeader, "header");
    }

    @Test
    public void headerPrefixNullByDefault() {
        Assert.assertNull(card.getHeaderPrefix());
    }

    @Test
    public void headerPrefixUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeaderPrefix,
                Card::setHeaderPrefix);
    }

    @Test
    public void setHeaderPrefix_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setHeaderPrefix,
                "header-prefix");
    }

    @Test
    public void headerSuffixNullByDefault() {
        Assert.assertNull(card.getHeaderSuffix());
    }

    @Test
    public void headerSuffixUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeaderSuffix,
                Card::setHeaderSuffix);
    }

    @Test
    public void setHeaderSuffix_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setHeaderSuffix,
                "header-suffix");
    }

    @Test
    public void hasNoFooterComponentsByDefault() {
        Assert.assertEquals(0, card.getFooterComponents().length);
    }

    @Test
    public void addToFooterInArray_footerUpdated() {
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
        var footerComponents = List.of(new Div(), new Span());
        footerComponents.forEach(card::addToFooter);
        footerComponents
                .forEach(footerComponent -> Assert.assertEquals("footer",
                        footerComponent.getElement().getAttribute("slot")));
    }

    @Test
    public void cardHasNoChildrenByDefault() {
        Assert.assertTrue(card.getChildren().findAny().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void addNullCollection_throwsNullPointerException() {
        card.add((Collection<Component>) null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullArray_throwsNullPointerException() {
        card.add((Component[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullComponentInArray_throwsNullPointerException() {
        card.add(new Div(), null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullComponentInCollection_throwsNullPointerException() {
        card.add(Arrays.asList(new Div(), null));
    }

    @Test
    public void addNullComponentInCollection_childrenNotUpdated() {
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
        var content = new Div();
        card.add(content);
        card.remove(List.of(contentWithAnotherParent));
    }

    @Test
    public void removeContentWithNoParent_childrenNotUpdated() {
        var contentWithNoParent = new Span();
        var content = new Div();
        card.add(content);
        card.remove(List.of(contentWithNoParent));
        Assert.assertEquals(List.of(content), card.getChildren().toList());
    }

    @Test(expected = NullPointerException.class)
    public void cardWithContent_removeNull_throwsNullPointerException() {
        card.add(new Span());
        card.remove(Collections.singletonList(null));
    }

    @Test(expected = NullPointerException.class)
    public void cardWithoutContent_removeNull_throwsNullPointerException() {
        card.remove(Collections.singletonList(null));
    }

    @Test
    public void removeAll_onlyRemovesContent() {
        card.setTitle(new Div());
        card.setSubtitle(new Div());
        card.setHeader(new Div());
        card.setHeaderPrefix(new Div());
        card.setHeaderSuffix(new Div());
        card.setMedia(new Div());
        card.removeAll();
        Assert.assertNotNull(card.getTitle());
        Assert.assertNotNull(card.getSubtitle());
        Assert.assertNotNull(card.getHeader());
        Assert.assertNotNull(card.getHeaderPrefix());
        Assert.assertNotNull(card.getHeaderSuffix());
        Assert.assertNotNull(card.getMedia());
    }

    @Test
    public void removeAll_allChildrenRemoved() {
        var component1 = new Div();
        var component2 = new Span();
        card.add(component1, component2);
        card.removeAll();
        Assert.assertTrue(card.getChildren().findAny().isEmpty());
        Assert.assertFalse(component1.isAttached());
        Assert.assertFalse(component2.isAttached());
    }

    @Test
    public void emptyCard_addComponentAtIndex_componentAddedAtCorrectIndex() {
        var component = new Div();
        card.addComponentAtIndex(0, component);
        var firstComponent = card.getChildren().findFirst();
        Assert.assertTrue(firstComponent.isPresent());
        Assert.assertEquals(component, firstComponent.get());
        Assert.assertTrue(component.isAttached());
    }

    @Test
    public void addComponentAtNextIndex_componentAddedAtCorrectIndex() {
        card.add(new Span());
        var component = new Div();
        var initialCount = (int) card.getChildren().count();
        card.addComponentAtIndex(initialCount, component);
        var firstComponent = card.getChildren().skip(initialCount).findFirst();
        Assert.assertTrue(firstComponent.isPresent());
        Assert.assertEquals(component, firstComponent.get());
        Assert.assertTrue(component.isAttached());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtOutOfBoundsIndex_throwsIllegalArgumentException() {
        card.add(new Span());
        var component = new Div();
        card.addComponentAtIndex((int) card.getChildren().count() + 1,
                component);
    }

    @Test
    public void addComponentAtIndex_componentsAddedAtCorrectIndexes() {
        card.add(new Span());
        var component = new Div();
        card.addComponentAtIndex(0, component);
        var firstComponent = card.getChildren().findFirst();
        Assert.assertTrue(firstComponent.isPresent());
        Assert.assertEquals(component, firstComponent.get());
        Assert.assertTrue(component.isAttached());
    }

    @Test
    public void cardThemeVariantsEmptyByDefault() {
        Assert.assertTrue(card.getThemeNames().isEmpty());
    }

    @Test
    public void ariaRoleEmptyByDefault() {
        Assert.assertTrue(card.getAriaRole().isEmpty());
    }

    @Test
    public void setAriaRole_ariaRoleUpdated() {
        var ariaRole = "custom-role";
        card.setAriaRole(ariaRole);
        Assert.assertTrue(card.getAriaRole().isPresent());
        Assert.assertEquals(ariaRole, card.getAriaRole().get());
    }

    @Test
    public void setAriaRoleNull_ariaRoleUpdated() {
        card.setAriaRole("custom-role");
        card.setAriaRole(null);
        Assert.assertTrue(card.getAriaRole().isEmpty());
    }

    private void setSlotContent_slotAttributeIsSet(
            BiConsumer<Card, Component> setter, String slotName) {
        var slotContent = new Div();
        setter.accept(card, slotContent);
        Assert.assertEquals(slotName,
                slotContent.getElement().getAttribute("slot"));
    }

    private void slotBasedFieldUpdatedCorrectly(
            Function<Card, Component> getter,
            BiConsumer<Card, Component> setter) {
        // Set slot component
        var component = new Span("Text");
        setter.accept(card, component);
        Assert.assertEquals(component, getter.apply(card));
        Assert.assertTrue(component.isAttached());
        Assert.assertTrue(isAncestor(component, card));
        // Set another slot component
        var anotherComponent = new Div("New Text");
        setter.accept(card, anotherComponent);
        Assert.assertEquals(anotherComponent, getter.apply(card));
        Assert.assertTrue(anotherComponent.isAttached());
        Assert.assertTrue(isAncestor(anotherComponent, card));
        Assert.assertFalse(component.isAttached());
        Assert.assertFalse(isAncestor(component, card));
        // Set null
        setter.accept(card, null);
        Assert.assertNull(getter.apply(card));
        Assert.assertFalse(anotherComponent.isAttached());
        Assert.assertFalse(isAncestor(anotherComponent, card));
    }

    private boolean isAncestor(Component component, Card probableAncestor) {
        var parent = component.getParent();
        while (parent.isPresent()) {
            if (parent.get().equals(probableAncestor)) {
                return true;
            }
            parent = parent.get().getParent();
        }
        return false;
    }
}
