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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLoggerFactory;
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

        @Test
        void explicitPageOneIsKept() {
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "x", "extracts": [
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

            fill(controller, field, """
                    {"value": "x", "extracts": [
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

            var result = fill(controller, field, """
                    {"value": "x", "confidence": "banana",
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

            fill(controller, field, """
                    {"value": "x", "extracts": [
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

            fill(controller, field, """
                    {"value": "x", "extracts": [
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

            var result = fill(controller, field, """
                    {"value": "x", "extracts": [
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

            fill(controller, field,
                    """
                            {"value": "x", "extracts": [
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

            fill(controller, field, """
                    {"value": "x", "extracts": [
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

            fill(controller, field, """
                    {"value": "x", "extracts": [
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
        void objectShapedExtractsAreDroppedAsNonArray() {
            // "extracts" must be an array. An object carrying extract-shaped
            // values must not have its values mined for extracts.
            var field = new TestField();
            var controller = trackingControllerFor(field);

            fill(controller, field, """
                    {"value": "x", "extracts":
                     {"first": {"text": "snippet"}}}""");

            Assertions.assertEquals("x", field.getValue());
            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "Non-array extracts must be dropped entirely");
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
            controller.onResponse(null);

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
            fill(controller, field, trackedValue("Acme"));
            controller.onResponse(null);

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
            fill(controller, field, trackedValue("Acme"));
            controller.onResponse(null);

            field.setValue("edited by hand");
            field.setValue("Acme");

            Assertions.assertTrue(controller.getFieldSource(field).isEmpty(),
                    "An unread edit must still drop the source for good");
        }

        @Test
        void sourcelessWriteDoesNotInheritEarlierSource() {
            // The AI writing back a value an earlier turn sourced — without
            // reporting a source for it — must not revive the old source:
            // the new write never had one, and the old citation would be
            // fabricated for it.
            var field = new TestField();
            var controller = trackingControllerFor(field);
            fill(controller, field, trackedValue("Acme"));
            field.setValue("edited by hand");

            controller.onRequest();
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
            controller.onResponse(null);
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
