/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.testutil.ClassesSerializableTest;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class AIComponentsProSerializableTest extends ClassesSerializableTest {

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory\\$LazyHolder",
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\..*",
                "com\\.vaadin\\.flow\\.component\\.grid\\.GridColumnOrderHelper.*",
                "com\\.vaadin\\.flow\\.component\\.grid\\.GridSelectionSignalHelper.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.provider\\..*",
                // Static utility class performing the development-mode
                // license check — not a runtime component
                "com\\.vaadin\\.flow\\.component\\.ai\\.pro\\.AIComponentsProLicenseChecker",
                // GridAIController — intentionally not serializable; restored
                // via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.grid\\.GridAIController(\\$\\d+)?",
                "com\\.vaadin\\.flow\\.component\\.ai\\.grid\\.GridAITools.*",
                // AIController — intentionally not serializable; restored
                // via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIController",
                "com\\.vaadin\\.flow\\.component\\.ai\\.AIComponentsFeatureFlagProvider",
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator\\$Reconnector",
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator\\$Builder",
                // Static utility class with anonymous ToolSpec instances —
                // not instantiable or serializable
                "com\\.vaadin\\.flow\\.component\\.ai\\.chart\\.ChartAITools(\\$\\d+)?",
                // ChartAIController — intentionally not serializable;
                // restored via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.chart\\.ChartAIController(\\$\\d+)?",
                // Build-time generator — not a runtime component
                "com\\.vaadin\\.flow\\.component\\.ai\\.chart\\.PlotOptionsSchemaGenerator",
                // FormAIController and its helpers — intentionally not
                // serializable; restored via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormAIController.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormAITools.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormFieldDiscovery.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormFieldHints.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormFieldSchema.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormFieldType.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormFieldValidation.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormValueConverter.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.FormFieldHighlighter(\\$\\d+)?",
                // vaadin-field-highlighter-flow dependency: a one-shot
                // init utility, not Serializable by design.
                "com\\.vaadin\\.flow\\.component\\.fieldhighlighter\\.FieldHighlighterInitializer(\\$\\d+)?",
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.BinderReflection(\\$\\d+)?",
                // ValueOptions is a transient registration helper: the
                // controller copies its state into FormFieldHints during
                // fieldValueOptions(...) and discards the instance. It is not
                // part
                // of any serialized session state.
                "com\\.vaadin\\.flow\\.component\\.ai\\.form\\.ValueOptions(\\$\\d+)?"));
    }
}
