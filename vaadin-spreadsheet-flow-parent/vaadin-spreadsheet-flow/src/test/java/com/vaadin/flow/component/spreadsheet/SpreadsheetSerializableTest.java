/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

/**
 * The classes of the component serialize, but those holding what POI makes,
 * which does not, and helpers that are never made.
 */
class SpreadsheetSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                // Holds the POI workbook.
                "com\\.vaadin\\.flow\\.component\\.spreadsheet\\.Spreadsheet",
                // Turns a POI chart into a chart, holding POI's objects
                // while it does.
                "com\\.vaadin\\.flow\\.component\\.spreadsheet\\.charts\\.converter\\..*",
                // As the charts module excludes them, whose jar is here too.
                "com\\.vaadin\\.flow\\.component\\.charts\\..*Serializer(Modifier)?",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializationDelegate",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializerDelegator",
                // The GWT client's state and pop-up button calls, kept
                // deprecated as main has them and never made by the component.
                "com\\.vaadin\\.flow\\.component\\.spreadsheet\\.(shared\\.SpreadsheetState"
                        + "|rpc\\.PopupButtonClientRpc)",
                // Static helpers, never made.
                "com\\.vaadin\\.flow\\.component\\.spreadsheet\\.(ClipboardText(\\$.*)?"
                        + "|ColumnWidths|InvalidFormulaMarks|POIReflectionTools"
                        + "|RangeSequenceDetector|SequenceIncrement"
                        + "|SequenceValueExtractor|Serializer)"));
    }
}
