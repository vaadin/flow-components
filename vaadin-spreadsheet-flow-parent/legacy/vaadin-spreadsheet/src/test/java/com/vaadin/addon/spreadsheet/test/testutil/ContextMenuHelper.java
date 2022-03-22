package com.vaadin.addon.spreadsheet.test.testutil;

import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.TestBenchTestCase;

public class ContextMenuHelper extends SeleniumHelper {
	
	public ContextMenuHelper(WebDriver driver) {
		super(driver);
	}

	public void clickItem (String caption) {
		try {
		new Actions(driver)
			.click(driver.findElement(By.xpath("//div[@class='popupContent']//*[text()='"+caption+"']")))
			.perform();
		} catch (NoSuchElementException ex) {
			throw new RuntimeException("Menu item '"+caption+"' not found", ex);
		}
		TestBenchTestCase.testBench(driver).waitForVaadin();
	}

	public boolean hasOption (String caption) {
		return driver.findElements(By.xpath("//div[@class='popupContent']//*[text()='"+caption+"']")).size()!=0;
	}
}
