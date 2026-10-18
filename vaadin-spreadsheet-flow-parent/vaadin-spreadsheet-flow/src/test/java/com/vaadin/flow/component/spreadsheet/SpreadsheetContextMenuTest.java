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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.JsFunctionCallUtil;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * The context menu opened on the cells and headers: what the action handlers do
 * is theirs, failures included, and the icons of their actions are shown only
 * while a menu is open, however the menu was closed.
 */
class SpreadsheetContextMenuTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    @Test
    void whatAnActionHandlerThrows_goesThrough() {
        IllegalStateException thrown = new IllegalStateException("theirs");
        spreadsheet.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                throw thrown;
            }

            @Override
            public void handleAction(Action action, Object sender,
                    Object target) {
            }
        });

        assertSame(thrown,
                assertThrows(IllegalStateException.class,
                        () -> TestHelper.fireClientEvent(spreadsheet,
                                "contextMenuOpenOnSelection",
                                "{\"row\": 2, \"column\": 2}")));
    }

    /** One action, with one icon: the same objects on every menu. */
    private final Icon icon = VaadinIcon.ACADEMY_CAP.create();
    private final Action teach = new Action("Teach", icon);
    private final List<Object> handledOn = new ArrayList<>();

    /**
     * Offers the same action on every menu, as an application keeping its
     * actions in fields does.
     */
    private void offerOneActionWithAnIcon() {
        spreadsheet.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                return new Action[] { teach };
            }

            @Override
            public void handleAction(Action action, Object sender,
                    Object target) {
                handledOn.add(target);
            }
        });
    }

    /**
     * Ends the request; returns the keys of the actions the menu shows, by
     * caption: the component's own actions are offered too.
     */
    private Map<String, String> actionsShown() {
        ui.fakeClientCommunication();
        Map<String, String> keys = new LinkedHashMap<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            if ("showActions".equals(JsFunctionCallUtil
                    .getFunctionName(invocation.getInvocation()))) {
                JsonNode actions = TestHelper.sentJson(JsFunctionCallUtil
                        .getArguments(invocation.getInvocation()).get(0));
                for (JsonNode action : actions) {
                    keys.put(action.get("caption").asString(),
                            action.get("key").asString());
                }
            }
        }
        return keys;
    }

    private void rightClickCell() {
        TestHelper.fireClientEvent(spreadsheet, "contextMenuOpenOnSelection",
                "{\"row\": 2, \"column\": 2}");
    }

    private void rightClickRowHeader() {
        TestHelper.fireClientEvent(spreadsheet, "rowHeaderContextMenuOpen",
                "{\"rowIndex\": 3}");
    }

    private void pick(String type, String key) {
        TestHelper.fireClientEvent(spreadsheet, type,
                "{\"actionKey\": \"" + key + "\"}");
    }

    @Test
    void anActionWhoseIconIsReused_opensTheMenuAgainAfterAPick() {
        offerOneActionWithAnIcon();
        rightClickCell();
        String key = actionsShown().get("Teach");
        pick("actionOnCurrentSelection", key);
        ui.fakeClientCommunication();

        rightClickCell();

        assertEquals(key, actionsShown().get("Teach"));
        assertEquals(spreadsheet.getElement(), icon.getElement().getParent());
    }

    @Test
    void theIconsOfAMenu_areTakenOffOnceAnItemIsPicked() {
        offerOneActionWithAnIcon();
        rightClickRowHeader();
        String key = actionsShown().get("Teach");
        assertEquals(spreadsheet.getElement(), icon.getElement().getParent());

        pick("actionOnRowHeader", key);

        assertNull(icon.getElement().getParent());
        assertEquals(List.of(new CellRangeAddress(2, 2, -1, -1)), handledOn);
    }

    @Test
    void aMenuOpenedWhileTheLastWasNotClosed_showsItsIconAgain() {
        // A client that never said the last menu closed, as a page left
        // while it was open.
        offerOneActionWithAnIcon();
        rightClickCell();
        actionsShown();

        rightClickRowHeader();

        assertTrue(actionsShown().containsKey("Teach"));
        assertEquals(spreadsheet.getElement(), icon.getElement().getParent());
    }
}
