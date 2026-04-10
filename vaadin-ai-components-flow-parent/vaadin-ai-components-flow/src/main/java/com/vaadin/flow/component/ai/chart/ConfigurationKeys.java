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
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;

/**
 * Highcharts configuration property key constants used by
 * {@link ChartConfigurationParser} and referenced in the
 * {@link ChartAITools#updateChartConfiguration} tool parameter schema.
 * <p>
 * These match the Highcharts options object property names.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class ConfigurationKeys implements Serializable {

    private ConfigurationKeys() {
    }

    // --- Top-level configuration sections ---

    public static final String CHART = "chart";
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String TOOLTIP = "tooltip";
    public static final String LEGEND = "legend";
    public static final String X_AXIS = "xAxis";
    public static final String Y_AXIS = "yAxis";
    public static final String Z_AXIS = "zAxis";
    public static final String COLOR_AXIS = "colorAxis";
    public static final String CREDITS = "credits";
    public static final String PANE = "pane";
    public static final String PLOT_OPTIONS = "plotOptions";
    public static final String SERIES = "series";

    // --- Common properties ---

    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TEXT = "text";
    public static final String ENABLED = "enabled";
    public static final String MIN = "min";
    public static final String MAX = "max";

    // --- Chart model ---

    public static final String BACKGROUND_COLOR = "backgroundColor";
    public static final String BORDER_COLOR = "borderColor";
    public static final String BORDER_WIDTH = "borderWidth";
    public static final String BORDER_RADIUS = "borderRadius";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String MARGIN_TOP = "marginTop";
    public static final String MARGIN_RIGHT = "marginRight";
    public static final String MARGIN_BOTTOM = "marginBottom";
    public static final String MARGIN_LEFT = "marginLeft";
    public static final String SPACING_TOP = "spacingTop";
    public static final String SPACING_RIGHT = "spacingRight";
    public static final String SPACING_BOTTOM = "spacingBottom";
    public static final String SPACING_LEFT = "spacingLeft";
    public static final String PLOT_BACKGROUND_COLOR = "plotBackgroundColor";
    public static final String PLOT_BORDER_COLOR = "plotBorderColor";
    public static final String PLOT_BORDER_WIDTH = "plotBorderWidth";
    public static final String INVERTED = "inverted";
    public static final String POLAR = "polar";
    public static final String ANIMATION = "animation";
    public static final String STYLED_MODE = "styledMode";
    public static final String ZOOM_TYPE = "zoomType";
    public static final String OPTIONS_3D = "options3d";
    public static final String ALPHA = "alpha";
    public static final String BETA = "beta";
    public static final String DEPTH = "depth";
    public static final String VIEW_DISTANCE = "viewDistance";
    public static final String FRAME = "frame";
    public static final String BACK = "back";
    public static final String BOTTOM = "bottom";
    public static final String SIDE = "side";
    public static final String TOP = "top";
    public static final String COLOR = "color";

    // --- Tooltip ---

    public static final String POINT_FORMAT = "pointFormat";
    public static final String HEADER_FORMAT = "headerFormat";
    public static final String SHARED = "shared";
    public static final String VALUE_SUFFIX = "valueSuffix";
    public static final String VALUE_PREFIX = "valuePrefix";

    // --- Legend ---

    public static final String ALIGN = "align";
    public static final String VERTICAL_ALIGN = "verticalAlign";
    public static final String LAYOUT = "layout";

    // --- Axis ---

    public static final String CATEGORIES = "categories";
    public static final String OPPOSITE = "opposite";

    // --- Color axis ---

    public static final String MIN_COLOR = "minColor";
    public static final String MAX_COLOR = "maxColor";

    // --- Credits ---

    public static final String HREF = "href";

    // --- Pane ---

    public static final String START_ANGLE = "startAngle";
    public static final String END_ANGLE = "endAngle";
    public static final String CENTER = "center";
    public static final String SIZE = "size";
}
