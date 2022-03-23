package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Dash styles used to render lines.
 * <p>
 * Visually they look like this:<br>
 * <img src=
 * "data:image/png;base64,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"
 * />
 *
 */
public enum DashStyle implements ChartEnum {

    SOLID("Solid"), SHORTDASH("ShortDash"), SHORTDOT("ShortDot"), SHORTDASHDOT(
            "ShortDashDot"), SHORTDASHDOTDOT("ShortDashDotDot"), DOT(
                    "Dot"), DASH("Dash"), LONGDASH("LongDash"), DASHDOT(
                            "DashDot"), LONGDASHDOT(
                                    "LongDashDot"), LONGDASHDOTDOT(
                                            "LongDashDotDot");

    private final String type;

    private DashStyle(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
