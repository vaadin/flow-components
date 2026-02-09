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
package com.vaadin.flow.component.icon.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-icons/font-icons")
public class FontIconIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void fontIconUsingFont() {
        var icon = findElement(By.className("fa-user"));
        // Verify the FontAwesome icon renders correctly by checking
        // that the ::before pseudo-element has the correct glyph and
        // uses the FontAwesome font family
        var result = executeScript("""
                const style = window.getComputedStyle(arguments[0], '::before');
                const content = style.content;
                const fontFamily = style.fontFamily;
                // Get the code point of the glyph character (content is quoted)
                const codePoint = content.length >= 2
                    ? content.codePointAt(1).toString(16)
                    : null;
                return { fontFamily, codePoint };
                """, icon);

        @SuppressWarnings("unchecked")
        var map = (java.util.Map<String, Object>) result;
        var fontFamily = (String) map.get("fontFamily");
        var codePoint = (String) map.get("codePoint");

        // Verify the glyph is the FontAwesome "user" icon (U+F007)
        Assert.assertEquals("Icon should be the 'user' glyph (f007)", "f007",
                codePoint);

        // Font family should be FontAwesome
        Assert.assertTrue("Font family should be Font Awesome",
                fontFamily.contains("Font Awesome"));
    }
}
