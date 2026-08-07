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
package com.vaadin.flow.component.popover.tests;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/7758.
 * setTarget(attached target) runs onTargetAttach synchronously, which auto-adds
 * the popover and fires its onAttach before
 * targetAttachRegistration/targetDetachRegistration are assigned. If onAttach
 * calls setTarget again (e.g. a UI-scoped component re-applying its target),
 * the re-entrant call NPEs on targetAttachRegistration.remove().
 */
@Route("repro-7758")
public class Repro7758View extends Div {

    public Repro7758View() {
        Span status = new Span("initial");
        status.setId("status");

        NativeButton reentrantTarget = new NativeButton("Reentrant target");
        reentrantTarget.setId("reentrant-target");
        ReentrantPopover reentrantPopover = new ReentrantPopover();
        reentrantPopover.add(new Span("reentrant popover content"));

        NativeButton triggerReentrant = new NativeButton("Set reentrant target",
                event -> {
                    reentrantPopover.setTarget(reentrantTarget);
                    status.setText("reentrant setTarget completed");
                });
        triggerReentrant.setId("trigger-reentrant");

        // Control: plain Popover, identical sequence, no re-entrant call
        NativeButton plainTarget = new NativeButton("Plain target");
        plainTarget.setId("plain-target");
        Popover plainPopover = new Popover();
        plainPopover.add(new Span("plain popover content"));

        NativeButton triggerPlain = new NativeButton("Set plain target",
                event -> {
                    plainPopover.setTarget(plainTarget);
                    status.setText("plain setTarget completed");
                });
        triggerPlain.setId("trigger-plain");

        add(reentrantTarget, plainTarget, triggerReentrant, triggerPlain,
                status);
    }

    // Mimics a UI-scoped component that re-applies its target when attached
    public static class ReentrantPopover extends Popover {
        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
            setTarget(getTarget());
        }
    }
}
