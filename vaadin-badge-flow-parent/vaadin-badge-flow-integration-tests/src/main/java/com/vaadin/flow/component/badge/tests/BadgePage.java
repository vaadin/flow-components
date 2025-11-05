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
package com.vaadin.flow.component.badge.tests;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Comprehensive visual test page for the Badge component showing all variants,
 * sizes, colors, and use cases.
 */
@Route("vaadin-badge")
public class BadgePage extends Div {

    public BadgePage() {
        setWidthFull();
        getStyle().set("padding", "20px");

        add(new H3("Badge Component - Visual Test Page"));

        // Basic badges
        add(new H3("Basic Badges"));
        Div basicSection = createSection();
        basicSection.add(new Badge("Default"));
        basicSection.add(new Badge("New"));
        basicSection.add(new Badge("Beta"));
        basicSection.add(new Badge("123"));
        add(basicSection);

        add(new Hr());

        // Color variants
        add(new H3("Color Variants"));
        Div colorSection = createSection();

        Badge primary = new Badge("Primary");
        primary.addThemeVariants(BadgeVariant.LUMO_PRIMARY);
        primary.setId("badge-primary");
        colorSection.add(primary);

        Badge success = new Badge("Success");
        success.addThemeVariants(BadgeVariant.LUMO_SUCCESS);
        success.setId("badge-success");
        colorSection.add(success);

        Badge warning = new Badge("Warning");
        warning.addThemeVariants(BadgeVariant.LUMO_WARNING);
        warning.setId("badge-warning");
        colorSection.add(warning);

        Badge error = new Badge("Error");
        error.addThemeVariants(BadgeVariant.LUMO_ERROR);
        error.setId("badge-error");
        colorSection.add(error);

        Badge contrast = new Badge("Contrast");
        contrast.addThemeVariants(BadgeVariant.LUMO_CONTRAST);
        contrast.setId("badge-contrast");
        colorSection.add(contrast);

        add(colorSection);

        add(new Hr());

        // Small size variant
        add(new H3("Small Size"));
        Div smallSection = createSection();

        Badge smallDefault = new Badge("Small");
        smallDefault.addThemeVariants(BadgeVariant.LUMO_SMALL);
        smallDefault.setId("badge-small");
        smallSection.add(smallDefault);

        Badge smallPrimary = new Badge("Small Primary");
        smallPrimary.addThemeVariants(BadgeVariant.LUMO_SMALL,
                BadgeVariant.LUMO_PRIMARY);
        smallSection.add(smallPrimary);

        Badge smallSuccess = new Badge("Small Success");
        smallSuccess.addThemeVariants(BadgeVariant.LUMO_SMALL,
                BadgeVariant.LUMO_SUCCESS);
        smallSection.add(smallSuccess);

        add(smallSection);

        add(new Hr());

        // Pill shape variant
        add(new H3("Pill Shape"));
        Div pillSection = createSection();

        Badge pill = new Badge("Pill");
        pill.addThemeVariants(BadgeVariant.LUMO_PILL);
        pill.setId("badge-pill");
        pillSection.add(pill);

        Badge pillPrimary = new Badge("99+");
        pillPrimary.addThemeVariants(BadgeVariant.LUMO_PILL,
                BadgeVariant.LUMO_PRIMARY);
        pillSection.add(pillPrimary);

        Badge pillSmall = new Badge("3");
        pillSmall.addThemeVariants(BadgeVariant.LUMO_PILL,
                BadgeVariant.LUMO_SMALL, BadgeVariant.LUMO_ERROR);
        pillSection.add(pillSmall);

        add(pillSection);

        add(new Hr());

        // Badges with components
        add(new H3("Badges with Components"));
        Div componentSection = createSection();

        Badge withSpan = new Badge();
        withSpan.add(new Span("Status: "), new Span("Active"));
        withSpan.addThemeVariants(BadgeVariant.LUMO_SUCCESS);
        withSpan.setId("badge-with-components");
        componentSection.add(withSpan);

        Badge multiComponent = new Badge();
        multiComponent.add(new Span("Count: "), new Span("42"));
        multiComponent.addThemeVariants(BadgeVariant.LUMO_PRIMARY);
        componentSection.add(multiComponent);

        add(componentSection);

        add(new Hr());

        // Accessibility examples
        add(new H3("Accessibility Features"));
        Div accessibilitySection = createSection();

        Badge withAriaLabel = new Badge("!");
        withAriaLabel.setAriaLabel("Important notification");
        withAriaLabel.addThemeVariants(BadgeVariant.LUMO_ERROR,
                BadgeVariant.LUMO_PILL);
        withAriaLabel.setId("badge-aria-label");
        accessibilitySection.add(withAriaLabel);

        Badge withTooltip = new Badge("?");
        withTooltip.setTooltipText("This is a help badge");
        withTooltip.addThemeVariants(BadgeVariant.LUMO_CONTRAST,
                BadgeVariant.LUMO_PILL);
        withTooltip.setId("badge-tooltip");
        accessibilitySection.add(withTooltip);

        add(accessibilitySection);

        add(new Hr());

        // Real-world use cases
        add(new H3("Real-world Use Cases"));
        Div useCasesSection = createSection();

        // Notification badge
        Span bellIcon = new Span("🔔");
        Badge notificationBadge = new Badge("5");
        notificationBadge.addThemeVariants(BadgeVariant.LUMO_ERROR,
                BadgeVariant.LUMO_SMALL, BadgeVariant.LUMO_PILL);
        Div notificationExample = new Div(bellIcon, notificationBadge);
        notificationExample.getStyle().set("display", "inline-flex")
                .set("align-items", "center").set("gap", "5px");
        useCasesSection.add(notificationExample);

        // Status badge
        Badge statusBadge = new Badge("Online");
        statusBadge.addThemeVariants(BadgeVariant.LUMO_SUCCESS,
                BadgeVariant.LUMO_SMALL);
        useCasesSection.add(statusBadge);

        // Count badge
        Badge countBadge = new Badge("NEW");
        countBadge.addThemeVariants(BadgeVariant.LUMO_PRIMARY);
        useCasesSection.add(countBadge);

        add(useCasesSection);

        add(new Hr());

        // Empty badge (for dynamic content)
        add(new H3("Empty Badge (Dynamic Content)"));
        Badge emptyBadge = new Badge();
        emptyBadge.setId("empty-badge");
        add(emptyBadge);
    }

    private Div createSection() {
        Div section = new Div();
        section.getStyle().set("display", "flex").set("gap", "10px")
                .set("flex-wrap", "wrap").set("align-items", "center")
                .set("padding", "10px 0");
        return section;
    }
}
