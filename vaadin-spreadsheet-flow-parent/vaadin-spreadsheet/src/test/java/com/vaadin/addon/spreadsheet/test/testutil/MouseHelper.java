package com.vaadin.addon.spreadsheet.test.testutil;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.TestBenchTestCase;

public class MouseHelper extends SeleniumHelper {

	public MouseHelper(WebDriver driver) {
		super(driver);
	}

	public void rightClick (WebElement element) {
		new Actions(driver)
			.contextClick(element)
			.perform();
		TestBenchTestCase.testBench(driver).waitForVaadin();
	}
	
	public void toolTip (WebElement element) {
		
		new Actions(driver)
			.moveToElement(element)
			.perform();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
