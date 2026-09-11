/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import static com.vaadin.flow.component.ai.form.FormTestSupport.findTool;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;
import static com.vaadin.flow.component.ai.form.FormTestSupport.requestEvent;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.AITurnEvents;
import com.vaadin.flow.component.ai.common.ConfidenceLevel;
import com.vaadin.flow.component.ai.common.PageRegion;
import com.vaadin.flow.component.ai.common.SourceExtract;
import com.vaadin.flow.component.ai.common.ValueSource;
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormAIController} source tracking. Per the RFC, with
 * {@link FormAIController#setSourceTrackingEnabled(boolean)} on, a
 * {@code fill_form} value may arrive wrapped in an envelope carrying the
 * snippets the LLM read, their locations, and a confidence level. The reported
 * source is readable from {@link FormAIController#getFieldSource(HasValue)} and
 * {@link FieldValueChangeEvent#getFieldSource()}, lasts as long as the field
 * holds the value it was reported with, and bad source data is dropped without
 * ever blocking the value. Each test drives the {@code fill_form} tool the way
 * the LLM would.
 */
class SourceTrackingTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @BeforeEach
    void clearParserLogger() {
        TestLoggerFactory.getTestLogger(ValueSourceParser.class).clearAll();
    }

    /**
     * The debug events {@link ValueSourceParser} logged in this test. Each
     * dropped part must be logged exactly once — a drop that falls through to a
     * second catch-all message would double-log.
     */
    private static List<String> parserDebugMessages() {
        return TestLoggerFactory.getTestLogger(ValueSourceParser.class)
                .getLoggingEvents().stream()
                .filter(e -> e.getLevel() == Level.DEBUG)
                .map(e -> e.getMessage()).toList();
    }

    @Nested
    class Toggle {

        @Test
        void sourceTrackingIsOffByDefault() {
            var controller = controllerFor(new TestField());

            Assertions.assertFalse(controller.isSourceTrackingEnabled());
        }

        @Test
        void setterAndGetterRoundTrip() {
            var controller = controllerFor(new TestField());

            var returned = controller.setSourceTrackingEnabled(true);

            Assertions.assertSame(controller, returned,
                    "Setter must return the controller for chaining");
            Assertions.assertTrue(controller.isSourceTrackingEnabled());
            controller.setSourceTrackingEnabled(false);
            Assertions.assertFalse(controller.isSourceTrackingEnabled());
        }
    }

    @Nested
    class ToolSchema {

        @Test
        void schemaDeclaresSourcesOnlyWhileTrackingIsOn() {
            var controller = controllerFor(new TestField());
            var untracked = fillFormSchema(controller);
            Assertions.assertTrue(
                    parameters(untracked).path("sources").isMissingNode(),
                    "Untracked schema must not ask for sources, got: "
                            + untracked);

            controller.setSourceTrackingEnabled(true);
            var tracked = fillFormSchema(controller);
            Assertions.assertTrue(
                    parameters(tracked).path("sources").isObject(),
                    "Tracking must ask for sources next to values, got: "
                            + tracked);
            Assertions.assertTrue(
                    parameters(tracked).path("values")
                            .path("additionalProperties").isBoolean(),
                    "Values must stay plain, the source lives in its own "
                            + "map, got: " + tracked);
            Assertions.assertEquals(tracked, fillFormSchema(controller),
                    "Tracked schema must be byte-identical across calls so "
                            + "providers can cache the tool definition");

            controller.setSourceTrackingEnabled(false);
            Assertions.assertEquals(untracked, fillFormSchema(controller),
                    "Toggling tracking off must restore the untracked "
                            + "schema");
        }

        @Test
        void sourcesAreOptionalAndKeyedLikeValues() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);

            var schema = JacksonUtils.readTree(fillFormSchema(controller));
            Assertions.assertEquals(List.of("values"),
                    stringsOf(schema.path("required")),
                    "A fill without sources must stay valid");
            var sources = schema.path("properties").path("sources");
            Assertions.assertEquals("object", sources.path("type").asString());
            Assertions.assertTrue(
                    sources.path("additionalProperties").isObject(),
                    "Sources must be an open map keyed by field id");
        }

        @Test
        void bothSchemasDeclareAnObjectRootWithAnOpenKeyedValuesMap() {
            var controller = controllerFor(new TestField());

            for (var tracking : List.of(false, true)) {
                controller.setSourceTrackingEnabled(tracking);
                var schema = JacksonUtils.readTree(fillFormSchema(controller));
                Assertions.assertEquals("object",
                        schema.path("type").asString(),
                        "Source tracking " + tracking);
                var values = schema.path("properties").path("values");
                Assertions.assertEquals("object",
                        values.path("type").asString(),
                        "Source tracking " + tracking);
                Assertions.assertTrue(
                        values.path("additionalProperties").asBoolean(),
                        "Any field id must be accepted, source tracking "
                                + tracking);
            }
        }

        @Test
        void sourceEntryDeclaresTheTypesTheParserAccepts() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);

            var entry = sourceEntry(controller);
            Assertions.assertEquals("object", entry.path("type").asString());
            var properties = entry.path("properties");
            Assertions.assertEquals("string",
                    properties.path("confidence").path("type").asString());
            Assertions.assertEquals("array",
                    properties.path("extracts").path("type").asString());
            var item = properties.path("extracts").path("items");
            Assertions.assertEquals("object", item.path("type").asString());
            Assertions.assertEquals("string", item.path("properties")
                    .path("text").path("type").asString());
            Assertions.assertEquals("object", item.path("properties")
                    .path("location").path("type").asString());
        }

        @Test
        void sourceEntryPinsTheRectToFourNumbers() {
            // The parser drops a rect that is not four numbers, so the
            // schema must ask for exactly that.
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);

            var rect = sourceEntry(controller).path("properties")
                    .path("extracts").path("items").path("properties")
                    .path("location").path("properties").path("rect");

            Assertions.assertEquals("array", rect.path("type").asString());
            Assertions.assertEquals("number",
                    rect.path("items").path("type").asString());
            Assertions.assertEquals(4, rect.path("minItems").asInt());
            Assertions.assertEquals(4, rect.path("maxItems").asInt());
        }

        @Test
        void sourceEntryListsEveryConfidenceLevel() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);

            var levels = stringsOf(sourceEntry(controller).path("properties")
                    .path("confidence").path("enum"));

            var expected = new ArrayList<String>();
            for (var level : ConfidenceLevel.values()) {
                expected.add(level.name().toLowerCase());
            }
            Assertions.assertEquals(expected, levels,
                    "The schema must offer exactly the levels the parser "
                            + "accepts, in lower case");
        }

        @Test
        void sourceEntryDescribesExtractsAsParserReadsThem() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);

            var extract = sourceEntry(controller).path("properties")
                    .path("extracts").path("items");
            Assertions.assertEquals(List.of("text"),
                    stringsOf(extract.path("required")),
                    "An extract needs its text, the location is optional");
            var location = extract.path("properties").path("location")
                    .path("properties");
            Assertions.assertTrue(location.has("type"));
            Assertions.assertTrue(location.has("page"));
            Assertions.assertTrue(location.has("rect"));
        }

        private JsonNode parameters(String schema) {
            return JacksonUtils.readTree(schema).path("properties");
        }

        /** The schema of one entry of the {@code sources} map. */
        private JsonNode sourceEntry(FormAIController controller) {
            return parameters(fillFormSchema(controller)).path("sources")
                    .path("additionalProperties");
        }

        private List<String> stringsOf(JsonNode array) {
            var strings = new ArrayList<String>();
            array.forEach(node -> strings.add(node.asString()));
            return strings;
        }
    }

    @Nested
    class ToolDescription {

        @Test
        void descriptionChangesOnlyWhileTrackingIsOn() {
            // The instruction prose itself is deliberately not pinned by
            // tests — only that turning tracking on extends the description
            // and turning it off restores the untracked one byte for byte.
            var controller = controllerFor(new TestField());
            var untracked = fillFormDescription(controller);

            controller.setSourceTrackingEnabled(true);
            Assertions.assertNotEquals(untracked,
                    fillFormDescription(controller),
                    "Tracking must add source instructions to the "
                            + "description");

            controller.setSourceTrackingEnabled(false);
            Assertions.assertEquals(untracked, fillFormDescription(controller),
                    "Toggling tracking off must restore the untracked "
                            + "description");
        }

        @Test
        void customConfidenceWordingAppearsInToolDescription() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);
            controller.describeConfidenceLevel(ConfidenceLevel.HIGH,
                    "the value is stated in the contract as a signed figure");

            Assertions.assertTrue(fillFormDescription(controller).contains(
                    "the value is stated in the contract as a signed figure"),
                    "The wording given to describeConfidenceLevel must reach "
                            + "the LLM");
        }

        @Test
        void describeConfidenceLevelRejectsNullArguments() {
            var controller = controllerFor(new TestField());

            var thrown = Assertions.assertThrows(NullPointerException.class,
                    () -> controller.describeConfidenceLevel(null, "text"));
            Assertions.assertEquals("Level must not be null",
                    thrown.getMessage(),
                    "The guard must fail fast with its own message, not "
                            + "through a downstream NPE");
            Assertions.assertThrows(NullPointerException.class, () -> controller
                    .describeConfidenceLevel(ConfidenceLevel.HIGH, null));
        }
    }

    @Nested
    class SourceParsing {

        @Test
        void reportedSourceIsStoredWithTheWrittenValue() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"Acme Ltd\"", """
                    {"confidence": "high", "extracts": [
                      {"text": "Invoiced to Acme Ltd.",
                       "location": {"type": "page-region", "page": 2,
                        "rect": [0.12, 0.34, 0.25, 0.04]}}]}""");

            Assertions.assertEquals("Acme Ltd", field.getValue());
            var source = controller.getFieldSource(field).orElseThrow();
            Assertions.assertEquals(ConfidenceLevel.HIGH, source.confidence());
            Assertions.assertEquals(1, source.extracts().size());
            var extract = source.extracts().get(0);
            Assertions.assertEquals("Invoiced to Acme Ltd.", extract.text());
            var region = Assertions.assertInstanceOf(PageRegion.class,
                    extract.location());
            Assertions.assertEquals(2, region.page());
            Assertions.assertEquals(0.12, region.rect().x());
            Assertions.assertEquals(0.34, region.rect().y());
            Assertions.assertEquals(0.25, region.rect().width());
            Assertions.assertEquals(0.04, region.rect().height());
        }

        @Test
        void sourceLandsOnTheFieldItsKeyNames() {
            // The case the feature exists for: one field read from the
            // attachment, another taken from the prompt, in one fill.
            var fromDocument = new TestField();
            var fromPrompt = new TestField();
            var controller = trackingControllerFor(fromDocument, fromPrompt);

            fillRaw(controller,
                    """
                            {"values": {"%s": "Acme Ltd", "%s": "Ada"},
                             "sources": {"%s": {"confidence": "high",
                               "extracts": [{"text": "Invoiced to Acme Ltd."}]}}}"""
                            .formatted(idOf(fromDocument), idOf(fromPrompt),
                                    idOf(fromDocument)));

            Assertions.assertEquals("Acme Ltd", fromDocument.getValue());
            Assertions.assertEquals("Ada", fromPrompt.getValue());
            var source = controller.getFieldSource(fromDocument).orElseThrow();
            Assertions.assertEquals("Invoiced to Acme Ltd.",
                    source.extracts().get(0).text());
            Assertions.assertTrue(
                    controller.getFieldSource(fromPrompt).isEmpty(),
                    "The prompt-derived field must carry no source");
        }

        @Test
        void sourcesAreMatchedByIdNotByOrder() {
            // Sources listed in the opposite order of the values, each with
            // its own snippet, so a positional match would swap them.
            var first = new TestField();
            var second = new TestField();
            var controller = trackingControllerFor(first, second);

            fillRaw(controller,
                    """
                            {"values": {"%s": "one", "%s": "two"},
                             "sources": {
                               "%s": {"extracts": [{"text": "second snippet"}]},
                               "%s": {"extracts": [{"text": "first snippet"}]}}}"""
                            .formatted(idOf(first), idOf(second), idOf(second),
                                    idOf(first)));

            Assertions.assertEquals("first snippet",
                    controller.getFieldSource(first).orElseThrow().extracts()
                            .get(0).text());
            Assertions.assertEquals("second snippet",
                    controller.getFieldSource(second).orElseThrow().extracts()
                            .get(0).text());
        }

        @Test
        void multipleExtractsAreKeptInReportedOrder() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"total\"", """
                    {"extracts": [
                      {"text": "first"}, {"text": "second"}]}""");

            var texts = controller.getFieldSource(field).orElseThrow()
                    .extracts().stream().map(SourceExtract::text).toList();
            Assertions.assertEquals(List.of("first", "second"), texts);
        }

        @Test
        void plainValueStillWritesWithTrackingOnAndCarriesNoSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, "\"plain\"");

            Assertions.assertEquals("plain", field.getValue());
            Assertions.assertTrue(rejectedIsEmpty(result),
                    "Plain value must not be rejected, got: " + result);
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "Plain value must carry no source");
        }

        @Test
        void sourcesAreIgnoredWhileTrackingIsOff() {
            // With tracking off the value is written as always and whatever
            // the model put under "sources" is not stored.
            var field = new TestField();
            var controller = controllerFor(field);

            var result = fill(controller, field, "\"Acme Ltd\"", """
                    {"confidence": "high", "extracts": [{"text": "x"}]}""");

            Assertions.assertEquals("Acme Ltd", field.getValue());
            Assertions.assertTrue(rejectedIsEmpty(result),
                    "A source must not affect the write, got: " + result);
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "No source must be stored while tracking is off");
        }

        @Test
        void objectValueIsRejectedLikeAnyBadValueWhileTrackingIsOn() {
            // Sources live in their own map; an object where a plain value
            // belongs is not unwrapped but rejected like any bad value.
            var field = new TestField();
            field.setValue("before");
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, """
                    {"value": "Acme Ltd", "confidence": "high"}""");

            Assertions.assertEquals("before", field.getValue());
            Assertions.assertFalse(rejectedIsEmpty(result),
                    "An object value must be rejected, got: " + result);
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty());
        }

        @Test
        void sourceWithoutAValueIsReportedAndNotStored() {
            // The model keyed the source with an id that is not among the
            // values, so the source describes nothing. It is reported in
            // "rejected" so the model can fix the key, and nothing is stored.
            var field = new TestField();
            field.setValue("before");
            var controller = trackingControllerFor(field);

            var result = JacksonUtils.readTree(findTool(controller.getTools(),
                    "fill_form")
                    .execute(JacksonUtils.readTree(
                            "{\"values\": {}, \"sources\": {\"" + idOf(field)
                                    + "\": " + TRACKED_SOURCE + "}}")));

            Assertions.assertEquals("before", field.getValue());
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A source for an unwritten field must not be stored");
            var rejected = result.path("rejected");
            Assertions.assertEquals(1, rejected.size(),
                    "The stray source must be reported once, got: " + result);
            Assertions.assertEquals(idOf(field),
                    rejected.get(0).path("id").asString());
            Assertions.assertTrue(
                    rejected.get(0).path("reason").asString()
                            .contains("not among the ids"),
                    "The reason must point at the key mismatch, got: "
                            + result);
        }

        @Test
        void sourceWithoutAValueIsIgnoredWhileTrackingIsOff() {
            var field = new TestField();
            var controller = controllerFor(field);

            var result = JacksonUtils.readTree(findTool(controller.getTools(),
                    "fill_form")
                    .execute(JacksonUtils.readTree(
                            "{\"values\": {}, \"sources\": {\"" + idOf(field)
                                    + "\": " + TRACKED_SOURCE + "}}")));

            Assertions.assertTrue(rejectedIsEmpty(result),
                    "Without tracking, sources are not read at all, got: "
                            + result);
        }

        @Test
        void nonObjectSourceIsDroppedWhileValueIsWritten() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, "\"Acme\"", "\"page two\"");

            Assertions.assertEquals("Acme", field.getValue());
            Assertions.assertTrue(rejectedIsEmpty(result),
                    "A malformed source must not block the value, got: "
                            + result);
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty());
            Assertions.assertEquals(1, parserDebugMessages().size(),
                    "The malformed source must be logged exactly once, got: "
                            + parserDebugMessages());
        }

        @Test
        void aFillWithoutASourceLogsNothing() {
            // The ordinary case with tracking on: a value taken from the
            // prompt carries no source. That is not a malformed source, so
            // nothing is dropped and nothing is logged.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"plain\"");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty());
            Assertions.assertTrue(parserDebugMessages().isEmpty(),
                    "A value reported without a source must not be logged as "
                            + "a dropped one, got: " + parserDebugMessages());
        }

        @Test
        void sourceWithoutDataYieldsNoSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"bare\"", "{}");

            Assertions.assertEquals("bare", field.getValue());
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A source with no confidence and no extracts must not "
                            + "be stored");
        }

        @Test
        void confidenceOnlySourceYieldsSourceWithEmptyExtracts() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"guess\"", "{\"confidence\": \"low\"}");

            var source = controller.getFieldSource(field).orElseThrow();
            Assertions.assertEquals(ConfidenceLevel.LOW, source.confidence());
            Assertions.assertTrue(source.extracts().isEmpty());
        }

        @Test
        void confidenceIsParsedCaseInsensitively() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", "{\"confidence\": \"Medium\"}");

            Assertions.assertEquals(ConfidenceLevel.MEDIUM, controller
                    .getFieldSource(field).orElseThrow().confidence());
        }

        @Test
        void missingConfidenceMeansUnknownNotLow() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [{"text": "snippet"}]}""");

            Assertions.assertNull(controller.getFieldSource(field).orElseThrow()
                    .confidence());
        }

        @Test
        void missingPageDefaultsToOne() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location": {"type": "page-region",
                       "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var region = (PageRegion) controller.getFieldSource(field)
                    .orElseThrow().extracts().get(0).location();
            Assertions.assertEquals(1, region.page(),
                    "A single-surface source must land on page 1");
        }

        @Test
        void explicitPageOneIsKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location": {"type": "page-region",
                       "page": 1, "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var region = (PageRegion) controller.getFieldSource(field)
                    .orElseThrow().extracts().get(0).location();
            Assertions.assertEquals(1, region.page(),
                    "Page numbers start at 1, so an explicit first page is "
                            + "valid");
        }

        @Test
        void rectBoundaryValuesAreAccepted() {
            // The 0..1 range is inclusive at both ends for x and y, and
            // inclusive at 1 for width and height: a snippet can start at the
            // page edge and a rectangle can span the whole page.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "whole page", "location":
                       {"type": "page-region", "rect": [0, 0, 1, 1]}},
                      {"text": "far corner", "location":
                       {"type": "page-region", "rect": [1, 1, 0.5, 0.5]}}]}""");

            var extracts = controller.getFieldSource(field).orElseThrow()
                    .extracts();
            var wholePage = Assertions.assertInstanceOf(PageRegion.class,
                    extracts.get(0).location(),
                    "A rect covering the whole page must be kept");
            Assertions.assertEquals(0, wholePage.rect().x());
            Assertions.assertEquals(1, wholePage.rect().width());
            Assertions.assertInstanceOf(PageRegion.class,
                    extracts.get(1).location(),
                    "A rect starting at the far page corner must be kept");
        }
    }

    @Nested
    class BestEffortDropping {

        @Test
        void unknownConfidenceLevelIsDroppedWhileValueAndExtractsAreKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, "\"x\"", """
                    {"confidence": "banana",
                     "extracts": [{"text": "snippet"}]}""");

            Assertions.assertEquals("x", field.getValue());
            Assertions.assertTrue(rejectedIsEmpty(result),
                    "Bad confidence must not block the value, got: " + result);
            var source = controller.getFieldSource(field).orElseThrow();
            Assertions.assertNull(source.confidence());
            Assertions.assertEquals("snippet", source.extracts().get(0).text());
            Assertions.assertEquals(1, parserDebugMessages().size(),
                    "The unknown level must be logged exactly once, got: "
                            + parserDebugMessages());
        }

        @Test
        void unknownLocationTypeIsDroppedWhileExtractTextIsKept() {
            // The unknown-type location carries a valid page and rect — the
            // type check alone must drop it, not the shape of the rest.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location":
                       {"type": "time-range", "start": 3, "end": 8,
                        "page": 2, "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var extract = controller.getFieldSource(field).orElseThrow()
                    .extracts().get(0);
            Assertions.assertEquals("snippet", extract.text());
            Assertions.assertNull(extract.location(),
                    "Unknown location type must be dropped");
        }

        @Test
        void nonObjectLocationIsDroppedWhileExtractTextIsKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location": "on page three"}]}""");

            var extract = controller.getFieldSource(field).orElseThrow()
                    .extracts().get(0);
            Assertions.assertEquals("snippet", extract.text());
            Assertions.assertNull(extract.location(),
                    "A non-object location must be dropped");
            Assertions.assertEquals(1, parserDebugMessages().size(),
                    "The malformed location must be logged exactly once, "
                            + "got: " + parserDebugMessages());
        }

        @Test
        void pageRegionWithoutRectDropsLocationButKeepsExtract() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location":
                       {"type": "page-region", "page": 2}}]}""");

            Assertions.assertEquals("x", field.getValue());
            Assertions.assertTrue(rejectedIsEmpty(result),
                    "A rect-less location must not block the value, got: "
                            + result);
            var extract = controller.getFieldSource(field).orElseThrow()
                    .extracts().get(0);
            Assertions.assertEquals("snippet", extract.text());
            Assertions.assertNull(extract.location(),
                    "A page-region without a rect must be dropped");
        }

        @Test
        void malformedRectDropsLocationButKeepsExtract() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"",
                    """
                            {"extracts": [
                                      {"text": "three numbers", "location":
                                       {"type": "page-region", "rect": [0.1, 0.2, 0.3]}},
                                      {"text": "no width", "location":
                                       {"type": "page-region", "rect": [0.1, 0.2, 0, 0.1]}},
                                      {"text": "no height", "location":
                                       {"type": "page-region", "rect": [0.1, 0.2, 0.3, 0]}},
                                      {"text": "out of range", "location":
                                       {"type": "page-region", "rect": [1.5, 0.2, 0.3, 0.1]}},
                                      {"text": "not numbers", "location":
                                       {"type": "page-region", "rect": [0.1, "oops", 0.3, 0.1]}}]}""");

            var extracts = controller.getFieldSource(field).orElseThrow()
                    .extracts();
            Assertions.assertEquals(5, extracts.size(),
                    "Every extract must survive its bad rect");
            extracts.forEach(
                    extract -> Assertions.assertNull(extract.location(),
                            "Malformed rect must drop the location of: "
                                    + extract.text()));
        }

        @Test
        void wholeNumberFloatPageIsAccepted() {
            // LLMs sometimes emit 2.0 for an integer — accepted the same way
            // integer fields accept whole-number floats.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location":
                       {"type": "page-region", "page": 2.0,
                        "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var region = (PageRegion) controller.getFieldSource(field)
                    .orElseThrow().extracts().get(0).location();
            Assertions.assertEquals(2, region.page());
        }

        @Test
        void pageBeyondIntRangeDropsLocationButKeepsExtract() {
            // 4294967297 truncates to 1 in a plain asInt() — the location
            // must be dropped instead of landing on a page it never named.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location":
                       {"type": "page-region", "page": 4294967297,
                        "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var extract = controller.getFieldSource(field).orElseThrow()
                    .extracts().get(0);
            Assertions.assertEquals("snippet", extract.text());
            Assertions.assertNull(extract.location(),
                    "An out-of-int-range page must drop the location");
        }

        @Test
        void invalidPageNumberDropsLocationButKeepsExtract() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"text": "snippet", "location":
                       {"type": "page-region", "page": 0,
                        "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var extract = controller.getFieldSource(field).orElseThrow()
                    .extracts().get(0);
            Assertions.assertEquals("snippet", extract.text());
            Assertions.assertNull(extract.location());
        }

        @Test
        void extractWithoutTextIsDroppedWhileOthersAreKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts": [
                      {"location": {"type": "page-region",
                       "rect": [0.1, 0.2, 0.3, 0.04]}},
                      {"text": "kept"}]}""");

            var extracts = controller.getFieldSource(field).orElseThrow()
                    .extracts();
            Assertions.assertEquals(1, extracts.size());
            Assertions.assertEquals("kept", extracts.get(0).text());
        }

        @Test
        void objectShapedExtractsAreDroppedAsNonArray() {
            // "extracts" must be an array. An object carrying extract-shaped
            // values must not have its values mined for extracts.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"x\"", """
                    {"extracts":
                     {"first": {"text": "snippet"}}}""");

            Assertions.assertEquals("x", field.getValue());
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "Non-array extracts must be dropped entirely");
        }

        @Test
        void rejectedValueStoresNoSource() {
            var field = new DoubleField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, "\"not a number\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");

            Assertions.assertFalse(rejectedIsEmpty(result),
                    "The value must still go through conversion, got: "
                            + result);
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A rejected write must not leave a source behind");
        }
    }

    @Nested
    class SourceLifetime {

        @Test
        void sourceIsReturnedWhileFieldHoldsTheReportedValue() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);

            Assertions.assertTrue(controller.getFieldSource(field).isPresent());
            Assertions.assertTrue(controller.getFieldSource(field).isPresent(),
                    "Reading a source must not consume it");
        }

        @Test
        void sourceGoesStaleWhenUserEditsTheField() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);
            controller.onResponse(AITurnEvents.success());

            field.setValue("edited by hand");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A source must not outlive the value it describes");
        }

        @Test
        void staleSourceDoesNotComeBackWhenValueIsRestored() {
            // Editing away and back is the revert case: once the user edited
            // the field the source is gone for good, not resurrected by the
            // field regaining the AI-written value.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);
            controller.onResponse(AITurnEvents.success());

            field.setValue("edited by hand");
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty());
            field.setValue("Acme");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A stale source must not come back when the old value is "
                            + "restored");
        }

        @Test
        void staleSourceDoesNotComeBackWithoutAnIntermediateRead() {
            // The drop must not depend on anyone observing the source stale:
            // an edit away and back with no getFieldSource call in between
            // must not hand back the old citation for the retyped value.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);
            controller.onResponse(AITurnEvents.success());

            field.setValue("edited by hand");
            field.setValue("Acme");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "An unread edit must still drop the source for good");
        }

        @Test
        void sourceStaleAtTurnEndIsDroppedForGood() {
            // A value-change cascade during a turn can overwrite a sourced
            // field without the write path touching its source, and the
            // eager drop stands down for writes made while the turn runs.
            // The turn-end sweep must drop the entry so a later turn's
            // cascade landing back on the recorded value cannot resurrect
            // a citation that was never reported for its write.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);
            controller.onResponse(AITurnEvents.success());

            controller.onRequest(requestEvent());
            field.setValue("cascaded");
            controller.onResponse(AITurnEvents.success());

            controller.onRequest(requestEvent());
            field.setValue("Acme");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A source stale at turn end must not be returned when a "
                            + "later turn lands on the recorded value again");
        }

        @Test
        void sourcelessWriteDoesNotInheritEarlierSource() {
            // The AI writing back a value an earlier turn sourced — without
            // reporting a source for it — must not revive the old source:
            // the new write never had one, and the old citation would be
            // fabricated for it.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);
            field.setValue("edited by hand");

            controller.onRequest(requestEvent());
            fill(controller, field, "\"Acme\"");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A write without a source must clear the field's source, "
                            + "even when it lands on a previously sourced "
                            + "value");
        }

        @Test
        void refillingAFieldReplacesItsSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"first\"", """
                    {"extracts": [{"text": "old snippet"}]}""");

            fill(controller, field, "\"second\"", """
                    {"extracts": [{"text": "new snippet"}]}""");

            Assertions.assertEquals("new snippet",
                    controller.getFieldSource(field).orElseThrow().extracts()
                            .get(0).text());
        }

        @Test
        void rewritingTheSameValueStoresSourceWithoutChangeEvent() {
            // The model may write a field with the value it already had —
            // no change event fires, but the getter must still return the
            // freshly reported source.
            var field = new TestField();
            field.setValue("Acme");
            var controller = trackingControllerFor(field);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            fill(controller, field, "\"Acme\"", TRACKED_SOURCE);
            controller.onResponse(AITurnEvents.success());

            Assertions.assertTrue(events.isEmpty(),
                    "Writing the value the field already had must not fire a "
                            + "change event");
            Assertions.assertTrue(controller.getFieldSource(field).isPresent(),
                    "The source must still be readable from the getter");
        }

        @Test
        void getFieldSourceRejectsNullField() {
            var controller = controllerFor(new TestField());

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.getFieldSource(null));
        }
    }

    @Nested
    class Restore {

        @Test
        void restoredSourceIsReturnedForTheCurrentValue() {
            var field = new TestField();
            field.setValue("persisted");
            var controller = controllerFor(field);
            var source = new ValueSource(ConfidenceLevel.MEDIUM,
                    List.of(new SourceExtract("snippet", null)));

            controller.restoreFieldSource(field, source);

            Assertions.assertEquals(source,
                    controller.getFieldSource(field).orElseThrow());
        }

        @Test
        void restoredSourceGoesStaleOnNextEdit() {
            var field = new TestField();
            field.setValue("persisted");
            var controller = controllerFor(field);
            controller.onResponse(AITurnEvents.success());
            controller.restoreFieldSource(field,
                    new ValueSource(ConfidenceLevel.MEDIUM, null));

            field.setValue("edited");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty());
        }

        @Test
        void restoreFieldSourceRejectsNullArguments() {
            var field = new TestField();
            var controller = controllerFor(field);
            var source = new ValueSource(null, null);

            var thrown = Assertions.assertThrows(NullPointerException.class,
                    () -> controller.restoreFieldSource(null, source));
            Assertions.assertEquals("Field must not be null",
                    thrown.getMessage(),
                    "The guard must fail fast with its own message, not "
                            + "through a downstream NPE");
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.restoreFieldSource(field, null));
        }
    }

    @Nested
    class ChangeEvent {

        @Test
        void eventCarriesTheReportedSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            fill(controller, field, "\"Acme\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertEquals(1, events.size());
            var source = events.get(0).getFieldSource().orElseThrow();
            Assertions.assertEquals(ConfidenceLevel.HIGH, source.confidence());
            Assertions.assertEquals("snippet", source.extracts().get(0).text());
        }

        @Test
        void eventSourceIsEmptyForPlainValue() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            fill(controller, field, "\"plain\"");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertEquals(1, events.size());
            Assertions.assertTrue(events.get(0).getFieldSource().isEmpty());
        }
    }

    @Nested
    class MarkerConfidence {

        @Test
        void markerShowsConfidenceFromReportedSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"Acme\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertEquals("high",
                    markerOn(field).getProperty("confidence"),
                    "The marker must show the reported confidence level");
        }

        @Test
        void markerShowsNoConfidenceForPlainValue() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"plain\"");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(markerOn(field).getProperty("confidence"),
                    "A value with no reported source must show no indicator");
        }

        @Test
        void markerShowsNoConfidenceWhenSourceHasNoLevel() {
            // A missing level means unknown, not low — the marker must not
            // show the value as doubtful.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "\"Acme\"", """
                    {"extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(markerOn(field).getProperty("confidence"),
                    "A source without a level must show no indicator");
        }

        @Test
        void sameValueRewriteWithNewLevelUpdatesMarkerConfidence() {
            // Rewriting the value the field already had fires no change event
            // and never re-marks, so the kept marker's indicator can only be
            // updated by the write itself — a newly reported level must
            // replace the one shown.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());
            Assertions.assertEquals("high",
                    markerOn(field).getProperty("confidence"));

            controller.onRequest(requestEvent());
            fill(controller, field, "\"Acme\"", """
                    {"confidence": "low",
                     "extracts": [{"text": "re-read"}]}""");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertEquals("low",
                    markerOn(field).getProperty("confidence"),
                    "The kept marker must show the newly reported level");
        }

        @Test
        void sameValueRewriteWithoutLevelClearsMarkerConfidence() {
            // Rewriting the value the field already had fires no change event
            // and never re-marks, but it does replace the source — the kept
            // marker must not show a level the new source does not include.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());
            Assertions.assertEquals("high",
                    markerOn(field).getProperty("confidence"));

            controller.onRequest(requestEvent());
            fill(controller, field, "\"Acme\"", """
                    {"extracts": [{"text": "re-read"}]}""");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(markerOn(field).getProperty("confidence"),
                    "The kept marker must drop the level the new source does "
                            + "not include");
        }

        @Test
        void sourcelessSameValueRewriteClearsMarkerConfidence() {
            // A plain rewrite of the value the field already had clears the
            // stored source, and the kept marker must drop its level with it
            // — an indicator with no source behind it would be fabricated.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());

            controller.onRequest(requestEvent());
            fill(controller, field, "\"Acme\"");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(markerOn(field).getProperty("confidence"),
                    "The kept marker must drop its level when the rewrite "
                            + "carries no source");
        }

        @Test
        void refillWithoutSourceClearsMarkerConfidence() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"first\"", """
                    {"confidence": "medium",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());

            controller.onRequest(requestEvent());
            fill(controller, field, "\"second\"");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(markerOn(field).getProperty("confidence"),
                    "A refill without a source must clear the indicator the "
                            + "previous fill set on the reused marker");
        }

        @Test
        void cascadedValueClearsMarkerConfidence() {
            // A value the application cascades into the field during a turn
            // never goes through the AI's write path, so only the turn-end
            // marking can sync the indicator: the earlier source is stale for
            // the new value, and the reused marker must not keep describing
            // the value the field no longer holds.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, "\"Acme\"", """
                    {"confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(AITurnEvents.success());
            Assertions.assertEquals("high",
                    markerOn(field).getProperty("confidence"));

            controller.onRequest(requestEvent());
            field.setValue("cascaded");
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(markerOn(field).getProperty("confidence"),
                    "A cascaded value must drop the level the previous fill "
                            + "set on the reused marker");
        }

        private static Element markerOn(HasValue<?, ?> field) {
            return ((Component) field).getElement().getChildren().filter(
                    child -> "vaadin-ai-field-marker".equals(child.getTag()))
                    .findFirst().orElseThrow(() -> new AssertionError(
                            "Expected a marker on the field"));
        }
    }

    // --- helpers ---

    /**
     * Builds a controller around a form attached to the mock UI and drives
     * {@code onRequest()} so each field has its id stamped for
     * {@link FormTestSupport#idOf}.
     */
    private FormAIController controllerFor(Component... fields) {
        var form = new Div(fields);
        ui.add(form);
        var controller = new FormAIController(form);
        controller.onRequest(requestEvent());
        return controller;
    }

    private FormAIController trackingControllerFor(Component... fields) {
        return controllerFor(fields).setSourceTrackingEnabled(true);
    }

    /** A minimal source: high confidence with one located extract. */
    private static final String TRACKED_SOURCE = """
            {"confidence": "high", "extracts": [
              {"text": "snippet", "location": {"type": "page-region",
               "page": 1, "rect": [0.1, 0.2, 0.3, 0.04]}}]}""";

    /** Executes {@code fill_form} with the given arguments JSON. */
    private static JsonNode fillRaw(FormAIController controller,
            String arguments) {
        var response = findTool(controller.getTools(), "fill_form")
                .execute(JacksonUtils.readTree(arguments));
        return JacksonUtils.readTree(response);
    }

    /**
     * Executes {@code fill_form} with a single-field payload whose value is the
     * given JSON text and no source, and returns the parsed response.
     */
    private static JsonNode fill(FormAIController controller,
            HasValue<?, ?> field, String jsonValue) {
        var arguments = JacksonUtils.readTree(
                "{\"values\": {\"" + idOf(field) + "\": " + jsonValue + "}}");
        var response = findTool(controller.getTools(), "fill_form")
                .execute(arguments);
        return JacksonUtils.readTree(response);
    }

    /**
     * Executes {@code fill_form} with a single-field payload whose value is the
     * given JSON text and whose source, under the same field id, is the given
     * JSON text, and returns the parsed response.
     */
    private static JsonNode fill(FormAIController controller,
            HasValue<?, ?> field, String jsonValue, String jsonSource) {
        var arguments = JacksonUtils.readTree("{\"values\": {\"" + idOf(field)
                + "\": " + jsonValue + "}, \"sources\": {\"" + idOf(field)
                + "\": " + jsonSource + "}}");
        var response = findTool(controller.getTools(), "fill_form")
                .execute(arguments);
        return JacksonUtils.readTree(response);
    }

    private static boolean rejectedIsEmpty(JsonNode result) {
        return !result.path("rejected").iterator().hasNext();
    }

    private static String fillFormDescription(FormAIController controller) {
        return findTool(controller.getTools(), "fill_form").getDescription();
    }

    private static String fillFormSchema(FormAIController controller) {
        return findTool(controller.getTools(), "fill_form")
                .getParametersSchema();
    }
}
