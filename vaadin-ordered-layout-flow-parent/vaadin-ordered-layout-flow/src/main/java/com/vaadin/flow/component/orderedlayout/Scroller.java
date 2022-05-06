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
package com.vaadin.flow.component.orderedlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * Scroller is a component container which enables scrolling overflowing
 * content.
 *
 * Scroll direction can be configured with
 * {@link #setScrollDirection(ScrollDirection)}
 */
@Tag("vaadin-scroller")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/scroller", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-ordered-layout", version = "23.1.0-beta1")
@JsModule("@vaadin/scroller/vaadin-scroller.js")
public class Scroller extends Component implements HasSize, HasStyle {

    private static final String SCROLL_DIRECTION_PROPERTY = "scrollDirection";

    private Component content;

    /**
     * Constructs an empty scroller. Content can be set with
     * {@link #setContent(Component)} and scroll direction defaults to
     * {@link ScrollDirection#BOTH} and can be set with
     * {@link #setScrollDirection(ScrollDirection)}.
     *
     */
    public Scroller() {
    }

    /**
     * Convenience constructor to create a scroller with the given content.
     * Scroll direction defaults to {@link ScrollDirection#BOTH} and can be set
     * with {@link #setScrollDirection(ScrollDirection)}.
     *
     * @param content
     *            the content of this scroller
     * @see #setContent(Component)
     */
    public Scroller(Component content) {
        this();
        setContent(content);
    }

    /**
     * Convenience constructor to create a scroller with the given content and
     * scroll direction.
     *
     * @param content
     *            the content of this scroller
     * @param scrollDirection
     *            scroll direction that the scroller will have
     */
    public Scroller(Component content, ScrollDirection scrollDirection) {
        this();
        setContent(content);
        setScrollDirection(scrollDirection);
    }

    /**
     * Convenience constructor to create an empty scroller with the given scroll
     * direction. Content can be set with {@link #setContent(Component)}
     *
     * @param scrollDirection
     *            scroll direction that the scroller will have
     */
    public Scroller(ScrollDirection scrollDirection) {
        this();
        setScrollDirection(scrollDirection);
    }

    /**
     * Sets the content of this scroller.
     *
     * The content must always be set, either with a constructor parameter or by
     * calling this method.
     *
     * @param content
     *            a component to use as content
     */
    public void setContent(Component content) {
        if (this.content != null) {
            this.content.getElement().removeFromParent();
        }
        this.content = content;
        if (content != null) {
            this.getElement().appendChild(content.getElement());
        }
    }

    /**
     * Gets the content of this scroller.
     *
     * @return the component used as content
     */
    public Component getContent() {
        return content;
    }

    /**
     * Sets the scroll direction for this scroller. Defaults to
     * {@link ScrollDirection#BOTH}.
     *
     * @param scrollDirection
     *            {@link ScrollDirection#BOTH} to enable both vertical and
     *            horizontal scrollbars. {@link ScrollDirection#HORIZONTAL} to
     *            enable only horizontal scrollbars.
     *            {@link ScrollDirection#VERTICAL} to enable only vertical
     *            scrollbars. {@link ScrollDirection#NONE} to disable both
     *            vertical and horizontal scrollbars.
     */
    public void setScrollDirection(ScrollDirection scrollDirection) {
        requireNonNull(scrollDirection, "Scroll direction must not be null");
        getElement().setProperty(SCROLL_DIRECTION_PROPERTY,
                scrollDirection.toWebComponentValue());
    }

    /**
     * Returns the scroll direction for this scroller.
     *
     * @see #setScrollDirection(ScrollDirection)
     *
     * @return the scroll direction for this scroller.
     */
    public ScrollDirection getScrollDirection() {
        return ScrollDirection.fromWebComponentValue(
                getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    /**
     * Enum for the values of the ScrollDirection property.
     *
     * @see Scroller#setScrollDirection(ScrollDirection)
     */
    public enum ScrollDirection {
        VERTICAL, HORIZONTAL, BOTH, NONE;

        private String toWebComponentValue() {
            return BOTH == this ? null
                    : this.name().toLowerCase(Locale.ENGLISH);
        }

        private static ScrollDirection fromWebComponentValue(
                String stringValue) {
            return stringValue != null ? valueOf(stringValue.toUpperCase())
                    : BOTH;
        }
    }
}
