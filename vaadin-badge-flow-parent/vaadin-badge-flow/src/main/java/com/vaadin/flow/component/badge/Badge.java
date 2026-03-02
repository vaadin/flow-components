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
package com.vaadin.flow.component.badge;

import java.util.Optional;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.SignalPropertySupport;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.signals.Signal;

/**
 * Badge is a component for displaying small pieces of information, such as
 * statuses, counts, or labels. It can display plain text, a number, an icon, or
 * a custom component, as well as combinations of these. Use theme variants to
 * customize the badge's appearance.
 *
 * <pre>
 * Badge badge = new Badge("Completed", LumoIcon.CHECKMARK.create());
 * badge.addThemeVariants(BadgeVariant.SUCCESS);
 * </pre>
 *
 * The {@link BadgeVariant#DOT dot} variant renders the badge as a small dot
 * indicator, visually hiding all content:
 *
 * <pre>
 * Badge badge = new Badge();
 * badge.addThemeVariants(BadgeVariant.DOT);
 * </pre>
 *
 * <h3>Accessibility</h3>
 *
 * When a Badge displays only an icon or a number, it may not provide enough
 * context for screen reader users. To address this, you can add descriptive
 * text via {@link #setText(String)} and use the {@link BadgeVariant#ICON_ONLY
 * icon-only} or {@link BadgeVariant#NUMBER_ONLY number-only} theme variant to
 * hide the text visually while keeping it available to screen readers:
 *
 * <pre>
 * Badge badge = new Badge("new messages", 5); // announced as "5 new messages"
 * badge.addThemeVariants(BadgeVariant.NUMBER_ONLY);
 * </pre>
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-badge")
@NpmPackage(value = "@vaadin/badge", version = "25.1.0-alpha9")
@JsModule("@vaadin/badge/src/vaadin-badge.js")
public class Badge extends Component
        implements HasSize, HasText, HasThemeVariant<BadgeVariant> {

    private static final String ICON_SLOT = "icon";

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        boolean enabled = featureFlags
                .isEnabled(BadgeFeatureFlagProvider.BADGE_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    private final Text textNode = new Text("");

    private final SignalPropertySupport<String> textSignalSupport = SignalPropertySupport
            .<String> create(this, this::updateText);

    /**
     * Default constructor. Creates an empty badge.
     */
    public Badge() {
    }

    /**
     * Creates a badge with a text inside.
     *
     * @param text
     *            the text inside the badge
     * @see #setText(String)
     */
    public Badge(String text) {
        setText(text);
    }

    /**
     * Creates a badge with an icon inside.
     *
     * @param icon
     *            the icon inside the badge
     * @see #setIcon(Component)
     */
    public Badge(Component icon) {
        setIcon(icon);
    }

    /**
     * Creates a badge with a text and an icon inside.
     *
     * @param text
     *            the text inside the badge
     * @param icon
     *            the icon inside the badge
     * @see #setText(String)
     * @see #setIcon(Component)
     */
    public Badge(String text, Component icon) {
        setText(text);
        setIcon(icon);
    }

    /**
     * Creates a badge with a text and a number inside.
     *
     * @param text
     *            the text inside the badge
     * @param number
     *            the number to display in the badge
     * @see #setText(String)
     * @see #setNumber(Integer)
     */
    public Badge(String text, int number) {
        setText(text);
        setNumber(number);
    }

    /**
     * Creates a badge with a text, a number, and an icon inside.
     *
     * @param text
     *            the text inside the badge
     * @param number
     *            the number to display in the badge
     * @param icon
     *            the icon inside the badge
     * @see #setText(String)
     * @see #setNumber(Integer)
     * @see #setIcon(Component)
     */
    public Badge(String text, int number, Component icon) {
        setText(text);
        setNumber(number);
        setIcon(icon);
    }

    /**
     * Creates a badge with a text signal bound to it.
     *
     * @param textSignal
     *            the signal providing the text content
     * @see #bindText(Signal)
     */
    public Badge(Signal<String> textSignal) {
        this();
        bindText(textSignal);
    }

    /**
     * Creates a badge with a text signal and an icon inside.
     *
     * @param textSignal
     *            the signal providing the text content
     * @param icon
     *            the icon inside the badge
     * @see #bindText(Signal)
     * @see #setIcon(Component)
     */
    public Badge(Signal<String> textSignal, Component icon) {
        this();
        setIcon(icon);
        bindText(textSignal);
    }

    /**
     * Sets the given string as the text content of this component.
     * <p>
     * This method removes any existing content in the default slot and replaces
     * it with the given text. Other slotted children (such as icons) are
     * preserved.
     *
     * @param text
     *            the text content to set, or {@code null} to remove existing
     *            text
     */
    @Override
    public void setText(String text) {
        updateContent(null);
        textSignalSupport.set(text);
    }

    /**
     * Gets the text content of this component.
     *
     * @return the text content, or an empty string if not set
     */
    @Override
    public String getText() {
        return textSignalSupport.get();
    }

    @Override
    public void bindText(Signal<String> textSignal) {
        updateContent(null);
        textSignalSupport.bind(textSignal);
    }

    /**
     * Sets the number to display in the badge.
     *
     * @param number
     *            the number to display, or {@code null} to clear it
     */
    public void setNumber(Integer number) {
        if (number == null) {
            getElement().removeProperty("number");
        } else {
            getElement().setProperty("number", number.intValue());
        }
    }

    /**
     * Binds the number property to the given signal.
     *
     * @param numberSignal
     *            the signal providing the number value
     */
    public void bindNumber(Signal<Integer> numberSignal) {
        getElement().bindProperty("number", numberSignal, null);
    }

    /**
     * Gets the number displayed in the badge.
     *
     * @return the number, or {@code null} if not set
     */
    public Integer getNumber() {
        return Optional.ofNullable(getElement().getProperty("number"))
                .map(Integer::valueOf).orElse(null);
    }

    /**
     * Sets the given component as the content of this badge.
     * <p>
     * This method removes any existing content in the default slot and replaces
     * it with the given component. Other slotted children (such as icons) are
     * preserved.
     *
     * @param content
     *            the content component, or {@code null} to remove it
     */
    public void setContent(Component content) {
        textSignalSupport.set(null);
        updateContent(content);
    }

    /**
     * Gets the component in the default slot of this badge.
     *
     * @return the content component, or {@code null} if not set
     */
    public Component getContent() {
        return getChildren()
                .filter(component -> !component.equals(textNode)
                        && !component.getElement().hasAttribute("slot"))
                .findAny().orElse(null);
    }

    /**
     * Sets the given component as the icon of this badge.
     * <p>
     * The icon is placed in the {@code icon} slot of the badge.
     *
     * @param icon
     *            component to be used as an icon, or {@code null} to remove it
     */
    public void setIcon(Component icon) {
        if (icon == null) {
            SlotUtils.clearSlot(this, ICON_SLOT);
        } else {
            SlotUtils.setSlot(this, ICON_SLOT, icon);
        }
    }

    /**
     * Gets the component that is defined as the icon of this badge.
     *
     * @return the icon of this badge, or {@code null} if the icon is not set
     */
    public Component getIcon() {
        return SlotUtils.getChildInSlot(this, ICON_SLOT);
    }

    /**
     * Sets the ARIA role attribute on the badge.
     *
     * @param role
     *            the ARIA role, or {@code null} to clear
     */
    public void setRole(String role) {
        if (role == null) {
            getElement().removeAttribute("role");
        } else {
            getElement().setAttribute("role", role);
        }
    }

    /**
     * Gets the ARIA role attribute of the badge.
     *
     * @return the ARIA role, or {@code null} if not set
     */
    public String getRole() {
        return getElement().getAttribute("role");
    }

    private void updateText(String text) {
        textNode.setText(text);

        if (text == null || text.isEmpty()) {
            textNode.removeFromParent();
            return;
        }

        if (textNode.getParent().isEmpty()) {
            getElement().appendChild(textNode.getElement());
        }
    }

    private void updateContent(Component content) {
        var oldContent = getContent();
        if (oldContent == content) {
            return;
        }
        if (oldContent != null) {
            getElement().removeChild(oldContent.getElement());
        }
        if (content != null) {
            getElement().appendChild(content.getElement());
        }
    }
}
