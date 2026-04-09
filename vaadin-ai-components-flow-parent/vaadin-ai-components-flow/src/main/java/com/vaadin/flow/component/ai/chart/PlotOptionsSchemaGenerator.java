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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.PlotOptionsArearange;
import com.vaadin.flow.component.charts.model.PlotOptionsAreaspline;
import com.vaadin.flow.component.charts.model.PlotOptionsAreasplinerange;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsBoxplot;
import com.vaadin.flow.component.charts.model.PlotOptionsBubble;
import com.vaadin.flow.component.charts.model.PlotOptionsBullet;
import com.vaadin.flow.component.charts.model.PlotOptionsCandlestick;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsColumnrange;
import com.vaadin.flow.component.charts.model.PlotOptionsErrorbar;
import com.vaadin.flow.component.charts.model.PlotOptionsFlags;
import com.vaadin.flow.component.charts.model.PlotOptionsFunnel;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.PlotOptionsGauge;
import com.vaadin.flow.component.charts.model.PlotOptionsHeatmap;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsOhlc;
import com.vaadin.flow.component.charts.model.PlotOptionsOrganization;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsPolygon;
import com.vaadin.flow.component.charts.model.PlotOptionsPyramid;
import com.vaadin.flow.component.charts.model.PlotOptionsSankey;
import com.vaadin.flow.component.charts.model.PlotOptionsScatter;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSolidgauge;
import com.vaadin.flow.component.charts.model.PlotOptionsSpline;
import com.vaadin.flow.component.charts.model.PlotOptionsTimeline;
import com.vaadin.flow.component.charts.model.PlotOptionsTreemap;
import com.vaadin.flow.component.charts.model.PlotOptionsWaterfall;
import com.vaadin.flow.component.charts.model.PlotOptionsXrange;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * Build-time generator that produces complete JSON schemas for each chart
 * type's plot options, including property descriptions parsed from JavaDoc.
 * <p>
 * Invoked automatically during the Maven build via {@code exec-maven-plugin}.
 * The output is consumed by {@link PlotOptionsSchema} at runtime.
 */
public final class PlotOptionsSchemaGenerator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PlotOptionsSchemaGenerator.class);

    private static final int MAX_DEPTH = 1;

    /**
     * Fields that are internal, technical, or accessibility-related and not
     * useful for natural-language chart customization. Excluded from the
     * generated schema to reduce noise for the LLM.
     */
    private static final Set<String> EXCLUDED_FIELDS = Set.of(
            // Accessibility
            "description", "exposeElementToA11y", "skipKeyboardNavigation",
            // Performance internals
            "animationLimit", "cropThreshold", "turboThreshold",
            // Developer / styled-mode only
            "className", "colorIndex", "colorKey",
            // Rendering internals
            "clip", "crisp", "linecap", "boostBlending",
            // Event tracking
            "enableMouseTracking", "stickyTracking", "findNearestPointBy",
            "trackByArea",
            // Internal data mapping
            "keys", "compare", "compareBase", "gapSize", "gapUnit",
            // Series linking / legend internals
            "linkedTo", "legendIndex", "showInNavigator", "legendType",
            // Axis / point calculation internals
            "getExtremesFromAll", "pointRange", "pointStart", "pointInterval",
            "pointIntervalUnit", "pointPlacement", "softThreshold", "zoneAxis",
            // Selection internals
            "selected", "showCheckbox",
            // Null / gap handling
            "connectNulls", "connectEnds",
            // Treemap internals
            "levelIsConstant", "interactByLeaf", "layoutAlgorithm",
            "layoutStartingDirection", "alternateStartingDirection",
            "sortIndex",
            // Export / rendering
            "includeInDataExport", "useHTML",
            // Other internals
            "allowDrillToNode", "allowTraversingTree", "inactiveOtherPoints");

    private static final Set<Class<?>> EXPANDABLE_TYPES = Set.of(
            findClass("com.vaadin.flow.component.charts.model.DataLabels"),
            findClass("com.vaadin.flow.component.charts.model.Marker"),
            findClass("com.vaadin.flow.component.charts.model.SeriesTooltip"));

    // Matches the JavaDoc block immediately preceding a setter.
    // Uses (?:[^*]+|\*(?!/))* to stay within a single /** ... */ block.
    private static final Pattern SETTER_JAVADOC_PATTERN = Pattern.compile( // NOSONAR
            "/\\*\\*((?:[^*]+|\\*(?!/))*+)\\*/\\s*public\\s+(?:abstract\\s+)?void\\s+set(\\w+)\\s*\\(");

    private PlotOptionsSchemaGenerator() {
    }

    /**
     * Entry point for the build-time generation.
     *
     * @param args
     *            {@code args[0]} = chart model source directory,
     *            {@code args[1]} = output file path
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            LOGGER.error(
                    "Usage: PlotOptionsSchemaGenerator <sourceDir> <outputFile>");
            System.exit(1);
        }

        Path sourceDir = Path.of(args[0]);
        Path outputFile = Path.of(args[1]);

        if (!Files.isDirectory(sourceDir)) {
            LOGGER.warn("Source directory not found: {}"
                    + " — skipping schema generation", sourceDir);
            return;
        }

        // Parse descriptions from source files
        Map<String, Map<String, String>> descriptions = parseDescriptions(
                sourceDir);

        // Build complete schemas for each chart type
        var plotOptionsByType = buildTypeMap();
        var schemas = JacksonUtils.createObjectNode();

        for (var entry : plotOptionsByType.entrySet()) {
            var schema = buildObjectSchema(entry.getValue(), 0, descriptions);
            schemas.set(entry.getKey(), schema);
        }

        Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, schemas.toString());
    }

    // ── Schema building (reflection-based) ──────────────────────────────

    private static ObjectNode buildObjectSchema(Class<?> clazz, int depth,
            Map<String, Map<String, String>> descriptions) {
        var schema = JacksonUtils.createObjectNode();
        schema.put("type", "object");
        var properties = schema.putObject("properties");

        for (var field : collectFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            var name = field.getName();
            if (name.startsWith("_fn_") || EXCLUDED_FIELDS.contains(name)) {
                continue;
            }
            var fieldSchema = fieldToSchema(field, depth, descriptions);
            if (fieldSchema != null) {
                addDescription(fieldSchema, field, descriptions);
                properties.set(name, fieldSchema);
            }
        }
        return schema;
    }

    private static ObjectNode fieldToSchema(Field field, int depth,
            Map<String, Map<String, String>> descriptions) {
        var type = field.getType();

        if (type == Boolean.class || type == boolean.class) {
            return typeNode("boolean");
        }
        if (Number.class.isAssignableFrom(type) || type == int.class
                || type == long.class || type == double.class
                || type == float.class) {
            return typeNode("number");
        }
        if (type == String.class) {
            return typeNode("string");
        }
        if (Color.class.isAssignableFrom(type)) {
            var node = typeNode("string");
            node.put("description", "CSS color (e.g. '#ff0000')");
            return node;
        }
        if (type.isEnum()) {
            return enumSchema(type);
        }
        if (type == ArrayList.class || type == List.class) {
            return arraySchema(field);
        }
        if (depth < MAX_DEPTH && EXPANDABLE_TYPES.contains(type)) {
            return buildObjectSchema(type, depth + 1, descriptions);
        }
        // Skip non-expandable complex objects — an opaque "type":"object"
        // gives the LLM no useful information about valid keys.
        return null;
    }

    private static void addDescription(ObjectNode schema, Field field,
            Map<String, Map<String, String>> descriptions) {
        if (schema.has("description")) {
            return;
        }
        var classDescriptions = descriptions
                .get(field.getDeclaringClass().getSimpleName());
        if (classDescriptions != null) {
            var description = classDescriptions.get(field.getName());
            if (description != null) {
                schema.put("description", description);
            }
        }
    }

    private static ObjectNode enumSchema(Class<?> enumClass) {
        var node = typeNode("string");
        var values = node.putArray("enum");
        for (var constant : enumClass.getEnumConstants()) {
            var value = constant.toString();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return node;
    }

    private static ObjectNode arraySchema(Field field) {
        var node = typeNode("array");
        var genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType pt
                && pt.getActualTypeArguments().length == 1) {
            var itemType = pt.getActualTypeArguments()[0];
            if (itemType == String.class) {
                node.set("items", typeNode("string"));
            } else if (itemType instanceof Class<?> itemClass
                    && Color.class.isAssignableFrom(itemClass)) {
                var itemNode = typeNode("string");
                itemNode.put("description", "CSS color");
                node.set("items", itemNode);
            } else {
                // Skip arrays of complex objects — without an items schema
                // the LLM cannot know what to put in the array.
                return null;
            }
        }
        return node;
    }

    private static ObjectNode typeNode(String type) {
        var node = JacksonUtils.createObjectNode();
        node.put("type", type);
        return node;
    }

    private static List<Field> collectFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        var current = clazz;
        while (current != null && current != AbstractPlotOptions.class
                && current != AbstractConfigurationObject.class
                && current != Object.class) {
            for (var field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    // ── Description parsing (source-based) ──────────────────────────────

    private static Map<String, Map<String, String>> parseDescriptions(
            Path sourceDir) throws IOException {
        Set<Class<?>> classesToParse = collectClassesToParse();
        Map<String, Map<String, String>> allDescriptions = new HashMap<>();

        for (Class<?> clazz : classesToParse) {
            Path sourceFile = sourceDir
                    .resolve(clazz.getSimpleName() + ".java");
            if (!Files.exists(sourceFile)) {
                continue;
            }

            String source = Files.readString(sourceFile);
            Map<String, String> descriptions = extractDescriptions(source);
            if (!descriptions.isEmpty()) {
                allDescriptions.put(clazz.getSimpleName(), descriptions);
            }
        }

        return allDescriptions;
    }

    private static Set<Class<?>> collectClassesToParse() {
        var plotOptionsByType = buildTypeMap();
        Set<Class<?>> classes = new LinkedHashSet<>();

        for (Class<?> clazz : plotOptionsByType.values()) {
            Class<?> current = clazz;
            while (current != null && current != AbstractPlotOptions.class
                    && current != AbstractConfigurationObject.class
                    && current != Object.class) {
                classes.add(current);
                current = current.getSuperclass();
            }
        }

        for (Class<?> clazz : classes.toArray(Class<?>[]::new)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Class<?> fieldType = field.getType();
                if (AbstractConfigurationObject.class
                        .isAssignableFrom(fieldType)
                        && fieldType != AbstractConfigurationObject.class) {
                    classes.add(fieldType);
                }
            }
        }

        return classes;
    }

    static Map<String, String> extractDescriptions(String source) {
        Map<String, String> descriptions = new LinkedHashMap<>();
        Matcher matcher = SETTER_JAVADOC_PATTERN.matcher(source);
        while (matcher.find()) {
            String javadoc = matcher.group(1);
            String setterSuffix = matcher.group(2);
            String fieldName = Character.toLowerCase(setterSuffix.charAt(0))
                    + setterSuffix.substring(1);
            String description = cleanJavadoc(javadoc);
            if (!description.isEmpty()) {
                descriptions.put(fieldName, description);
            }
        }
        return descriptions;
    }

    static String cleanJavadoc(String javadoc) {
        // Remove leading * from each line
        String cleaned = javadoc.replaceAll("(?m)^\\s*\\*\\s?", " ");
        // Remove @see, @param, @return tags
        cleaned = cleaned.replaceAll("@see\\s+[^\\s]+", "");
        cleaned = cleaned.replaceAll("@param\\s+\\w+\\s*", "");
        cleaned = cleaned.replaceAll("@return\\s*", "");
        // Replace {@link ClassName#method} and {@code text} keeping the text
        cleaned = cleaned.replaceAll("\\{@\\w+\\s+(?:[^#}]*#)?+([^}]*)\\}", // NOSONAR
                "$1");
        // Remove HTML tags
        cleaned = cleaned.replaceAll("<[^>]+>", "");
        // Normalize whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        // Remove "Defaults to: ..." suffix
        int defaultsIdx = cleaned.indexOf("Defaults to:");
        if (defaultsIdx >= 0) {
            cleaned = cleaned.substring(0, defaultsIdx).trim();
        }
        return cleaned;
    }

    // ── Type mapping ────────────────────────────────────────────────────

    static Map<String, Class<? extends AbstractPlotOptions>> buildTypeMap() {
        var map = new HashMap<String, Class<? extends AbstractPlotOptions>>();
        for (var supplier : plotOptionsSuppliers()) {
            var instance = supplier.get();
            map.put(instance.getChartType().toString(), instance.getClass());
        }
        map.put("series", PlotOptionsSeries.class);
        return Map.copyOf(map);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<AbstractPlotOptions>[] plotOptionsSuppliers() {
        return new Supplier[] { PlotOptionsArea::new, PlotOptionsArearange::new,
                PlotOptionsAreaspline::new, PlotOptionsAreasplinerange::new,
                PlotOptionsBar::new, PlotOptionsBoxplot::new,
                PlotOptionsBubble::new, PlotOptionsBullet::new,
                PlotOptionsCandlestick::new, PlotOptionsColumn::new,
                PlotOptionsColumnrange::new, PlotOptionsErrorbar::new,
                PlotOptionsFlags::new, PlotOptionsFunnel::new,
                PlotOptionsGantt::new, PlotOptionsGauge::new,
                PlotOptionsHeatmap::new, PlotOptionsLine::new,
                PlotOptionsOhlc::new, PlotOptionsOrganization::new,
                PlotOptionsPie::new, PlotOptionsPolygon::new,
                PlotOptionsPyramid::new, PlotOptionsSankey::new,
                PlotOptionsScatter::new, PlotOptionsSolidgauge::new,
                PlotOptionsSpline::new, PlotOptionsTimeline::new,
                PlotOptionsTreemap::new, PlotOptionsWaterfall::new,
                PlotOptionsXrange::new };
    }

    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
