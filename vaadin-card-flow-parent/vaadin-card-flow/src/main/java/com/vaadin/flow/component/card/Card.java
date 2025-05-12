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
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/card", version = "24.8.0-alpha18")
@JsModule("@vaadin/card/src/vaadin-card.js")
public class Card extends Component implements HasSize,
        HasThemeVariant<CardVariant>, HasComponents, HasAriaLabel {

    private static final String MEDIA_SLOT_NAME = "media";
    private static final String TITLE_SLOT_NAME = "title";
    private static final String SUBTITLE_SLOT_NAME = "subtitle";
    private static final String HEADER_SLOT_NAME = "header";
    private static final String HEADER_PREFIX_SLOT_NAME = "header-prefix";
    private static final String HEADER_SUFFIX_SLOT_NAME = "header-suffix";
    private static final String FOOTER_SLOT_NAME = "footer";

    private static final String CARD_TITLE_PROPERTY = "cardTitle";
    private static final String TITLE_HEADING_LEVEL_PROPERTY = "titleHeadingLevel";

    private Element contentRoot;

    private boolean featureFlagEnabled;

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
        SlotUtils.setSlot(this, MEDIA_SLOT_NAME, media);
    }

    /**
     * Gets the current media component.
     *
     * @return the media component, or {@code null} if none is set
     */
    public Component getMedia() {
        return SlotUtils.getChildInSlot(this, MEDIA_SLOT_NAME);
    }

    /**
     * Sets the {@code cardTitle} property. If a {@link #setHeader(Component)
     * header component} is set, the title will not be displayed. Setting a
     * title this way removes any title component set using
     * {@link #setTitle(Component)}. Setting {@code null} or empty string
     * removes any previously set {@code String} titles.
     *
     * @param title
     *            the title property
     * @see #setTitle(String, Integer)
     * @see #setTitleHeadingLevel(Integer)
     * @see #getTitleAsText()
     */
    public void setTitle(String title) {
        doSetTitle((Component) null);
        doSetTitle(title);
    }

    /**
     * Sets the title and the heading level for the title. If a
     * {@link #setHeader(Component) header component} is set, the title will not
     * be displayed. Setting a title this way removes any title component set
     * using {@link #setTitle(Component)}. Setting {@code null} or empty title
     * removes any previously set {@code String} titles.
     *
     * @param title
     *            the title property
     * @param titleHeadingLevel
     *            the title heading level property,
     * @see #setTitle(String)
     * @see #setTitleHeadingLevel(Integer)
     * @see #getTitleAsText()
     */
    public void setTitle(String title, Integer titleHeadingLevel) {
        setTitleHeadingLevel(titleHeadingLevel);
        setTitle(title);
    }

    /**
     * Sets the title heading level property for the titles set using
     * {@link #setTitle(String)}. The default is 2. Setting {@code null} resets
     * it to default. Does not affect the title components set using
     * {@link #setTitle(Component)}.
     *
     * @param titleHeadingLevel
     *            the title heading level property, {@code null} to remove
     */
    public void setTitleHeadingLevel(Integer titleHeadingLevel) {
        if (titleHeadingLevel == null) {
            getElement().removeProperty(TITLE_HEADING_LEVEL_PROPERTY);
        } else {
            getElement().setProperty(TITLE_HEADING_LEVEL_PROPERTY,
                    titleHeadingLevel);
        }
    }

    /**
     * Sets the component used as the card's title. If a
     * {@link #setHeader(Component) header component} is set, the title will not
     * be displayed. This also removes any previously set {@code String} titles.
     * <p>
     * Passing {@code null} removes the current title from the card.
     *
     * @param title
     *            the title component, or {@code null} to remove
     */
    public void setTitle(Component title) {
        doSetTitle((String) null);
        doSetTitle(title);
    }

    /**
     * Gets the value of the {@code cardTitle} property. Returns empty if no
     * title is set.
     *
     * @return the value of the title property
     */
    public String getTitleAsText() {
        return getElement().getProperty(CARD_TITLE_PROPERTY, "");
    }

    /**
     * Gets the current title component set using {@link #setTitle(Component)}.
     *
     * @return the title component, or {@code null} if none is set
     */
    public Component getTitle() {
        return SlotUtils.getChildInSlot(this, TITLE_SLOT_NAME);
    }

    /**
     * Sets the component used as the card's subtitle. If a
     * {@link #setHeader(Component) header component} is set, the subtitle will
     * not be displayed.
     * <p>
     * Passing {@code null} removes the current subtitle from the card.
     *
     * @param subtitle
     *            the subtitle component, or {@code null} to remove
     */
    public void setSubtitle(Component subtitle) {
        SlotUtils.setSlot(this, SUBTITLE_SLOT_NAME, subtitle);
    }

    /**
     * Gets the current subtitle component.
     *
     * @return the subtitle component, or {@code null} if none is set
     */
    public Component getSubtitle() {
        return SlotUtils.getChildInSlot(this, SUBTITLE_SLOT_NAME);
    }

    /**
     * Sets the component used as the card's header. The header is prioritized
     * over the {@link #setTitle(Component) title} and
     * {@link #setSubtitle(Component) subtitle} components, and will be
     * displayed instead.
     * <p>
     * Passing {@code null} removes the current header component from the card.
     *
     * @param header
     *            the header component, or {@code null} to remove
     */
    public void setHeader(Component header) {
        SlotUtils.setSlot(this, HEADER_SLOT_NAME, header);
    }

    /**
     * Gets the current header component.
     *
     * @return the header component, or {@code null} if none is set
     */
    public Component getHeader() {
        return SlotUtils.getChildInSlot(this, HEADER_SLOT_NAME);
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
        SlotUtils.setSlot(this, HEADER_PREFIX_SLOT_NAME, headerPrefix);
    }

    /**
     * Gets the current header prefix component.
     *
     * @return the header prefix component, or {@code null} if none is set
     */
    public Component getHeaderPrefix() {
        return SlotUtils.getChildInSlot(this, HEADER_PREFIX_SLOT_NAME);
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
        SlotUtils.setSlot(this, HEADER_SUFFIX_SLOT_NAME, headerSuffix);
    }

    /**
     * Gets the current header suffix component.
     *
     * @return the header suffix component, or {@code null} if none is set
     */
    public Component getHeaderSuffix() {
        return SlotUtils.getChildInSlot(this, HEADER_SUFFIX_SLOT_NAME);
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
        var componentsToAdd = Arrays.stream(footerComponent)
                .map(component -> Objects.requireNonNull(component,
                        "Component to add cannot be null"))
                .toList();
        componentsToAdd.forEach(component -> SlotUtils.addToSlot(this,
                FOOTER_SLOT_NAME, component));
    }

    /**
     * Gets all components added to the card's footer.
     *
     * @return an array of footer components
     */
    public Component[] getFooterComponents() {
        return SlotUtils.getElementsInSlot(this, FOOTER_SLOT_NAME)
                .map(Element::getComponent).map(Optional::orElseThrow)
                .toArray(Component[]::new);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag();
    }

    /**
     * Gets the feature flags for the current UI.
     * <p>
     * Not private in order to support mocking
     *
     * @return the current set of feature flags
     */
    FeatureFlags getFeatureFlags() {
        return FeatureFlags
                .get(UI.getCurrent().getSession().getService().getContext());
    }

    /**
     * Only for test use.
     */
    void setFeatureFlagEnabled() {
        featureFlagEnabled = true;
    }

    /**
     * Checks whether the Card component feature flag is active. Succeeds if the
     * flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#CARD_COMPONENT} feature is not
     *             enabled
     */
    private void checkFeatureFlag() {
        boolean enabled = featureFlagEnabled
                || getFeatureFlags().isEnabled(FeatureFlags.CARD_COMPONENT);
        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    private void initContentRoot() {
        contentRoot = new Element("div");
        contentRoot.getStyle().set("display", "contents");
        getElement().appendChild(contentRoot);
    }

    private void doSetTitle(String title) {
        if (title == null) {
            getElement().removeProperty(CARD_TITLE_PROPERTY);
        } else {
            getElement().setProperty(CARD_TITLE_PROPERTY, title);
        }
    }

    private void doSetTitle(Component title) {
        SlotUtils.setSlot(this, TITLE_SLOT_NAME, title);
    }
}
