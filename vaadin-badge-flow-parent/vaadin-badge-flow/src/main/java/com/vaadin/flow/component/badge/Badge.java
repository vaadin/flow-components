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
package com.vaadin.flow.component.badge;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;

/**
 * Badge is a colored text element for labeling content, displaying metadata,
 * or highlighting information. Note, currently only Lumo theme supports badges.
 * <p>
 * Badges are typically used to show statuses, categories, or counts. They
 * support text content, icons, and various theme variants for different colors
 * and sizes.
 * </p>
 *
 * <h3>Usage Examples</h3>
 *
 * <pre>
 * {@code
 * // Simple badge with text
 * Badge badge = new Badge("New");
 *
 * // Badge with theme variants
 * Badge successBadge = new Badge("Success");
 * successBadge.addThemeVariants(BadgeVariant.LUMO_SUCCESS);
 *
 * // Small pill-shaped badge
 * Badge pillBadge = new Badge("3");
 * pillBadge.addThemeVariants(BadgeVariant.LUMO_SMALL, BadgeVariant.LUMO_PILL);
 *
 * // Badge with icon
 * Badge iconBadge = new Badge();
 * iconBadge.add(new Icon(VaadinIcon.CHECK), new Span("Verified"));
 * }
 * </pre>
 *
 * <h3>Theme Variants</h3>
 * <p>
 * The component supports several theme variants for styling:
 * </p>
 * <ul>
 * <li>{@link BadgeVariant#LUMO_PRIMARY} - Primary color</li>
 * <li>{@link BadgeVariant#LUMO_SUCCESS} - Success/positive color</li>
 * <li>{@link BadgeVariant#LUMO_WARNING} - Warning color</li>
 * <li>{@link BadgeVariant#LUMO_ERROR} - Error/danger color</li>
 * <li>{@link BadgeVariant#LUMO_CONTRAST} - High contrast color</li>
 * <li>{@link BadgeVariant#LUMO_SMALL} - Smaller size</li>
 * <li>{@link BadgeVariant#LUMO_PILL} - Pill shape with rounded corners</li>
 * </ul>
 *
 * <h3>Accessibility</h3>
 * <p>
 * When using icon-only badges or when the badge's visual appearance alone
 * doesn't convey the full meaning, use {@link #setAriaLabel(String)} to
 * provide a text alternative for screen readers. Additionally, consider using
 * {@link #setTooltipText(String)} to provide helpful information to all users.
 * </p>
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-badge")
public class Badge extends Component implements HasText, HasSize, HasStyle,
        HasComponents, HasThemeVariant<BadgeVariant>, HasTooltip, HasAriaLabel {

    /**
     * Creates an empty badge.
     * <p>
     * Use {@link #setText(String)} to set the badge text or {@link #add(Component...)}
     * to add icons or other components.
     * </p>
     */
    public Badge() {
        getElement().getThemeList().add("badge");
    }

    /**
     * Creates a badge with the specified text.
     *
     * @param text
     *            the text content of the badge
     */
    public Badge(String text) {
        this();
        setText(text);
    }
}
