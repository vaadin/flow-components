/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import static java.util.Objects.requireNonNull;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * ScrollContainer is a component container, which will display scrollbars when
 * its content is overflowing.
 * 
 * Scrollbars can be configured with
 * {@link #setScrollDirection(ScrollDirection)}
 */
@Tag("vaadin-scroll-container")
@NpmPackage(value = "@vaadin/vaadin-ordered-layout", version = "1.2.0-alpha2")
@JsModule("@vaadin/vaadin-ordered-layout/vaadin-scroll-container.js")
@HtmlImport("frontend://bower_components/vaadin-ordered-layout/src/vaadin-scroll-container.html")
public class ScrollContainer extends Component implements HasSize, HasStyle {

    private static final String SCROLL_DIRECTION_PROPERTY = "scrollDirection";

    private Component content;

    /**
     * Constructs an empty container. Content can be set with
     * {@link #setContent(Component)} and scroll direction defaults to
     * {@link ScrollDirection#BOTH} and can be set with
     * {@link #setScrollDirection(ScrollDirection)}.
     * 
     */
    public ScrollContainer() {
    }

    /**
     * Convenience constructor to create a container with the given content.
     * Scroll direction defaults to {@link ScrollDirection#BOTH} and can be set
     * with {@link #setScrollDirection(ScrollDirection)}.
     * 
     * @param content
     *            the content of this container
     * @see #setContent(Component)
     */
    public ScrollContainer(Component content) {
        this();
        setContent(content);
    }

    /**
     * Convenience constructor to create a container with the given content and
     * scroll direction.
     *
     * @param content
     *            the content of this container
     * @param scrollDirection
     *            scroll direction that the container will have
     */
    public ScrollContainer(Component content, ScrollDirection scrollDirection) {
        this();
        setContent(content);
        setScrollDirection(scrollDirection);
    }

    /**
     * Convenience constructor to create an empty container with the given
     * scroll direction. Content can be set with {@link #setContent(Component)}
     *
     * @param scrollDirection
     *            scroll direction that the container will have
     */
    public ScrollContainer(ScrollDirection scrollDirection) {
        this();
        setScrollDirection(scrollDirection);
    }

    /**
     * Sets the content of this container.
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
        this.getElement().appendChild(content.getElement());
    }

    /**
     * Gets the content of this container.
     *
     * @return the component used as content
     */
    public Component getContent() {
        return content;
    }

    /**
     * Sets the scroll direction for this container. Defaults to
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
     * Returns the scroll direction for this container.
     * 
     * @see #setScrollDirection(ScrollDirection)
     * 
     * @return the scroll direction for this container.
     */
    public ScrollDirection getScrollDirection() {
        return ScrollDirection.fromWebComponentValue(
                getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    /**
     * Enum for the values of the ScrollDirection property.
     * 
     * @see ScrollContainer#setScrollDirection(ScrollDirection)
     */
    public enum ScrollDirection {
        VERTICAL, HORIZONTAL, BOTH, NONE;

        public String toWebComponentValue() {
            return BOTH == this ? null : this.name().toLowerCase();
        }

        public static ScrollDirection fromWebComponentValue(
                String stringValue) {
            return stringValue != null ? valueOf(stringValue.toUpperCase())
                    : BOTH;
        }
    }
}
