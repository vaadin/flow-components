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
 * for each of the 6 DTOs whose server-side shape is consumed by the
 * GWT-compiled client. Each test populates fields with a known set of values
 * and asserts the exact JSON bytes the server would emit on the wire.
 *
 * <p>
 * These are NOT "mirror" tests — the server-side and client-side classes have
 * intentionally different shapes (server uses enums where the client uses
 * String constants; server uses {@code int} where the client uses
 * {@code String}; the client side of {@code PopupButtonState} extends
 * {@code AbstractComponentState} where the server is a POJO). They communicate
 * via Jackson's tolerant cross-type JSON convention. These tests pin the
 * server's serialization output so that any field rename, default-value change,
 * or {@code @JsonInclude} edit on the server side surfaces here at PR time —
 * before it would silently drift the wire and break the GWT client.
 *
 * <p>
 * Two Jackson behaviors are load-bearing and pinned implicitly by every
 * assertion: (1) fields are emitted in alphabetical order, not declaration
 * order; (2) global {@code JsonInclude.Include.NON_DEFAULT} compares to the
 * TYPE default (null / 0 / false), not the field-initializer default. So a
 * {@code cellStyle = "cs0"} field-initializer still appears on the wire because
 * {@code "cs0" != null}.
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
        // Pins two Jackson behaviors at once:
        // (1) cellStyle = "cs0" is on the wire because Jackson NON_DEFAULT
        // compares to type default (null), not field initializer ("cs0").
        // (2) value is on the wire even when null, because CellData.value
        // carries @JsonInclude (no argument) which defaults to ALWAYS —
        // overriding the global NON_DEFAULT for that field.
        // Both are load-bearing for the GWT client which expects them.
        CellData cell = new CellData();

        String json = Serializer.serialize(cell);

        assertEquals("{\"cellStyle\":\"cs0\",\"value\":null}", json);
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
    void overlayInfo_freshInstance_emitsTypeIMAGE() {
        // Pins that the IMAGE field-initializer default IS on the wire (because
        // Jackson NON_DEFAULT for an enum field compares to null, not to the
        // initializer). The GWT client reads "IMAGE" as a String — matching
        // its own IMAGE String constant.
        OverlayInfo overlay = new OverlayInfo();

        String json = Serializer.serialize(overlay);

        assertEquals("{\"type\":\"IMAGE\"}", json);
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
        // type is `int` on the server, `String` on the client. iconNodeId is
        // similarly typed differently. Wire format is JSON numbers; the GWT
        // client coerces. This pin catches either side silently flipping to
        // the other's type.
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
        // Server is a plain POJO; the GWT-side mirror extends
        // AbstractComponentState (a Vaadin-8-era base class with caption /
        // description / enabled / etc.). The wire-format compatibility relies
        // on the client tolerating ABSENT base-class fields — which
        // Serializer's
        // NON_DEFAULT inclusion guarantees by never emitting them from the
        // server. This snapshot pins that the server side stays a clean POJO.
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
