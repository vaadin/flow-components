package com.vaadin.flow.component.cookieconsent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-cookie-consent")
public class CookieConsentElement extends TestBenchElement {

	WebElement getDismissButton() {
		return getDriver().findElement(By.className("cc-dismiss"));				
		//return findElement(By.className("cc-dismiss"));				
	}
}
