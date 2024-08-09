/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

/**
 * Wrapper for {@link Actions#sendKeys(CharSequence...)} which retains the
 * functional similarity to the Chrome Driver versions < 75.
 */
public final class SendKeysHelper {
    private static final Set<Keys> modifiers = Stream
            .of(Keys.SHIFT, Keys.ALT, Keys.CONTROL, Keys.META)
            .collect(Collectors.toSet());

    public static void sendKeys(WebDriver driver, CharSequence... keys) {
        Actions actions = new Actions(driver);
        for (CharSequence keySeq : keys) {
            if (modifiers.contains(keySeq)) {
                actions.keyDown(keySeq);
            } else {
                actions.sendKeys(keySeq);
            }
        }
        actions.build().perform();
        resetModifierKeys(driver);
    }

    public static void resetModifierKeys(WebDriver driver) {
        Actions actions = new Actions(driver);
        modifiers.forEach(actions::keyUp);
        actions.build().perform();
    }
}
