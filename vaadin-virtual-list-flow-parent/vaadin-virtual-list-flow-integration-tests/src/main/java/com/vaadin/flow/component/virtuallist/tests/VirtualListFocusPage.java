/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.stream.IntStream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-virtual-list/virtual-list-focus")
public class VirtualListFocusPage extends Div {

    private VirtualList<String> list;
    private Div statusDiv;
    private boolean firstItemFocused = false;

    public VirtualListFocusPage() {
        // Create virtual list with items
        list = new VirtualList<>();
        list.setId("virtual-list");
        list.setItems(
                IntStream.range(0, 100).mapToObj(i -> "Item " + i).toList());

        // Use component renderer with focusable elements
        list.setRenderer(new ComponentRenderer<>(item -> {
            NativeButton button = new NativeButton(item);
            button.setId("item-" + item.replace(" ", "-"));
            button.getElement().setAttribute("tabindex", "0");
            return button;
        }));

        // Status div to report focus state
        statusDiv = new Div();
        statusDiv.setId("status");
        statusDiv.setText("Not focused");

        // Button to trigger focus on first item
        NativeButton focusFirstButton = new NativeButton("Focus First Item",
                e -> {
                    focusFirstItem();
                });
        focusFirstButton.setId("focus-first-button");

        // Button to reset the list (simulating navigation)
        NativeButton resetButton = new NativeButton("Reset List", e -> {
            resetList();
        });
        resetButton.setId("reset-button");

        add(focusFirstButton, resetButton, statusDiv, list);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Try to focus first item when page loads
        focusFirstItem();
    }

    private void focusFirstItem() {
        // Execute focus after a small delay to ensure rendering is complete
        getElement().executeJs(
                """
                        setTimeout(() => {
                          const list = document.getElementById('virtual-list');
                          const firstItem = list.querySelector('[id^="item-"]');
                          if (firstItem) {
                            firstItem.focus();
                            document.getElementById('status').textContent = 'First item focused: ' + document.activeElement.id;
                            return true;
                          } else {
                            document.getElementById('status').textContent = 'No item found to focus';
                            return false;
                          }
                        }, 100);
                        """);
    }

    private void resetList() {
        // Simulate resetting the list (like navigating away and back)
        list.setItems(
                IntStream.range(0, 100).mapToObj(i -> "Item " + i).toList());
        statusDiv.setText("List reset");

        // Try to focus first item after reset
        getElement().executeJs("""
                setTimeout(() => {
                  document.getElementById('focus-first-button').click();
                }, 200);
                """);
    }
}
