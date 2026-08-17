/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.util.ArrayList;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.ConfidenceLevel;
import com.vaadin.flow.component.ai.common.PageRegion;
import com.vaadin.flow.component.ai.common.Rect;
import com.vaadin.flow.component.ai.common.SourceExtract;
import com.vaadin.flow.component.ai.common.SourceLocation;
import com.vaadin.flow.component.ai.common.ValueSource;

import tools.jackson.databind.JsonNode;

/**
 * Parses the source-tracking envelope a {@code fill_form} value may arrive in
 * when source tracking is on:
 *
 * <pre>
 * {"value": ..., "confidence": "high", "extracts": [
 *   {"text": "...", "location": {"type": "page-region", "page": 2,
 *    "rect": [0.12, 0.34, 0.25, 0.04]}}]}
 * </pre>
 *
 * Parsing is best effort: source data exists to help review a fill, so it must
 * never block one. A malformed confidence level, extract, or location is
 * dropped and logged while the value is still written. Only a missing
 * {@code value} key rejects the write, which the caller handles by never
 * unwrapping such an object as an envelope.
 *
 * @author Vaadin Ltd
 */
final class ValueSourceParser {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ValueSourceParser.class);

    private static final String PAGE_REGION_TYPE = "page-region";

    private ValueSourceParser() {
    }

    /**
     * Whether the given {@code fill_form} value is a source-tracking envelope —
     * a JSON object carrying the required {@code value} key. Only called when
     * source tracking is on; plain values and every other shape pass through
     * the regular conversion untouched.
     */
    static boolean isEnvelope(JsonNode value) {
        return value != null && value.isObject() && value.has("value");
    }

    /**
     * Returns the plain value wrapped inside the envelope. Call only when
     * {@link #isEnvelope} returned {@code true}.
     */
    static JsonNode unwrapValue(JsonNode envelope) {
        return envelope.get("value");
    }

    /**
     * Extracts the source data from the envelope. Bad parts are dropped and
     * logged rather than failing the parse.
     *
     * @param envelope
     *            the envelope object, with {@link #isEnvelope} already checked
     * @param fieldId
     *            the target field's id, used for log context only
     * @return the reported source, or {@code null} when the envelope carries no
     *         usable source data
     */
    static ValueSource parse(JsonNode envelope, String fieldId) {
        var confidence = parseConfidence(envelope.get("confidence"), fieldId);
        var extracts = parseExtracts(envelope.get("extracts"), fieldId);
        if (confidence == null && extracts.isEmpty()) {
            return null;
        }
        return new ValueSource(confidence, extracts);
    }

    private static ConfidenceLevel parseConfidence(JsonNode node,
            String fieldId) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isString()) {
            try {
                return ConfidenceLevel
                        .valueOf(node.asString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                LOGGER.debug(
                        "Dropping unknown confidence level '{}' "
                                + "reported for field {}",
                        node.asString(), fieldId);
                return null;
            }
        }
        LOGGER.debug("Dropping non-string confidence level reported for "
                + "field {}", fieldId);
        return null;
    }

    private static ArrayList<SourceExtract> parseExtracts(JsonNode node,
            String fieldId) {
        var extracts = new ArrayList<SourceExtract>();
        if (node == null || node.isNull()) {
            return extracts;
        }
        if (!node.isArray()) {
            LOGGER.debug("Dropping non-array extracts reported for field {}",
                    fieldId);
            return extracts;
        }
        for (var extract : node) {
            if (!extract.isObject() || !extract.path("text").isString()) {
                LOGGER.debug("Dropping extract without text reported for "
                        + "field {}", fieldId);
                continue;
            }
            var location = parseLocation(extract.get("location"), fieldId);
            extracts.add(new SourceExtract(extract.get("text").asString(),
                    location));
        }
        return extracts;
    }

    /**
     * Parses one extract's location block. Returns {@code null} — an extract
     * with no position, which is a valid state — for a missing block, an
     * unknown location type, or a malformed region, so a bad location never
     * drops the extract's text.
     */
    private static SourceLocation parseLocation(JsonNode node, String fieldId) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            LOGGER.debug("Dropping malformed location reported for field {}",
                    fieldId);
            return null;
        }
        var type = node.path("type").asString(null);
        if (!PAGE_REGION_TYPE.equals(type)) {
            LOGGER.debug("Dropping location of unknown type '{}' reported "
                    + "for field {}", type, fieldId);
            return null;
        }
        var rect = parseRect(node.get("rect"), fieldId);
        if (rect == null) {
            return null;
        }
        var page = parsePage(node.get("page"), fieldId);
        if (page == null) {
            return null;
        }
        return new PageRegion(page, rect);
    }

    /**
     * Parses the 1-based page number. A missing page means the source has a
     * single surface, such as an image, and becomes {@code 1}, so a
     * {@link PageRegion} always carries a page number. A page that is present
     * but invalid drops the whole location — a region on an unknown page would
     * point the reviewer at the wrong place.
     */
    private static Integer parsePage(JsonNode node, String fieldId) {
        if (node == null || node.isNull()) {
            return 1;
        }
        if (node.isIntegralNumber() && node.asInt() >= 1) {
            return node.asInt();
        }
        LOGGER.debug("Dropping location with invalid page number reported "
                + "for field {}", fieldId);
        return null;
    }

    /**
     * Parses the {@code [x, y, width, height]} rectangle, all fractions of the
     * page. Out-of-range numbers and rectangles with no size are dropped, per
     * the best-effort rule.
     */
    private static Rect parseRect(JsonNode node, String fieldId) {
        if (node == null || !node.isArray() || node.size() != 4
                || !allNumbers(node)) {
            LOGGER.debug("Dropping malformed rect reported for field {}",
                    fieldId);
            return null;
        }
        var x = node.get(0).asDouble();
        var y = node.get(1).asDouble();
        var width = node.get(2).asDouble();
        var height = node.get(3).asDouble();
        var inRange = x >= 0 && x <= 1 && y >= 0 && y <= 1 && width > 0
                && width <= 1 && height > 0 && height <= 1;
        if (!inRange) {
            LOGGER.debug("Dropping out-of-range rect reported for field {}",
                    fieldId);
            return null;
        }
        return new Rect(x, y, width, height);
    }

    private static boolean allNumbers(JsonNode array) {
        for (var element : array) {
            if (!element.isNumber()) {
                return false;
            }
        }
        return true;
    }
}
