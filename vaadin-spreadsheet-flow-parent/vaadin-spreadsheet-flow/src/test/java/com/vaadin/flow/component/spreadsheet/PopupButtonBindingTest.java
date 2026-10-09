/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

/**
 * Attaching pop-up buttons to the spreadsheet element and detaching them with
 * {@link PopupButtonBinding}.
 *
 * <p>
 * Pins the attach / detach / detachAll contract:
 *
 * <ul>
 * <li>{@code attach}: tracks the button in {@code attachedButtons}, calls the
 * client-side {@code addPopupButton} via Element.callJsFunction, appends the
 * button's element as a virtual child of the spreadsheet ONLY if it's not
 * already there.</li>
 * <li>{@code detach}: removes from tracking, calls client-side
 * {@code removePopupButton}, removes the virtual child ONLY if the spreadsheet
 * was its parent.</li>
 * <li>{@code detachAll}: detaches every tracked button and clears the tracking
 * set. Early-return when empty.</li>
 * </ul>
 */
class PopupButtonBindingTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element spreadsheetElement;
    @Mock
    private PopupButton buttonA;
    @Mock
    private PopupButton buttonB;
    @Mock
    private Element buttonAElement;
    @Mock
    private Element buttonBElement;

    private PopupButtonBinding binding;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        binding = new PopupButtonBinding(spreadsheet);
        lenient().when(spreadsheet.getElement()).thenReturn(spreadsheetElement);
        lenient().when(buttonA.getElement()).thenReturn(buttonAElement);
        lenient().when(buttonB.getElement()).thenReturn(buttonBElement);
        // State is null on a mocked PopupButton — Serializer.serialize(null)
        // returns "null" which is acceptable as a JS function argument.
    }

    @Test
    void attach_callsAddPopupButtonRpc_andAppendsVirtualChild() {
        when(buttonAElement.getParent()).thenReturn(null);

        binding.attach(buttonA);

        verify(spreadsheetElement).callJsFunction(eq("addPopupButton"),
                anyString());
        verify(spreadsheetElement).appendVirtualChild(buttonAElement);
    }

    @Test
    void attach_buttonAlreadyChildOfSpreadsheet_doesNotReAppend() {
        // Pin: the parent check guards against double-appending. If the
        // element's parent IS the spreadsheet, the virtual-child add is
        // skipped. The JS callback still fires (the web component needs to
        // know about the re-attach).
        when(buttonAElement.getParent()).thenReturn(spreadsheetElement);

        binding.attach(buttonA);

        verify(spreadsheetElement).callJsFunction(eq("addPopupButton"),
                anyString());
        verify(spreadsheetElement, never()).appendVirtualChild(buttonAElement);
    }

    @Test
    void detach_callsRemovePopupButtonRpc_andRemovesVirtualChild() {
        // Pin: detach is the symmetric inverse — RPC fires + virtual child
        // removed if it was a child of the spreadsheet.
        when(buttonAElement.getParent()).thenReturn(spreadsheetElement);

        binding.detach(buttonA);

        verify(spreadsheetElement).callJsFunction(eq("removePopupButton"),
                anyString());
        verify(spreadsheetElement).removeVirtualChild(buttonAElement);
    }

    @Test
    void detach_buttonNotChildOfSpreadsheet_skipsVirtualChildRemoval() {
        // Pin: a button whose element isn't actually a child of the
        // spreadsheet doesn't have removeVirtualChild called. The RPC
        // still fires unconditionally.
        when(buttonAElement.getParent()).thenReturn(null);

        binding.detach(buttonA);

        verify(spreadsheetElement).callJsFunction(eq("removePopupButton"),
                anyString());
        verify(spreadsheetElement, never()).removeVirtualChild(buttonAElement);
    }

    @Test
    void detachAll_emptyAttachedSet_isNoop() {
        // Pin the early-return: no JS calls, no virtual-child operations.
        binding.detachAll();

        verify(spreadsheetElement, never()).callJsFunction(anyString(),
                anyString());
        verify(spreadsheetElement, never())
                .removeVirtualChild(any(Element.class));
    }

    @Test
    void detachAll_detachesEveryTrackedButton() {
        // Pin: detachAll iterates the tracked set and calls detach on each.
        // The tracking set is then cleared.
        when(buttonAElement.getParent()).thenReturn(spreadsheetElement);
        when(buttonBElement.getParent()).thenReturn(spreadsheetElement);
        binding.attach(buttonA);
        binding.attach(buttonB);

        binding.detachAll();

        // Both buttons should have had detach side effects.
        verify(spreadsheetElement, times(2))
                .callJsFunction(eq("removePopupButton"), anyString());
        verify(spreadsheetElement).removeVirtualChild(buttonAElement);
        verify(spreadsheetElement).removeVirtualChild(buttonBElement);
    }

    @Test
    void detachAll_iteratesOverSnapshot_safeFromConcurrentModification() {
        // Pin: detachAll uses `new ArrayList<>(attachedButtons)` as the
        // iteration source so detach() removing entries from the set
        // during iteration doesn't cause ConcurrentModificationException.
        when(buttonAElement.getParent()).thenReturn(spreadsheetElement);
        when(buttonBElement.getParent()).thenReturn(spreadsheetElement);
        binding.attach(buttonA);
        binding.attach(buttonB);

        // Should not throw.
        binding.detachAll();

        // Confirm both got detached.
        verify(spreadsheetElement).removeVirtualChild(buttonAElement);
        verify(spreadsheetElement).removeVirtualChild(buttonBElement);
    }

    @Test
    void detachAll_clearsAttachedSet_subsequentCallIsNoop() {
        // Pin: after detachAll, the tracking set is empty, so a second
        // detachAll is a no-op.
        when(buttonAElement.getParent()).thenReturn(spreadsheetElement);
        binding.attach(buttonA);
        binding.detachAll();

        // Second call.
        org.mockito.Mockito.clearInvocations(spreadsheetElement);
        binding.detachAll();

        verify(spreadsheetElement, never()).callJsFunction(anyString(),
                anyString());
    }

    @Test
    void attachThenDetach_singleButton_endsWithBothCallsFired() {
        when(buttonAElement.getParent()).thenReturn(null) // first: pre-attach
                .thenReturn(spreadsheetElement); // then: post-attach

        binding.attach(buttonA);
        binding.detach(buttonA);

        verify(spreadsheetElement).callJsFunction(eq("addPopupButton"),
                anyString());
        verify(spreadsheetElement).callJsFunction(eq("removePopupButton"),
                anyString());
        verify(spreadsheetElement).appendVirtualChild(buttonAElement);
        verify(spreadsheetElement).removeVirtualChild(buttonAElement);
    }
}
