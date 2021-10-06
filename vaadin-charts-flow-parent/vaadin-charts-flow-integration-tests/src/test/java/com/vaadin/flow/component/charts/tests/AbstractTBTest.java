/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.component.charts.ui.MainView;
import com.vaadin.tests.AbstractParallelTest;

public abstract class AbstractTBTest extends AbstractParallelTest {

    @Override
    public void setup() throws Exception {
        super.setup();
        driver.get(getTestUrl(getView()));
    }

    protected ChartElement getChartElement() {
        return $(ChartElement.class).waitForFirst();
    }

    protected WebElement getElementFromShadowRoot(WebElement shadowRootOwner,
            By by) {
        return getElementFromShadowRoot(shadowRootOwner, by, 0);
    }

    protected WebElement getElementFromShadowRoot(WebElement shadowRootOwner,
            By by, int index) {
        WebElement shadowRoot = (WebElement) executeScript(
                "return arguments[0].shadowRoot", shadowRootOwner);
        assertNotNull("Could not locate shadowRoot in the element", shadowRoot);

        List<WebElement> elements = shadowRoot.findElements(by);
        if (elements.size() > index) {
            return elements.get(index);
        }

        throw new AssertionError(
                "Could not find required element in the shadowRoot");
    }

    /**
     * Overriding the way how test path is calculated. In Charts we prepend a
     * fragment like `/vaadin-charts/area/`
     */
    @Override
    protected String getDeploymentPath(Class<?> viewClass) {
        return "/" + viewClass.getCanonicalName()
                .replace(MainView.EXAMPLE_BASE_PACKAGE, "vaadin-charts/")
                .replace(".", "/");
    }

    protected abstract Class<? extends AbstractChartExample> getView();
}
