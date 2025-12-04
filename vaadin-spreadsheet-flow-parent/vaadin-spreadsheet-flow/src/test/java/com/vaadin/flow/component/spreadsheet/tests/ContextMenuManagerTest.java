/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.CellSelectionManager;
import com.vaadin.flow.component.spreadsheet.ContextMenuManager;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.framework.Action.Handler;

public class ContextMenuManagerTest {

    private ContextMenuManager contextMenuManager;

    @Mock
    private Spreadsheet mockSpreadsheet;

    @Mock
    private Handler mockHandler;

    @Mock
    private CellSelectionManager mockSelectionManager;

    private SelectionChangeEvent mockSelectionEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        contextMenuManager = new ContextMenuManager(mockSpreadsheet);

        // Create a mock selection event
        CellReference cellRef = new CellReference("Sheet1", 0, 0, false, false);
        mockSelectionEvent = new SelectionChangeEvent(mockSpreadsheet, cellRef,
                Collections.emptyList(), null, Collections.emptyList());

        Mockito.when(mockSpreadsheet.getCellSelectionManager())
                .thenReturn(mockSelectionManager);
        Mockito.when(mockSelectionManager.getLatestSelectionEvent())
                .thenReturn(mockSelectionEvent);
    }

    @Test
    public void hasActionHandlers_withNoHandlers_shouldReturnFalse() {
        assertFalse(contextMenuManager.hasActionHandlers());
    }

    @Test
    public void hasActionHandlers_withHandler_shouldReturnTrue() {
        contextMenuManager.addActionHandler(mockHandler);
        assertTrue(contextMenuManager.hasActionHandlers());
    }

    @Test
    public void addActionHandler_nullHandler_shouldNotAddHandler() {
        contextMenuManager.addActionHandler(null);
        assertFalse(contextMenuManager.hasActionHandlers());
    }

    @Test
    public void addActionHandler_sameHandlerTwice_shouldAddOnlyOnce() {
        contextMenuManager.addActionHandler(mockHandler);
        contextMenuManager.addActionHandler(mockHandler);

        // Verify only one handler is added by checking removal behavior
        contextMenuManager.removeActionHandler(mockHandler);
        assertFalse(contextMenuManager.hasActionHandlers());
    }

    @Test
    public void removeActionHandler_existingHandler_shouldRemoveHandler() {
        contextMenuManager.addActionHandler(mockHandler);
        assertTrue(contextMenuManager.hasActionHandlers());

        contextMenuManager.removeActionHandler(mockHandler);
        assertFalse(contextMenuManager.hasActionHandlers());
    }

    @Test
    public void createActionsListForSelection_withValidCaption_shouldCreateActionDetails()
            throws Exception {
        String caption = "Test Action";
        Action action = new Action(caption);
        Action[] actions = { action };

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        assertEquals(caption, details.caption);
        assertEquals(0, details.type); // CELL type
        assertNotNull(details.key);
    }

    @Test
    public void createActionsListForSelection_withNullCaption_shouldUseSanitizedEmptyString()
            throws Exception {
        Action action = new Action(null);
        Action[] actions = { action };

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        assertEquals("", details.caption);
        assertEquals(0, details.type); // CELL type
    }

    @Test
    public void createActionsListForSelection_withHTMLCaption_shouldSanitizeHTML()
            throws Exception {
        String htmlCaption = "<script>alert('xss')</script><b>Bold Text</b><p>Paragraph</p>";
        Action action = new Action(htmlCaption);
        Action[] actions = { action };

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);

        // Normalize whitespace for comparison
        String normalizedCaption = details.caption.replaceAll("\\s+", " ")
                .trim();
        String expectedNormalized = "<b>Bold Text</b> <p>Paragraph</p>"; // Normalized
                                                                         // expected

        assertEquals(expectedNormalized, normalizedCaption);
        assertFalse("Should not contain script tags",
                details.caption.contains("script"));
        assertEquals(0, details.type); // CELL type
    }

    @Test
    public void createActionsListForSelection_withMaliciousHTML_shouldRemoveDangerousContent()
            throws Exception {
        String maliciousCaption = "<script>document.cookie='stolen'</script>"
                + "<img src='x' onerror='alert(1)'>"
                + "<a href='javascript:void(0)'>Link</a>" + "Safe text";
        Action action = new Action(maliciousCaption);
        Action[] actions = { action };

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        // Should contain only safe content, no scripts or dangerous attributes
        assertTrue(details.caption.contains("Safe text"));
        assertFalse(details.caption.contains("script"));
        assertFalse(details.caption.contains("onerror"));
        assertFalse(details.caption.contains("javascript:"));
    }

    @Test
    public void createActionsListForRow_shouldCreateCorrectActionType()
            throws Exception {
        String caption = "Row Action";
        Action action = new Action(caption);
        Action[] actions = { action };
        int rowIndex = 5;

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(mockHandler.getActions(Mockito.any(CellRangeAddress.class),
                Mockito.eq(mockSpreadsheet))).thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForRow", int.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager, rowIndex);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        assertEquals(caption, details.caption);
        assertEquals(1, details.type); // ROW type
    }

    @Test
    public void createActionsListForColumn_shouldCreateCorrectActionType()
            throws Exception {
        String caption = "Column Action";
        Action action = new Action(caption);
        Action[] actions = { action };
        int columnIndex = 3;

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(mockHandler.getActions(Mockito.any(CellRangeAddress.class),
                Mockito.eq(mockSpreadsheet))).thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForColumn", int.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager, columnIndex);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        assertEquals(caption, details.caption);
        assertEquals(2, details.type); // COLUMN type
    }

    @Test
    public void createActionsListForColumn_withHTMLCaption_shouldSanitizeHTML()
            throws Exception {
        String htmlCaption = "<em>Emphasized</em><script>alert('bad')</script>";
        String expectedSanitized = "<em>Emphasized</em>"; // Script removed, em
                                                          // tag kept
        Action action = new Action(htmlCaption);
        Action[] actions = { action };
        int columnIndex = 1;

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(mockHandler.getActions(Mockito.any(CellRangeAddress.class),
                Mockito.eq(mockSpreadsheet))).thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForColumn", int.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager, columnIndex);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        assertEquals(expectedSanitized, details.caption);
        assertEquals(2, details.type); // COLUMN type
    }

    @Test
    public void createActionsListForRow_withHTMLCaption_shouldSanitizeHTML()
            throws Exception {
        String htmlCaption = "<strong>Strong</strong><iframe src='evil.com'></iframe>";
        String expectedSanitized = "<strong>Strong</strong>"; // iframe removed,
                                                              // strong kept
        Action action = new Action(htmlCaption);
        Action[] actions = { action };
        int rowIndex = 2;

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(mockHandler.getActions(Mockito.any(CellRangeAddress.class),
                Mockito.eq(mockSpreadsheet))).thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForRow", int.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager, rowIndex);

        assertEquals(1, result.size());
        SpreadsheetActionDetails details = result.get(0);
        assertEquals(expectedSanitized, details.caption);
        assertEquals(1, details.type); // ROW type
    }

    @Test
    public void createActionsListForSelection_withNullActions_shouldReturnEmptyList()
            throws Exception {
        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(null);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(0, result.size());
    }

    @Test
    public void createActionsListForSelection_withEmptyActions_shouldReturnEmptyList()
            throws Exception {
        Action[] emptyActions = {};

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(emptyActions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(0, result.size());
    }

    @Test
    public void createActionsListForSelection_withMultipleActions_shouldCreateMultipleDetails()
            throws Exception {
        Action action1 = new Action("Action 1");
        Action action2 = new Action("<b>Action 2</b>");
        Action[] actions = { action1, action2 };

        contextMenuManager.addActionHandler(mockHandler);
        Mockito.when(
                mockHandler.getActions(Mockito.any(SelectionChangeEvent.class),
                        Mockito.eq(mockSpreadsheet)))
                .thenReturn(actions);

        // Use reflection to access protected method
        Method method = ContextMenuManager.class
                .getDeclaredMethod("createActionsListForSelection");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<SpreadsheetActionDetails> result = (ArrayList<SpreadsheetActionDetails>) method
                .invoke(contextMenuManager);

        assertEquals(2, result.size());
        assertEquals("Action 1", result.get(0).caption);
        assertEquals("<b>Action 2</b>", result.get(1).caption);
        assertEquals(0, result.get(0).type); // Both should be CELL type
        assertEquals(0, result.get(1).type);
    }
}
