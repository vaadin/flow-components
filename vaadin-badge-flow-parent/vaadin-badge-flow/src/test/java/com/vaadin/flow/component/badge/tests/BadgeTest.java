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
package com.vaadin.flow.component.badge.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.html.Span;

class BadgeTest {

    @Test
    void defaultConstructor_emptyBadge() {
        var badge = new Badge();
        Assertions.assertNull(badge.getText());
        Assertions.assertNull(badge.getNumber());
        Assertions.assertNull(badge.getIcon());
    }

    @Test
    void textConstructor_setsText() {
        var badge = new Badge("New");
        Assertions.assertEquals("New", badge.getText());
    }

    @Test
    void iconConstructor_setsIcon() {
        var icon = new Span();
        var badge = new Badge(icon);
        Assertions.assertEquals(icon, badge.getIcon());
    }

    @Test
    void textAndIconConstructor_setsTextAndIcon() {
        var icon = new Span();
        var badge = new Badge("New", icon);
        Assertions.assertEquals("New", badge.getText());
        Assertions.assertEquals(icon, badge.getIcon());
    }

    @Test
    void textAndNumberConstructor_setsTextAndNumber() {
        var badge = new Badge("Messages", 5);
        Assertions.assertEquals("Messages", badge.getText());
        Assertions.assertEquals((Integer) 5, badge.getNumber());
    }

    @Test
    void textNumberAndIconConstructor_setsAll() {
        var icon = new Span();
        var badge = new Badge("Messages", 5, icon);
        Assertions.assertEquals("Messages", badge.getText());
        Assertions.assertEquals((Integer) 5, badge.getNumber());
        Assertions.assertEquals(icon, badge.getIcon());
    }

    @Test
    void setText_getText() {
        var badge = new Badge();

        badge.setText("Status");
        Assertions.assertEquals("Status", badge.getText());
        Assertions.assertEquals("Status", badge.getElement().getText());

        badge.setText("");
        Assertions.assertEquals("", badge.getText());
        Assertions.assertEquals("", badge.getElement().getText());

        badge.setText("Status");
        badge.setText(null);
        Assertions.assertNull(badge.getText());
        Assertions.assertEquals("", badge.getElement().getText());
    }

    @Test
    void setNumber_getNumber() {
        var badge = new Badge();

        badge.setNumber(5);
        Assertions.assertEquals((Integer) 5, badge.getNumber());
        Assertions.assertEquals("5", badge.getElement().getProperty("number"));

        badge.setNumber(null);
        Assertions.assertNull(badge.getNumber());
        Assertions.assertFalse(badge.getElement().hasProperty("number"));
    }

    @Test
    void setContent_getContent() {
        var badge = new Badge();
        var content0 = new Span();
        var content1 = new Span();

        badge.setContent(content0);
        Assertions.assertEquals(content0, badge.getContent());
        Assertions.assertEquals(badge, content0.getParent().get());

        badge.setContent(content1);
        Assertions.assertEquals(content1, badge.getContent());
        Assertions.assertEquals(badge, content1.getParent().get());
        Assertions.assertFalse(content0.getParent().isPresent());

        badge.setContent(null);
        Assertions.assertNull(badge.getContent());
        Assertions.assertFalse(content1.getParent().isPresent());
    }

    @Test
    void setContent_doesNotAffectIcon() {
        var badge = new Badge();
        var icon = new Span();
        badge.setIcon(icon);

        var content = new Span();
        badge.setContent(content);

        Assertions.assertEquals(icon, badge.getIcon());
        Assertions.assertEquals(content, badge.getContent());
    }

    @Test
    void setContent_replacesText() {
        var badge = new Badge("Text");
        var content = new Span();
        badge.setContent(content);

        Assertions.assertNull(badge.getText());
        Assertions.assertEquals(content, badge.getContent());
    }

    @Test
    void setText_replacesContent() {
        var badge = new Badge();
        var content = new Span();
        badge.setContent(content);

        badge.setText("Text");

        Assertions.assertEquals("Text", badge.getText());
        Assertions.assertNull(badge.getContent());
        Assertions.assertFalse(content.getParent().isPresent());
    }

    @Test
    void setText_thenSetContent_thenSetText() {
        var badge = new Badge();

        badge.setText("foo");
        Assertions.assertEquals("foo", badge.getText());

        var bar = new Span("bar");
        badge.setContent(bar);
        Assertions.assertNull(badge.getText());
        Assertions.assertEquals(bar, badge.getContent());

        badge.setText("baz");
        Assertions.assertEquals("baz", badge.getText());
        Assertions.assertNull(badge.getContent());
        Assertions.assertFalse(bar.getParent().isPresent());
    }

    @Test
    void getContent_doesNotReturnTextOrIcon() {
        var badge = new Badge("Text");
        var icon = new Span();
        badge.setIcon(icon);

        Assertions.assertNull(badge.getContent());
    }

    @Test
    void setRole_getRole() {
        var badge = new Badge();
        Assertions.assertNull(badge.getRole());

        badge.setRole("status");
        Assertions.assertEquals("status", badge.getRole());

        badge.setRole(null);
        Assertions.assertNull(badge.getRole());
    }

    @Test
    void setIcon_getIcon() {
        var badge = new Badge();
        var icon0 = new Span();
        var icon1 = new Span();

        badge.setIcon(icon0);
        Assertions.assertEquals(icon0, badge.getIcon());
        Assertions.assertEquals(badge, icon0.getParent().get());
        Assertions.assertEquals("icon",
                icon0.getElement().getAttribute("slot"));

        badge.setIcon(icon1);
        Assertions.assertEquals(icon1, badge.getIcon());
        Assertions.assertEquals(badge, icon1.getParent().get());
        Assertions.assertEquals("icon",
                icon1.getElement().getAttribute("slot"));
        Assertions.assertFalse(icon0.getParent().isPresent());
        Assertions.assertFalse(icon0.getElement().hasAttribute("slot"));

        badge.setIcon(null);
        Assertions.assertNull(badge.getIcon());
        Assertions.assertFalse(icon1.getParent().isPresent());
        Assertions.assertFalse(icon1.getElement().hasAttribute("slot"));
    }
}
