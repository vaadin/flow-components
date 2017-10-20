package com.vaadin.addon.charts.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.annotations.RunOnHub;
import com.vaadin.testbench.parallel.BrowserUtil;
import com.vaadin.testbench.parallel.ParallelTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@RunOnHub
public class VaadinChartIT extends ParallelTest {

	@Override
	protected String getHubURL() {
		final String username = System.getProperty("SAUCE_USERNAME");
		final String accessKey = System.getProperty("SAUCE_ACCESS_KEY");
		return "http://" + username + ":" + accessKey + "@localhost:4445/wd/hub";
	}

	@Before
	public void setUp() {
		getDriver().get("http://" + findAutoHostname() + ":8080");
	}

	@Test
	public void checkChartDisplayed() {
		waitUntilPresent(By.tagName("vaadin-chart"));
		final WebElement chart = findElement(By.tagName("vaadin-chart"));
		assertNotNull(chart);
		final WebElement title = getElementFromShadowRoot(chart, By.className("highcharts-title"));
		assertTrue(title.getText().contains("First Chart for Flow!"));
	}

	private void waitUntilPresent(By by) {
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	private WebElement getElementFromShadowRoot(WebElement shadowRootOwner, By by) {
		WebElement shadowRoot = (WebElement) executeScript("return arguments[0].shadowRoot", shadowRootOwner);
		assertNotNull("Could not locate shadowRoot in the element", shadowRoot);
		return shadowRoot.findElements(by).stream().findFirst()
				.orElseThrow(() -> new AssertionError("Could not find required element in the shadowRoot"));
	}

	/**
	 * @return all supported browsers which are actively tested
	 */
	public List<DesiredCapabilities> getAllBrowsers() {
		List<DesiredCapabilities> allBrowsers = new ArrayList<>();
			allBrowsers.add(BrowserUtil.chrome());
		return Collections.unmodifiableList(allBrowsers);
	}

	@BrowserConfiguration
	public List<DesiredCapabilities> getBrowserConfiguration() {
		return getAllBrowsers();
	}

	/**
	 * Tries to automatically determine the IP address of the machine the test
	 * is running on.
	 *
	 * @return An IP address of one of the network interfaces in the machine.
	 * @throws RuntimeException
	 *             if there was an error or no IP was found
	 */
	private static String findAutoHostname() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface nwInterface = interfaces.nextElement();
				if (!nwInterface.isUp() || nwInterface.isLoopback()
						|| nwInterface.isVirtual()) {
					continue;
				}
				Enumeration<InetAddress> addresses = nwInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address.isLoopbackAddress()) {
						continue;
					}
					if (address.isSiteLocalAddress()) {
						return address.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException("Could not enumerate ");
		}

		throw new RuntimeException(
				"No compatible (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16) ip address found.");
	}
}
