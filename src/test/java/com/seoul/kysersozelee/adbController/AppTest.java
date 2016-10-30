package com.seoul.kysersozelee.adbController;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.seoul.kysersozelee.adbController.adb.AdbClientComponent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	private AdbClientComponent adbClientComponent;
	private String serialNumber;

	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	public void testGetAdbDevices() throws IOException, InterruptedException, ExecutionException {
		adbClientComponent = new AdbClientComponent("1215fc23340f0803");
		Set<String> connectedDevices = adbClientComponent.getDevices();
		assertTrue(connectedDevices.size() > 0);

	}

	public void testScreenCpature() throws IOException, InterruptedException, ExecutionException {
		adbClientComponent = new AdbClientComponent("1215fc23340f0803");
		adbClientComponent.captureScreenshot(serialNumber);
	}
}
