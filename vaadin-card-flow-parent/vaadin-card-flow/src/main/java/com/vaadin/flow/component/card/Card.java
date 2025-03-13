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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;

/**
 * Card is a visual content container for creating a card-based layout.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-card")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha3")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/card", version = "24.8.0-alpha3")
@JsModule("@vaadin/card/src/vaadin-card.js")
public class Card extends Component implements HasSize,
        HasThemeVariant<CardVariant>, HasComponents, HasAriaLabel {

    static final String MEDIA_SLOT_NAME = "media";
    static final String TITLE_SLOT_NAME = "title";
    static final String SUBTITLE_SLOT_NAME = "subtitle";
    static final String HEADER_SLOT_NAME = "header";
    static final String HEADER_PREFIX_SLOT_NAME = "header-prefix";
    static final String HEADER_SUFFIX_SLOT_NAME = "header-suffix";
    static final String FOOTER_SLOT_NAME = "footer";

    private Element contentRoot;

    /**
     * Sets the component used as the card's media. The media slot is typically
     * used to display an image, icon, or other visual element.
     * <p>
     * Passing {@code null} removes the current media component from the card.
     *
     * @param media
     *            the media component, or {@code null} to remove
     */
    public void setMedia(Component media) {
        setSlotContent(media, MEDIA_SLOT_NAME);
    }

    /**
     * Gets the current media component.
     *
     * @return the media component, or {@code null} if none is set
     */
    public Component getMedia() {
        return getSlotContent(MEDIA_SLOT_NAME);
    }

    /**
     * Sets the component used as the card's title. If a header component is
     * set, the title will not be displayed.
     * <p>
     * Passing {@code null} removes the current title from the card.
     *
     * @param title
     *            the title component, or {@code null} to remove
     */
    public void setTitle(Component title) {
        setSlotContent(title, TITLE_SLOT_NAME);
    }

    /**
     * Gets the current title component.
     *
     * @return the title component, or {@code null} if none is set
     */
    public Component getTitle() {
        return getSlotContent(TITLE_SLOT_NAME);
    }

    /**
     * Sets the component used as the card's subtitle. If a header component is
     * set, the subtitle will not be displayed.
     * <p>
     * Passing {@code null} removes the current subtitle from the card.
     *
     * @param subtitle
     *            the subtitle component, or {@code null} to remove
     */
    public void setSubtitle(Component subtitle) {
        setSlotContent(subtitle, SUBTITLE_SLOT_NAME);
    }

    /**
     * Gets the current subtitle component.
     *
     * @return the subtitle component, or {@code null} if none is set
     */
    public Component getSubtitle() {
        return getSlotContent(SUBTITLE_SLOT_NAME);
    }

    /**
     * Sets the component used as the card's header. The header is prioritized
     * over the title and subtitle components, and will be displayed instead.
     * <p>
     * Passing {@code null} removes the current header component from the card.
     *
     * @param header
     *            the header component, or {@code null} to remove
     */
    public void setHeader(Component header) {
        setSlotContent(header, HEADER_SLOT_NAME);
    }

    /**
     * Gets the current header component.
     *
     * @return the header component, or {@code null} if none is set
     */
    public Component getHeader() {
        return getSlotContent(HEADER_SLOT_NAME);
    }

    /**
     * Sets a component to the header prefix slot, displayed before the header
     * content.
     * <p>
     * Passing {@code null} removes any existing prefix from the header.
     *
     * @param headerPrefix
     *            the header prefix component, or {@code null} to remove
     */
    public void setHeaderPrefix(Component headerPrefix) {
        setSlotContent(headerPrefix, HEADER_PREFIX_SLOT_NAME);
    }

    /**
     * Gets the current header prefix component.
     *
     * @return the header prefix component, or {@code null} if none is set
     */
    public Component getHeaderPrefix() {
        return getSlotContent(HEADER_PREFIX_SLOT_NAME);
    }

    /**
     * Sets a component to the header suffix slot, displayed after the header
     * content. Commonly used for decorative icons or custom suffixes.
     * <p>
     * Passing {@code null} removes any existing suffix from the header.
     *
     * @param headerSuffix
     *            the header suffix component, or {@code null} to remove
     */
    public void setHeaderSuffix(Component headerSuffix) {
        setSlotContent(headerSuffix, HEADER_SUFFIX_SLOT_NAME);
    }

    /**
     * Gets the current header suffix component.
     *
     * @return the header suffix component, or {@code null} if none is set
     */
    public Component getHeaderSuffix() {
        return getSlotContent(HEADER_SUFFIX_SLOT_NAME);
    }

    /**
     * Adds components to the card's footer slot.
     *
     * @param footerComponent
     *            the components to add into the footer
     */
    public void addToFooter(Component... footerComponent) {
        Objects.requireNonNull(footerComponent,
                "Components should not be null");
        var elementsToAdd = Arrays.stream(footerComponent)
                .map(component -> Objects.requireNonNull(component,
                        "Component to add cannot be null"))
                .map(Component::getElement).toList();
        if (!elementsToAdd.isEmpty()) {
            var footerRoot = new AtomicReference<Element>();
            SlotUtils.getElementsInSlot(this, FOOTER_SLOT_NAME).findFirst()
                    .ifPresentOrElse(footerRoot::set,
                            () -> footerRoot.set(initSlot(FOOTER_SLOT_NAME)));
            elementsToAdd.forEach(footerRoot.get()::appendChild);
        }
    }

    /**
     * Gets all components added to the card's footer.
     *
     * @return an array of footer components
     */
    public Component[] getFooterComponents() {
        return SlotUtils.getElementsInSlot(this, FOOTER_SLOT_NAME).findFirst()
                .map(element -> element.getChildren().map(Element::getComponent)
                        .map(Optional::orElseThrow).toArray(Component[]::new))
                .orElseGet(() -> new Component[0]);
    }

    @Override
    public Stream<Component> getChildren() {
        if (contentRoot == null) {
            return Stream.empty();
        }
        return contentRoot.getChildren()
                .map(element -> element.getComponent().orElseThrow());
    }

    @Override
    public void add(Collection<Component> components) {
        Objects.requireNonNull(components, "Components should not be null");
        var componentsToAdd = components.stream()
                .map(component -> Objects.requireNonNull(component,
                        "Component to add cannot be null"))
                .map(Component::getElement).toList();
        if (!componentsToAdd.isEmpty()) {
            if (contentRoot == null) {
                initContentRoot();
            }
            componentsToAdd.forEach(contentRoot::appendChild);
        }
    }

    @Override
    public void remove(Collection<Component> components) {
        Objects.requireNonNull(components, "Components should not be null.");
        var toRemove = new ArrayList<Component>(components.size());
        for (var component : components) {
            Objects.requireNonNull(component,
                    "Component to remove cannot be null");
            var parent = component.getElement().getParent();
            if (parent == null) {
                LoggerFactory.getLogger(getClass()).debug(
                        "Remove of a component with no parent does nothing.");
                continue;
            }
            if (contentRoot != null && contentRoot.equals(parent)) {
                toRemove.add(component);
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
        toRemove.stream().map(Component::getElement)
                .forEach(contentRoot::removeChild);
        if (contentRoot != null && contentRoot.getChildCount() == 0) {
            getElement().removeChild(contentRoot);
            contentRoot = null;
        }
    }

    @Override
    public void removeAll() {
        if (contentRoot != null) {
            contentRoot.removeAllChildren();
            getElement().removeChild(contentRoot);
            contentRoot = null;
        }
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        Objects.requireNonNull(component, "Component should not be null");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a component with a negative index");
        }
        if (contentRoot == null) {
            initContentRoot();
        }
        contentRoot.insertChild(index, component.getElement());
    }

    /**
     * Sets the ARIA role attribute on the card.
     *
     * @param role
     *            the ARIA role, or {@code null} to clear
     */
    public void setAriaRole(String role) {
        if (role == null) {
            getElement().removeAttribute("role");
        } else {
            getElement().setAttribute("role", role);
        }
    }

    /**
     * Gets the ARIA role attribute of the card.
     *
     * @return an optional ARIA role of the card if no ARIA role has been set
     */
    public Optional<String> getAriaRole() {
        return Optional.ofNullable(getElement().getAttribute("role"));
    }

    private void setSlotContent(Component slotContent, String slotName) {
        if (slotContent == null) {
            SlotUtils.getElementsInSlot(this, slotName).findFirst()
                    .ifPresent(Element::removeAllChildren);
            SlotUtils.clearSlot(this, slotName);
            return;
        }
        var slotElement = new AtomicReference<Element>();
        SlotUtils.getElementsInSlot(this, slotName).findFirst().ifPresentOrElse(
                slotElement::set, () -> slotElement.set(initSlot(slotName)));
        slotElement.get().removeAllChildren();
        slotElement.get().appendChild(slotContent.getElement());
    }

    private Element initSlot(String slotName) {
        var div = new Element("div");
        SlotUtils.setSlot(this, slotName, div);
        return div;
    }

    private Component getSlotContent(String slotName) {
        var slotElement = SlotUtils.getElementsInSlot(this, slotName)
                .findFirst();
        if (slotElement.isEmpty()) {
            return null;
        }
        var childElement = slotElement.get().getChildren().findFirst()
                .orElseThrow();
        return childElement.getComponent().orElseThrow();
    }

    private void initContentRoot() {
        contentRoot = new Element("div");
        getElement().appendChild(contentRoot);
    }
}
