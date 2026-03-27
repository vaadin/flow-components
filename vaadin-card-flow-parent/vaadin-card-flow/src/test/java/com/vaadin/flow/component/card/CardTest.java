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
package com.vaadin.flow.component.card;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.tests.MockUIExtension;

/**
 * Unit tests for the {@link Card} component.
 */
class CardTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private Card card;

    @BeforeEach
    void setup() {
        card = new Card();
        ui.add(card);
    }

    @Test
    void titleNullByDefault() {
        Assertions.assertNull(card.getTitle());
    }

    @Test
    void titleUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getTitle, Card::setTitle);
    }

    @Test
    void setTitle_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setTitle, "title");
    }

    @Test
    void stringTitleIsEmptyByDefault() {
        Assertions.assertEquals("", card.getTitleAsText());
    }

    @Test
    void setStringTitle_titleIsSet() {
        var title = "Some Title";
        card.setTitle(title);
        Assertions.assertEquals(title, card.getTitleAsText());
        Assertions.assertEquals(title,
                card.getElement().getProperty("cardTitle"));
        title = "Other Title";
        card.setTitle(title, 2);
        Assertions.assertEquals(title, card.getTitleAsText());
        Assertions.assertEquals(title,
                card.getElement().getProperty("cardTitle"));
    }

    @Test
    void setStringTitle_setNullStringTitle_titleCleared() {
        card.setTitle("Some Title");
        card.setTitle((String) null);
        Assertions.assertEquals("", card.getTitleAsText());
    }

    @Test
    void setTitleHeadingLevel_elementPropertyIsUpdated() {
        var titleHeadingLevel = 1;
        card.setTitleHeadingLevel(titleHeadingLevel);
        Assertions.assertEquals(titleHeadingLevel,
                card.getElement().getProperty("titleHeadingLevel", -1));
        titleHeadingLevel = 7;
        card.setTitleHeadingLevel(titleHeadingLevel);
        Assertions.assertEquals(titleHeadingLevel,
                card.getElement().getProperty("titleHeadingLevel", -1));
    }

    @Test
    void setTitleHeadingLevelNull_elementPropertyIsRemoved() {
        card.setTitleHeadingLevel(1);
        card.setTitleHeadingLevel(null);
        Assertions.assertFalse(
                card.getElement().hasProperty("titleHeadingLevel"));
    }

    @Test
    void setStringTitle_setComponentTitle_stringTitleIsRemoved() {
        card.setTitle("Some Title");
        card.setTitle(new Div("Other Title"));
        Assertions.assertEquals("", card.getTitleAsText());
    }

    @Test
    void setComponentTitle_setStringTitle_componentTitleIsRemoved() {
        card.setTitle(new Div("Other Title"));
        card.setTitle("Some Title");
        Assertions.assertNull(card.getTitle());
    }

    @Test
    void subtitleNullByDefault() {
        Assertions.assertNull(card.getSubtitle());
    }

    @Test
    void subtitleUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getSubtitle, Card::setSubtitle);
    }

    @Test
    void setSubtitle_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setSubtitle, "subtitle");
    }

    @Test
    void setStringSubtitle_subtitleIsSet() {
        var subtitle = "Some Subtitle";
        card.setSubtitle(subtitle);
        Assertions.assertNotNull(card.getSubtitle());
        Assertions.assertTrue(card.getSubtitle() instanceof Span);
        Assertions.assertEquals(subtitle,
                ((Span) card.getSubtitle()).getText());
    }

    @Test
    void setStringSubtitle_setNullStringSubtitle_subtitleCleared() {
        card.setSubtitle("Some Subtitle");
        card.setSubtitle((String) null);
        Assertions.assertNull(card.getSubtitle());
    }

    @Test
    void setStringSubtitle_setComponentSubtitle_stringSubtitleIsReplaced() {
        card.setSubtitle("Some Subtitle");
        var newSubtitle = new Div("Other Subtitle");
        card.setSubtitle(newSubtitle);
        Assertions.assertEquals(newSubtitle, card.getSubtitle());
    }

    @Test
    void setComponentSubtitle_setStringSubtitle_componentSubtitleIsReplaced() {
        var componentSubtitle = new Div("Component Subtitle");
        card.setSubtitle(componentSubtitle);
        var stringSubtitle = "String Subtitle";
        card.setSubtitle(stringSubtitle);
        Assertions.assertTrue(card.getSubtitle() instanceof Span);
        Assertions.assertEquals(stringSubtitle,
                ((Span) card.getSubtitle()).getText());
    }

    @Test
    void mediaNullByDefault() {
        Assertions.assertNull(card.getMedia());
    }

    @Test
    void mediaUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getMedia, Card::setMedia);
    }

    @Test
    void setMedia_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setMedia, "media");
    }

    @Test
    void headerNullByDefault() {
        Assertions.assertNull(card.getHeader());
    }

    @Test
    void headerUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeader, Card::setHeader);
    }

    @Test
    void setHeader_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setHeader, "header");
    }

    @Test
    void headerPrefixNullByDefault() {
        Assertions.assertNull(card.getHeaderPrefix());
    }

    @Test
    void headerPrefixUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeaderPrefix,
                Card::setHeaderPrefix);
    }

    @Test
    void setHeaderPrefix_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setHeaderPrefix,
                "header-prefix");
    }

    @Test
    void headerSuffixNullByDefault() {
        Assertions.assertNull(card.getHeaderSuffix());
    }

    @Test
    void headerSuffixUpdatedCorrectly() {
        slotBasedFieldUpdatedCorrectly(Card::getHeaderSuffix,
                Card::setHeaderSuffix);
    }

    @Test
    void setHeaderSuffix_slotAttributeSet() {
        setSlotContent_slotAttributeIsSet(Card::setHeaderSuffix,
                "header-suffix");
    }

    @Test
    void hasNoFooterComponentsByDefault() {
        Assertions.assertEquals(0, card.getFooterComponents().length);
    }

    @Test
    void addToFooterInArray_footerUpdated() {
        var firstFooterContent = new Div();
        var secondFooterContent = new Div();
        card.addToFooter(firstFooterContent, secondFooterContent);
        var footerComponents = card.getFooterComponents();
        Assertions.assertEquals(2, footerComponents.length);
        Assertions.assertEquals(firstFooterContent, footerComponents[0]);
        Assertions.assertEquals(secondFooterContent, footerComponents[1]);
    }

    @Test
    void addToFooterSeparately_footerUpdated() {
        var firstFooterContent = new Div();
        var secondFooterContent = new Div();
        card.addToFooter(firstFooterContent);
        card.addToFooter(secondFooterContent);
        var footerComponents = card.getFooterComponents();
        Assertions.assertEquals(2, footerComponents.length);
        Assertions.assertEquals(firstFooterContent, footerComponents[0]);
        Assertions.assertEquals(secondFooterContent, footerComponents[1]);
    }

    @Test
    void addToFooter_slotAttributeSet() {
        var footerComponents = List.of(new Div(), new Span());
        footerComponents.forEach(card::addToFooter);
        footerComponents
                .forEach(footerComponent -> Assertions.assertEquals("footer",
                        footerComponent.getElement().getAttribute("slot")));
    }

    @Test
    void getChildren_emptyByDefault() {
        Assertions.assertTrue(card.getChildren().findAny().isEmpty());
    }

    @Test
    void getChildren_onlyReturnsComponentsFromDefaultSlot() {
        card.setTitle(new Div());
        card.setSubtitle(new Div());
        card.setHeader(new Div());
        card.setHeaderPrefix(new Div());
        card.setHeaderSuffix(new Div());
        card.setMedia(new Div());

        Assertions.assertTrue(card.getChildren().findAny().isEmpty());

        var content = new Span();
        card.add(content);

        Assertions.assertEquals(List.of(content), card.getChildren().toList());
    }

    @Test
    void removeAll_onlyRemovesContent() {
        card.setTitle(new Div());
        card.setSubtitle(new Div());
        card.setHeader(new Div());
        card.setHeaderPrefix(new Div());
        card.setHeaderSuffix(new Div());
        card.setMedia(new Div());
        card.removeAll();
        Assertions.assertNotNull(card.getTitle());
        Assertions.assertNotNull(card.getSubtitle());
        Assertions.assertNotNull(card.getHeader());
        Assertions.assertNotNull(card.getHeaderPrefix());
        Assertions.assertNotNull(card.getHeaderSuffix());
        Assertions.assertNotNull(card.getMedia());
    }

    @Test
    void removeAll_allChildrenRemoved() {
        var component1 = new Div();
        var component2 = new Span();
        card.add(component1, component2);
        card.removeAll();
        Assertions.assertTrue(card.getChildren().findAny().isEmpty());
        Assertions.assertFalse(component1.isAttached());
        Assertions.assertFalse(component2.isAttached());
    }

    @Test
    void emptyCard_addComponentAtIndex_componentAddedAtCorrectIndex() {
        var component = new Span();
        card.addComponentAtIndex(0, component);
        Assertions.assertEquals(List.of(component),
                card.getChildren().toList());
        Assertions.assertTrue(component.isAttached());
    }

    @Test
    void addComponentAtNextIndex_componentAddedAtCorrectIndex() {
        var first = new Span();
        card.add(first);
        var second = new Span();
        card.addComponentAtIndex(1, second);
        Assertions.assertEquals(List.of(first, second),
                card.getChildren().toList());
        Assertions.assertTrue(second.isAttached());
    }

    @Test
    void addComponentAtNegativeIndex_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> card.addComponentAtIndex(-1, new Span()));
    }

    @Test
    void addComponentAtOutOfBoundsIndex_throwsIllegalArgumentException() {
        // Add components to other slots to check that index only operates on
        // default slot
        card.setTitle(new Div());
        card.setSubtitle(new Div());
        card.setHeader(new Div());
        card.setHeaderPrefix(new Div());
        card.setHeaderSuffix(new Div());
        card.setMedia(new Div());

        card.add(new Span());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> card.addComponentAtIndex(2, new Span()));
    }

    @Test
    void addComponentAtIndex_componentsAddedAtCorrectIndexes() {
        var first = new Span();
        card.add(first);

        var second = new Span();
        card.addComponentAtIndex(0, second);
        Assertions.assertEquals(List.of(second, first),
                card.getChildren().toList());

        var third = new Span();
        card.addComponentAtIndex(2, third);
        Assertions.assertEquals(List.of(second, first, third),
                card.getChildren().toList());

        var fourth = new Span();
        card.addComponentAtIndex(1, fourth);
        Assertions.assertEquals(List.of(second, fourth, first, third),
                card.getChildren().toList());
    }

    @Test
    void addComponentAtIndex_ignoresComponentsFromOtherSlots() {
        card.setTitle(new Div());
        card.setSubtitle(new Div());
        card.setHeader(new Div());
        card.setHeaderPrefix(new Div());
        card.setHeaderSuffix(new Div());
        card.setMedia(new Div());

        var first = new Span();
        var second = new Span();

        card.add(first);
        card.add(second);

        var third = new Span();
        card.addComponentAtIndex(1, third);
        Assertions.assertEquals(List.of(first, third, second),
                card.getChildren().toList());
    }

    @Test
    void addComponentAtIndex_withAlreadyAddedComponent() {
        card.setTitle(new Div());
        card.setSubtitle(new Div());
        card.setHeader(new Div());
        card.setHeaderPrefix(new Div());
        card.setHeaderSuffix(new Div());
        card.setMedia(new Div());

        var first = new Span();
        var second = new Span();
        var third = new Span();

        card.add(first);
        card.add(second);
        card.add(third);

        card.addComponentAtIndex(2, third);
        Assertions.assertEquals(List.of(first, second, third),
                card.getChildren().toList());

        card.addComponentAtIndex(0, third);
        Assertions.assertEquals(List.of(third, first, second),
                card.getChildren().toList());
    }

    @Test
    void ariaRoleEmptyByDefault() {
        Assertions.assertTrue(card.getAriaRole().isEmpty());
    }

    @Test
    void setAriaRole_ariaRoleUpdated() {
        var ariaRole = "custom-role";
        card.setAriaRole(ariaRole);
        Assertions.assertTrue(card.getAriaRole().isPresent());
        Assertions.assertEquals(ariaRole, card.getAriaRole().get());
    }

    @Test
    void setAriaRoleNull_ariaRoleUpdated() {
        card.setAriaRole("custom-role");
        card.setAriaRole(null);
        Assertions.assertTrue(card.getAriaRole().isEmpty());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions
                .assertTrue(HasThemeVariant.class.isAssignableFrom(Card.class));
    }

    private void setSlotContent_slotAttributeIsSet(
            BiConsumer<Card, Component> setter, String slotName) {
        var slotContent = new Div();
        setter.accept(card, slotContent);
        Assertions.assertEquals(slotName,
                slotContent.getElement().getAttribute("slot"));
    }

    private void slotBasedFieldUpdatedCorrectly(
            Function<Card, Component> getter,
            BiConsumer<Card, Component> setter) {
        // Set slot component
        var component = new Span("Text");
        setter.accept(card, component);
        Assertions.assertEquals(component, getter.apply(card));
        Assertions.assertTrue(component.isAttached());
        Assertions.assertTrue(isAncestor(component, card));
        // Set another slot component
        var anotherComponent = new Div("New Text");
        setter.accept(card, anotherComponent);
        Assertions.assertEquals(anotherComponent, getter.apply(card));
        Assertions.assertTrue(anotherComponent.isAttached());
        Assertions.assertTrue(isAncestor(anotherComponent, card));
        Assertions.assertFalse(component.isAttached());
        Assertions.assertFalse(isAncestor(component, card));
        // Set null
        setter.accept(card, null);
        Assertions.assertNull(getter.apply(card));
        Assertions.assertFalse(anotherComponent.isAttached());
        Assertions.assertFalse(isAncestor(anotherComponent, card));
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
