/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.flow.component.spreadsheet.shared.PopupButtonState;

/**
 * Pins the JSON wire-format produced by {@link Serializer#serialize(Object)}
 * for each of the 6 DTOs the client reads. Each test populates fields with a
 * known set of values and asserts the exact JSON bytes the server would emit on
 * the wire.
 *
 * <p>
 * These tests pin the server's serialization output so that any field rename,
 * default-value change, or {@code @JsonInclude} edit on the server side
 * surfaces here at PR time — before it would silently drift the wire and break
 * the client.
 *
 * <p>
 * Two Jackson behaviors are load-bearing and pinned implicitly by every
 * assertion: (1) fields are emitted in alphabetical order, not declaration
 * order; (2) global {@code JsonInclude.Include.NON_DEFAULT} compares to the
 * TYPE default (null / 0 / false), not the field-initializer default. So a
 * {@code cellStyle = "cs0"} field-initializer still appears on the wire because
 * {@code "cs0" != null}. A DTO that is a whole snapshot rather than a sparse
 * update overrides that with its own {@code @JsonInclude(ALWAYS)}, because a
 * field left out reads as {@code undefined} on the Lit client, not as 0.
 *
 * <p>
 * If a test fails, the question to answer is: "did I intend to change the wire
 * format, or did I accidentally break it?" Same question
 * {@code SpreadsheetHistoryManagerPayloadTest} answers at the
 * component-snapshot level — this is the granular per-DTO equivalent.
 */
class DtoWireFormatTest {

    @Test
    void cellData_allNonDefaultFields_serializeAlphabetically() {
        CellData cell = new CellData();
        cell.row = 3;
        cell.col = 5;
        cell.value = "hello";
        cell.formulaValue = "=A1+1";
        cell.originalValue = "raw";
        cell.cellStyle = "cs7";
        cell.textColor = "#ff0000";
        cell.locked = true;
        cell.needsMeasure = true;
        cell.isPercentage = true;

        String json = Serializer.serialize(cell);

        assertEquals(
                "{\"cellStyle\":\"cs7\",\"col\":5,"
                        + "\"formulaValue\":\"=A1+1\",\"isPercentage\":true,"
                        + "\"locked\":true,\"needsMeasure\":true,"
                        + "\"originalValue\":\"raw\",\"row\":3,"
                        + "\"textColor\":\"#ff0000\",\"value\":\"hello\"}",
                json);
    }

    @Test
    void cellData_freshInstance_emitsCellStyleAndNullValue() {
        // Cell updates stay sparse: the client keeps what it already
        // has for a field the server does not mention, so leaving the
        // defaults off the wire costs nothing. Two Jackson behaviors
        // ride along: cellStyle = "cs0" is on the wire because
        // NON_DEFAULT compares to the type default (null), not the
        // field initializer; and value is on the wire even when null
        // because CellData.value carries its own @JsonInclude.
        CellData cell = new CellData();

        String json = Serializer.serialize(cell);

        assertEquals("{\"cellStyle\":\"cs0\",\"value\":null}", json);
    }

    @Test
    void groupingData_freshInstance_emitsEveryFieldIncludingZeroes() {
        // A group starting at index 0, at outline level 0, expanded:
        // every one of those is a default, and every one of them is
        // load-bearing. Left off the wire they read as undefined on the
        // client and the outline spans nothing.
        GroupingData grouping = new GroupingData();

        String json = Serializer.serialize(grouping);

        assertEquals("{\"collapsed\":false,\"endIndex\":0,\"level\":0,"
                + "\"startIndex\":0,\"uniqueIndex\":0}", json);
    }

    @Test
    void actionDetails_cellAction_emitsTypeZero() {
        // Type 0 is a cell action, which is the commonest one. Dropped
        // from the wire it left the client with no type at all.
        SpreadsheetActionDetails action = new SpreadsheetActionDetails();
        action.caption = "Insert comment";
        action.key = "1";

        String json = Serializer.serialize(action);

        assertEquals("{\"caption\":\"Insert comment\",\"iconNodeId\":0,"
                + "\"key\":\"1\",\"type\":0}", json);
    }

    @Test
    void mergedRegion_allFields_serializeAlphabetically() {
        MergedRegion region = new MergedRegion(2, 3, 4, 5);
        region.id = 99;

        String json = Serializer.serialize(region);

        assertEquals("{\"col1\":2,\"col2\":4,\"id\":99,\"row1\":3,\"row2\":5}",
                json);
    }

    @Test
    void overlayInfo_freshInstance_emitsTypeIMAGEAndEveryDefault() {
        // The enum reaches the client as the String "IMAGE", and an
        // overlay anchored at row 0, column 0 with no offset still says
        // so rather than leaving the client to guess.
        OverlayInfo overlay = new OverlayInfo();

        String json = Serializer.serialize(overlay);

        assertEquals("{\"col\":0,\"dx\":0.0,\"dy\":0.0,\"height\":0.0,"
                + "\"row\":0,\"type\":\"IMAGE\",\"width\":0.0}", json);
    }

    @Test
    void overlayInfo_componentVariant_emitsEnumName() {
        OverlayInfo overlay = new OverlayInfo(OverlayInfo.Type.COMPONENT);
        overlay.col = 2;
        overlay.row = 3;
        overlay.width = 100f;
        overlay.height = 50f;
        overlay.dx = 1f;
        overlay.dy = 2f;

        String json = Serializer.serialize(overlay);

        assertEquals(
                "{\"col\":2,\"dx\":1.0,\"dy\":2.0,\"height\":50.0,"
                        + "\"row\":3,\"type\":\"COMPONENT\",\"width\":100.0}",
                json);
    }

    @Test
    void spreadsheetActionDetails_intTypeAndIconNodeId_serializeAsNumbers() {
        // type and iconNodeId are numbers on both sides, so the wire carries
        // JSON numbers. This pin catches either of them silently turning
        // into a string.
        SpreadsheetActionDetails details = new SpreadsheetActionDetails();
        details.caption = "Cut";
        details.key = "cut";
        details.type = 1;
        details.iconNodeId = 42;

        String json = Serializer.serialize(details);

        assertEquals("{\"caption\":\"Cut\",\"iconNodeId\":42,\"key\":\"cut\","
                + "\"type\":1}", json);
    }

    @Test
    void popupButtonState_serializesOnlyServerSideFields() {
        // Server is a plain POJO, with only the fields the client reads. This
        // snapshot pins that it stays one.
        PopupButtonState state = new PopupButtonState();
        state.col = 2;
        state.row = 3;
        state.active = true;
        state.headerHidden = true;
        state.popupHeight = "100px";
        state.popupWidth = "200px";
        state.sheet = "Sheet1";

        String json = Serializer.serialize(state);

        assertEquals("{\"active\":true,\"col\":2,\"headerHidden\":true,"
                + "\"popupHeight\":\"100px\",\"popupWidth\":\"200px\","
                + "\"row\":3,\"sheet\":\"Sheet1\"}", json);
    }

    @Test
    void groupingData_allFields_serializeAlphabetically() {
        GroupingData group = new GroupingData(5, 10, 1, 7, true);

        String json = Serializer.serialize(group);

        assertEquals("{\"collapsed\":true,\"endIndex\":10,\"level\":1,"
                + "\"startIndex\":5,\"uniqueIndex\":7}", json);
    }
}
