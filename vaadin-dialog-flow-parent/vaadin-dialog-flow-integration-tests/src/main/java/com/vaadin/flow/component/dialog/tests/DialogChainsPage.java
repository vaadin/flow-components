/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.dialog.tests;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Reproduces the "parallel overlay chains" scenario from
 * https://github.com/vaadin/web-components/issues/12191: two independent
 * chains of nested modeless draggable dialogs, where nested dialogs are
 * DOM-nested inside their parent dialog.
 * <p>
 * Both outer dialogs use a client-side workaround that keeps their nested
 * dialogs rendered on top whenever the parent dialog is brought to the front
 * (mousedown / touchstart).
 */
@Route("vaadin-dialog/dialog-chains")
public class DialogChainsPage extends VerticalLayout {

    private int counter = 0;

    public DialogChainsPage() {
        var openChainA = new Button("Open chain A", e -> openChain("A", 40));
        openChainA.setId("open-chain-a");

        var openChainB = new Button("Open chain B", e -> openChain("B", 600));
        openChainB.setId("open-chain-b");

        add(openChainA, openChainB);
    }

    private void openChain(String chain, int left) {
        var dialog = createDialog(chain, "outer", 180, left);

        // Workaround for the stacking change in Vaadin 25.3
        // (https://github.com/vaadin/web-components/issues/12191): keep
        // dialogs nested inside this dialog on top of it whenever it is
        // brought to the front.
        dialog.addAttachListener(e -> keepNestedDialogsOnTop(dialog));

        dialog.open();
    }

    private Dialog createDialog(String chain, String label, int top,
            int left) {
        var dialog = new Dialog();
        dialog.setModality(ModalityMode.MODELESS);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Chain %s — %s".formatted(chain, label));
        dialog.setTop(top + "px");
        dialog.setLeft(left + "px");

        var idPrefix = "chain-%s-%s".formatted(chain, label).toLowerCase()
                .replace(' ', '-');

        var hint = new Span("Chain %s. Drag me by the title bar.".formatted(chain));

        // Scrollable area to verify scroll position is not reset when the
        // dialog is brought to front.
        var lines = new Span(IntStream.rangeClosed(1, 20)
                .mapToObj(i -> "scrollable line " + i)
                .collect(Collectors.joining("\n")));
        lines.getStyle().set("white-space", "pre");
        var scroller = new Scroller(lines);
        scroller.setHeight("80px");
        scroller.setWidth("18em");

        var openNested = new Button("Open nested dialog", e -> {
            counter++;
            var nested = createDialog(chain, "nested " + counter, top + 40,
                    left + 40);
            // Add into the parent dialog so the nested dialog is DOM-nested,
            // like `parent.getElement().appendChild(dialog.getElement())` in
            // the issue reproduction.
            dialog.add(nested);
            nested.open();
        });
        openNested.setId(idPrefix + "-open-nested");

        var close = new Button("Close", e -> dialog.close());
        close.setId(idPrefix + "-close");

        var layout = new VerticalLayout(hint, scroller, openNested, close);
        layout.setPadding(false);
        dialog.add(layout);

        return dialog;
    }

    /**
     * Client-side workaround that keeps dialogs nested inside the given
     * dialog rendered on top of it whenever it is brought to the front by
     * mouse or touch interaction.
     * <p>
     * The dialog's own bring-to-front listener is attached to an inner
     * element, so by the time the event bubbles to the host element the
     * dialog has already been brought to the front. Nested dialogs are then
     * re-raised in DOM order, so the deepest one ends up frontmost. All of
     * this happens synchronously within one browser task, so there is no
     * flicker.
     */
    private static void keepNestedDialogsOnTop(Dialog dialog) {
        dialog.getElement().executeJs(
                """
                        if (!this.__keepNestedDialogsOnTop) {
                          this.__keepNestedDialogsOnTop = () => {
                            this.querySelectorAll('vaadin-dialog').forEach((nested) => {
                              if (nested.opened && nested.$ && nested.$.overlay) {
                                nested.$.overlay.bringToFront();
                              }
                            });
                          };
                          this.addEventListener('mousedown', this.__keepNestedDialogsOnTop);
                          this.addEventListener('touchstart', this.__keepNestedDialogsOnTop);
                        }
                        """);
    }
}
