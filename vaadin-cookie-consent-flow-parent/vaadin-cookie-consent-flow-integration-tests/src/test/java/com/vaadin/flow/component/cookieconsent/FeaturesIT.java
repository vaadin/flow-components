package com.vaadin.flow.component.cookieconsent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.confirmdialog.examples.Features;

public class FeaturesIT extends AbstractParallelTest {

	@Test
	public void test() throws Exception {
		open(Features.class, WINDOW_SIZE_SMALL);
		final CookieConsentElement element = $(CookieConsentElement.class).get(0);
		assertNotNull(element);
		WebElement container = getDriver().findElement(By.className("cc-window"));
		final WebElement dismiss = container.findElement(By.className("cc-dismiss"));
		assertNotNull(dismiss);
		assertEquals("Got it!", dismiss.getText());
		dismiss.click();
		Thread.sleep(1000);
		container = getDriver().findElement(By.className("cc-window"));
		assertFalse(container.isDisplayed());
	}
	
}
