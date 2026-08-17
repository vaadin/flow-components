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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.common.ConfidenceLevel;
import com.vaadin.flow.component.ai.common.PageRegion;
import com.vaadin.flow.component.ai.common.SourceExtract;
import com.vaadin.flow.component.ai.common.ValueSource;
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
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
    class ToolDescription {

        @Test
        void descriptionOmitsSourceInstructionsWhenTrackingOff() {
            var controller = controllerFor(new TestField());

            var description = fillFormDescription(controller);

            Assertions.assertFalse(description.contains("Source tracking"),
                    "Untracked description must not mention source tracking, "
                            + "got: " + description);
        }

        @Test
        void descriptionIncludesEnvelopeShapeWhenTrackingOn() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);

            var description = fillFormDescription(controller);

            Assertions.assertTrue(description.contains("Source tracking is on"),
                    "Description must announce source tracking, got: "
                            + description);
            Assertions.assertTrue(description.contains("\"extracts\""),
                    "Description must describe the envelope shape, got: "
                            + description);
            Assertions.assertTrue(description.contains("page-region"),
                    "Description must name the location type, got: "
                            + description);
        }

        @Test
        void descriptionReflectsTogglingTrackingBackOff() {
            var controller = controllerFor(new TestField());
            var untracked = fillFormDescription(controller);

            controller.setSourceTrackingEnabled(true);
            controller.setSourceTrackingEnabled(false);

            Assertions.assertEquals(untracked, fillFormDescription(controller),
                    "Toggling tracking off must restore the untracked "
                            + "description");
        }

        @Test
        void customConfidenceWordingReplacesDefaultForThatLevelOnly() {
            var controller = controllerFor(new TestField())
                    .setSourceTrackingEnabled(true);
            controller.describeConfidenceLevel(ConfidenceLevel.HIGH,
                    "the value is stated in the contract as a signed figure");

            var description = fillFormDescription(controller);

            Assertions.assertTrue(description.contains(
                    "the value is stated in the contract as a signed figure"),
                    "Custom wording must appear, got: " + description);
            Assertions.assertFalse(
                    description.contains(
                            "written in the document and copied as it is"),
                    "Default wording of the replaced level must be gone, "
                            + "got: " + description);
            Assertions.assertTrue(description.contains(
                    "the document is unclear, or the value is a " + "guess"),
                    "Untouched levels must keep the default wording, got: "
                            + description);
        }

        @Test
        void describeConfidenceLevelRejectsNullArguments() {
            var controller = controllerFor(new TestField());

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.describeConfidenceLevel(null, "text"));
            Assertions.assertThrows(NullPointerException.class, () -> controller
                    .describeConfidenceLevel(ConfidenceLevel.HIGH, null));
        }
    }

    @Nested
    class EnvelopeParsing {

        @Test
        void envelopeValueIsUnwrappedWrittenAndSourceStored() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "Acme Ltd", "confidence": "high", "extracts": [
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
        void multipleExtractsAreKeptInReportedOrder() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "total", "extracts": [
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
        void envelopeIsNotUnwrappedWhenTrackingOff() {
            // With tracking off the behavior is exactly today's: an object
            // is not a valid string-field value and is rejected like any
            // other bad value.
            var field = new TestField();
            field.setValue("before");
            var controller = controllerFor(field);

            var result = fill(controller, field, """
                    {"value": "Acme Ltd", "confidence": "high"}""");

            Assertions.assertEquals("before", field.getValue(),
                    "Envelope must not be unwrapped while tracking is off");
            Assertions.assertFalse(rejectedIsEmpty(result),
                    "Envelope object must be rejected like any bad value, "
                            + "got: " + result);
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty());
        }

        @Test
        void objectWithoutValueKeyIsRejectedWithTrackingOn() {
            var field = new TestField();
            field.setValue("before");
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, """
                    {"confidence": "high", "extracts": [{"text": "x"}]}""");

            Assertions.assertEquals("before", field.getValue());
            Assertions.assertFalse(rejectedIsEmpty(result),
                    "Object without the required value key must be rejected, "
                            + "got: " + result);
        }

        @Test
        void envelopeWithoutSourceDataYieldsNoSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, "{\"value\": \"bare\"}");

            Assertions.assertEquals("bare", field.getValue());
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "An envelope with no confidence and no extracts must not "
                            + "produce a source");
        }

        @Test
        void confidenceOnlyEnvelopeYieldsSourceWithEmptyExtracts() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field,
                    "{\"value\": \"guess\", \"confidence\": \"low\"}");

            var source = controller.getFieldSource(field).orElseThrow();
            Assertions.assertEquals(ConfidenceLevel.LOW, source.confidence());
            Assertions.assertTrue(source.extracts().isEmpty());
        }

        @Test
        void confidenceIsParsedCaseInsensitively() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field,
                    "{\"value\": \"x\", \"confidence\": \"Medium\"}");

            Assertions.assertEquals(ConfidenceLevel.MEDIUM, controller
                    .getFieldSource(field).orElseThrow().confidence());
        }

        @Test
        void missingConfidenceMeansUnknownNotLow() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "x", "extracts": [{"text": "snippet"}]}""");

            Assertions.assertNull(controller.getFieldSource(field).orElseThrow()
                    .confidence());
        }

        @Test
        void missingPageDefaultsToOne() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "x", "extracts": [
                      {"text": "snippet", "location": {"type": "page-region",
                       "rect": [0.1, 0.2, 0.3, 0.04]}}]}""");

            var region = (PageRegion) controller.getFieldSource(field)
                    .orElseThrow().extracts().get(0).location();
            Assertions.assertEquals(1, region.page(),
                    "A single-surface source must land on page 1");
        }
    }

    @Nested
    class BestEffortDropping {

        @Test
        void unknownConfidenceLevelIsDroppedWhileValueAndExtractsAreKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, """
                    {"value": "x", "confidence": "banana",
                     "extracts": [{"text": "snippet"}]}""");

            Assertions.assertEquals("x", field.getValue());
            Assertions.assertTrue(rejectedIsEmpty(result),
                    "Bad confidence must not block the value, got: " + result);
            var source = controller.getFieldSource(field).orElseThrow();
            Assertions.assertNull(source.confidence());
            Assertions.assertEquals("snippet", source.extracts().get(0).text());
        }

        @Test
        void unknownLocationTypeIsDroppedWhileExtractTextIsKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "x", "extracts": [
                      {"text": "snippet", "location":
                       {"type": "time-range", "start": 3, "end": 8}}]}""");

            var extract = controller.getFieldSource(field).orElseThrow()
                    .extracts().get(0);
            Assertions.assertEquals("snippet", extract.text());
            Assertions.assertNull(extract.location(),
                    "Unknown location type must be dropped");
        }

        @Test
        void malformedRectDropsLocationButKeepsExtract() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field,
                    """
                            {"value": "x", "extracts": [
                              {"text": "three numbers", "location":
                               {"type": "page-region", "rect": [0.1, 0.2, 0.3]}},
                              {"text": "no size", "location":
                               {"type": "page-region", "rect": [0.1, 0.2, 0, 0.1]}},
                              {"text": "out of range", "location":
                               {"type": "page-region", "rect": [1.5, 0.2, 0.3, 0.1]}}]}""");

            var extracts = controller.getFieldSource(field).orElseThrow()
                    .extracts();
            Assertions.assertEquals(3, extracts.size(),
                    "Every extract must survive its bad rect");
            extracts.forEach(
                    extract -> Assertions.assertNull(extract.location(),
                            "Malformed rect must drop the location of: "
                                    + extract.text()));
        }

        @Test
        void invalidPageNumberDropsLocationButKeepsExtract() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "x", "extracts": [
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

            fill(controller, field, """
                    {"value": "x", "extracts": [
                      {"location": {"type": "page-region",
                       "rect": [0.1, 0.2, 0.3, 0.04]}},
                      {"text": "kept"}]}""");

            var extracts = controller.getFieldSource(field).orElseThrow()
                    .extracts();
            Assertions.assertEquals(1, extracts.size());
            Assertions.assertEquals("kept", extracts.get(0).text());
        }

        @Test
        void rejectedValueStoresNoSource() {
            var field = new DoubleField();
            var controller = trackingControllerFor(field);

            var result = fill(controller, field, """
                    {"value": "not a number", "confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");

            Assertions.assertFalse(rejectedIsEmpty(result),
                    "The unwrapped value must still go through conversion, "
                            + "got: " + result);
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
            fill(controller, field, trackedValue("Acme"));

            Assertions.assertTrue(controller.getFieldSource(field).isPresent());
            Assertions.assertTrue(controller.getFieldSource(field).isPresent(),
                    "Reading a source must not consume it");
        }

        @Test
        void sourceGoesStaleWhenUserEditsTheField() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, trackedValue("Acme"));

            field.setValue("edited by hand");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "A source must not outlive the value it describes");
        }

        @Test
        void refillingAFieldReplacesItsSource() {
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field,
                    """
                            {"value": "first", "extracts": [{"text": "old snippet"}]}""");

            fill(controller, field,
                    """
                            {"value": "second", "extracts": [{"text": "new snippet"}]}""");

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

            fill(controller, field, trackedValue("Acme"));
            controller.onResponse(null);

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

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.restoreFieldSource(null, source));
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

            fill(controller, field, """
                    {"value": "Acme", "confidence": "high",
                     "extracts": [{"text": "snippet"}]}""");
            controller.onResponse(null);

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
            controller.onResponse(null);

            Assertions.assertEquals(1, events.size());
            Assertions.assertTrue(events.get(0).getFieldSource().isEmpty());
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
        controller.onRequest();
        return controller;
    }

    private FormAIController trackingControllerFor(Component... fields) {
        return controllerFor(fields).setSourceTrackingEnabled(true);
    }

    /**
     * Executes {@code fill_form} with a single-field payload whose value is the
     * given JSON text — a plain value or a source envelope — and returns the
     * parsed response.
     */
    private static JsonNode fill(FormAIController controller,
            HasValue<?, ?> field, String jsonValue) {
        var arguments = JacksonUtils.readTree(
                "{\"values\": {\"" + idOf(field) + "\": " + jsonValue + "}}");
        var response = findTool(controller.getTools(), "fill_form")
                .execute(arguments);
        return JacksonUtils.readTree(response);
    }

    /** A minimal envelope: the given value with one located extract. */
    private static String trackedValue(String value) {
        return """
                {"value": "%s", "confidence": "high", "extracts": [
                  {"text": "snippet", "location": {"type": "page-region",
                   "page": 1, "rect": [0.1, 0.2, 0.3, 0.04]}}]}"""
                .formatted(value);
    }

    private static boolean rejectedIsEmpty(JsonNode result) {
        return !result.path("rejected").iterator().hasNext();
    }

    private static String fillFormDescription(FormAIController controller) {
        return findTool(controller.getTools(), "fill_form").getDescription();
    }
}
