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
package com.vaadin.flow.component.ai.orchestrator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.vaadin.tests.MockUIExtension;

/**
 * Lets a turn that ends on a background thread run its end-of-turn
 * {@code ui.access()} task in tests. The mock session is locked by the test
 * thread for the whole test, and {@code ui.access()} from another thread waits
 * for that lock, so a test that waits for such a turn has to release the lock
 * while it waits.
 */
final class MockSession {

    private MockSession() {
    }

    /**
     * Waits up to five seconds for the latch with the mock session unlocked,
     * and locks it again before returning.
     *
     * @param ui
     *            the extension holding the mock session
     * @param latch
     *            the latch the background thread counts down
     * @return whether the latch reached zero before the timeout
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    static boolean awaitUnlocked(MockUIExtension ui, CountDownLatch latch)
            throws InterruptedException {
        ui.getSession().unlock();
        try {
            return latch.await(5, TimeUnit.SECONDS);
        } finally {
            ui.getSession().lock();
        }
    }
}
