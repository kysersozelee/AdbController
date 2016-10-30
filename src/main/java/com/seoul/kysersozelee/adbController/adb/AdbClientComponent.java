package com.seoul.kysersozelee.adbController.adb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.seoul.kysersozelee.adbController.domain.AdbResult;
import com.seoul.kysersozelee.adbController.domain.CpuStatus;
import com.seoul.kysersozelee.adbController.domain.DiskStatus;
import com.seoul.kysersozelee.adbController.domain.FrameBuffer;
import com.seoul.kysersozelee.adbController.domain.ImageCompareResult;
import com.seoul.kysersozelee.adbController.domain.MemoryStatus;

public class AdbClientComponent {

	private static final String ADB_INSTALL_PACKAGE = "adb -s %s install %s";

	private static final String HOST_DEVICES = "host:devices-l";
	private static final String SHELL_INPUT = "shell:input";
	private static final String SHELL_INPUT_TAP = SHELL_INPUT + " tap %s %s";
	private static final String SHELL_INPUT_SWIPE = SHELL_INPUT + " swipe %s %s %s %s %s";
	private static final String SHELL_INPUT_TEXT = SHELL_INPUT + " text %s";
	private static final String SHELL_INPUT_KEYEVENT = "shell:input keyevent %s";
	private static final String SHELL_ORIENTATION = "shell:dumpsys input | grep SurfaceOrientation";
	private static final String SHELL_START_ACTIVITY = "shell:am start -n %s/%s";
	private static final String SHELL_STOP_ACTIVITY = "shell:am force-stop %s";
	private static final String SHELL_CAT_PROC_STAT_GREP_CPU = "shell:cat /proc/stat | grep cpu";
	private static final String SHELL_DUMPSYS_MEMINFO_GREP_RAM = "shell:dumpsys meminfo | grep RAM";
	private static final String SHELL_UNINSTALL_ACTIVITY = "shell:pm uninstall %s && pm clear %s";
	private static final String SHELL_BATTERY_LEVEL = "shell:dumpsys battery | grep level";
	private static final String SHELL_DISKFREE = "shell:df %s";
	private static final String SHELL_SDK_LEVEL = "shell:getprop ro.build.version.sdk";
	private static final String SHELL_RELEASE_VERSION = "shell:getprop ro.build.version.release";
	private static final String SHELL_DEVICE_SIZE = "shell:dumpsys display | grep mDefaultViewport";
	private static final String SHELL_PRODUCT_MODEL = "shell:getprop ro.product.model";
	private static final String SHELL_PRODUCT_NAME = "shell:getprop ro.product.name";
	private static final String SHELL_RESTRICTED_SCREEN_SIZE = "shell:dumpsys window | grep mRestricted";
	private static final String SHELL_ROTATE_SCREEN = "shell:content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:%s";
	private static final String SHELL_SET_AUTO_ROTATION = "shell:content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:%s";
	private static final String SHELL_FRAMEBUFFER = "framebuffer:";

	private static final int LONG_PRESS_LIMIT = 300; // ms

	private static final String MSG_START_ACTIVITY_ERROR = "Error type 3";
	private static final String MSG_UNINSTALL_FAIL = "DELETE_FAILED_INTERNAL_ERROR";

	private static final Pattern RESTRICTED_SCREEN_PATTERN = Pattern.compile(".*?(\\d+).*?(\\d+).*?(\\d+).*?(\\d+)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern SHELL_DEVICE_SIZE_PATTERN = Pattern
			.compile(".*?(deviceWidth=)(\\d+).*?(deviceHeight=)(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern ORIENTATION_PATTERN = Pattern.compile(".*?(\\d+)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern BATTERY_LEVEL_PATTERN = Pattern.compile(".*?level.*?(\\d+)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern DISK_FREE_PATTERN = Pattern.compile(
			"((?:\\/[\\w\\.\\-]+)+).*?([+-]?\\d*\\.\\d+)(?![-+0-9\\.]).*?([+-]?\\d*\\.\\d+)(?![-+0-9\\.]).*?([+-]?\\d*\\.\\d+)(?![-+0-9\\.]).*?(\\d+)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private AdbServerComponent adbServerComponent;

	public AdbClientComponent(String serialNumber) throws IOException, InterruptedException, ExecutionException {
		adbServerComponent = new AdbServerComponent(serialNumber);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public AdbClientComponent() throws IOException, InterruptedException, ExecutionException {
		adbServerComponent = new AdbServerComponent();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public void setSerialNumber(String serialNumber) {
		adbServerComponent.setSerialNumber(serialNumber);
	}

	public Set<String> getDevices() throws IOException {
		AdbResult adbResult = adbServerComponent.sendHostMessage(HOST_DEVICES);
		if (adbResult.getData() == null || adbResult.getData().isEmpty()) {
			return Collections.emptySet();
		}

		String[] deviceLines = adbResult.getData().split("\\n");
		List<String> deviceLineList = Arrays.asList(deviceLines);

		Set<String> resultSet = deviceLineList.stream().map(line -> {
			return line.substring(0, line.indexOf(" ")).trim();

		}).collect(Collectors.toSet());
		return Collections.unmodifiableSet(resultSet);
	}

	public int[] getRestrictedScreenSize(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(SHELL_RESTRICTED_SCREEN_SIZE);

		String lines[] = adbResult.getData().split("\n");
		int[] restrictedScreen = new int[2];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.contains("mRestrictedScreen")) {
				Matcher m = RESTRICTED_SCREEN_PATTERN.matcher(line);
				if (m.find()) {
					int[] temp = { Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)) };
					restrictedScreen = temp;
				}

			}
		}

		return restrictedScreen;
	}

	public int[] getDeviceSize(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(SHELL_DEVICE_SIZE);

		String output = adbResult.getData();
		int[] deviceSize = new int[2];
		if (output.contains("deviceWidth") && output.contains("deviceHeight")) {
			Matcher m = SHELL_DEVICE_SIZE_PATTERN.matcher(output);
			if (m.find()) {
				int[] temp = { Integer.parseInt(m.group(2)), Integer.parseInt(m.group(4)) };
				deviceSize = temp;
			}
		}

		return deviceSize;
	}

	public boolean startActivity(String serialNumber, String apkPackageName, String mainActivityName)
			throws IOException {
		AdbResult adbResult = adbServerComponent
				.sendDeviceMessage(String.format(SHELL_START_ACTIVITY, apkPackageName, mainActivityName));

		if (adbResult.getData().contains(MSG_START_ACTIVITY_ERROR))
			return false;
		else
			return true;
	}

	public boolean stopActivity(String serialNumber, String apkPackageName) throws IOException {
		return adbServerComponent.sendDeviceMessage(String.format(SHELL_STOP_ACTIVITY, apkPackageName)).isSuccess();
	}

	public boolean installPackage(String serialNumber, String apkFilePath) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

		try {
			String cmd = String.format(ADB_INSTALL_PACKAGE, serialNumber, apkFilePath);

			CommandLine commandline = CommandLine.parse(cmd);

			DefaultExecutor executor = new DefaultExecutor();
			executor.setWatchdog(new ExecuteWatchdog(1000 * 60 * 5)); // 5
																		// minutes
																		// timeout
			executor.setStreamHandler(streamHandler);
			executor.execute(commandline);

			String output = outputStream.toString();

			if (output.contains("Success"))
				return true;
			else if (output.contains("INSTALL_FAILED_ALREADY_EXISTS"))
				return false;
			else
				return false;

		} catch (Exception e) {
			return false;
		} finally {
			outputStream.close();
			streamHandler.stop();
		}
	}

	public boolean uninstallPackage(String serialNumber, String apkPackageName) throws IOException {
		AdbResult adbResult = adbServerComponent
				.sendDeviceMessage(String.format(SHELL_UNINSTALL_ACTIVITY, apkPackageName, apkPackageName));

		if (adbResult.getData().contains(MSG_UNINSTALL_FAIL))
			return false;
		else
			return true;
	}

	public boolean rotateScreen(String serialNumber, String orientation) throws IOException {
		this.setAutoRotationOption(serialNumber, false);
		adbServerComponent.sendDeviceMessage(String.format(SHELL_ROTATE_SCREEN, orientation));
		return true;
	}

	public boolean setAutoRotationOption(String serialNumber, boolean on) throws IOException {
		return adbServerComponent.sendDeviceMessage(String.format(SHELL_SET_AUTO_ROTATION, ((on) ? 1 : 0))).isSuccess();
	}

	public boolean sendKeyEvent(String serialNumber, String keyCode) throws IOException {
		return adbServerComponent.sendDeviceMessage(String.format(SHELL_INPUT_KEYEVENT, keyCode)).isSuccess();
	}

	public boolean doTap(String serialNumber, String x, String y, String duration) throws IOException {
		if (Integer.parseInt(duration) > LONG_PRESS_LIMIT)
			return adbServerComponent.sendDeviceMessage(String.format(SHELL_INPUT_TAP, x, y)).isSuccess();
		else
			return adbServerComponent.sendDeviceMessage(String.format(SHELL_INPUT_SWIPE, x, y, x, y, duration))
					.isSuccess();

	}

	public boolean doWipe(String serialNumber, String x1, String y1, String x2, String y2, String duration)
			throws NumberFormatException, IOException {
		return adbServerComponent.sendDeviceMessage(String.format(SHELL_INPUT_SWIPE, x1, y1, x2, y2,
				((Integer.parseInt(duration) < LONG_PRESS_LIMIT) ? 200 : duration))).isSuccess();
	}

	public boolean doTextTyping(String serialNumber, String text) throws IOException {
		return adbServerComponent.sendDeviceMessage(String.format(SHELL_INPUT_TEXT, text)).isSuccess();
	}

	public List<CpuStatus> getCpuStatus(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(SHELL_CAT_PROC_STAT_GREP_CPU);

		// CPU 스트링 파싱을 시작한다. 파싱 후 Java Object에 할당
		String[] cpuStatLines = adbResult.getData().split("\n");
		List<CpuStatus> cpus = new ArrayList<>(cpuStatLines.length);

		for (String c : cpuStatLines) {
			String[] parsedData = c.split("\\s+");
			CpuStatus cpu = new CpuStatus();
			if (parsedData[0] != null && parsedData[0].startsWith("cpu")) {
				cpu.setUser(Integer.parseInt(parsedData[1]));
				cpu.setNice(Integer.parseInt(parsedData[2]));
				cpu.setSystem(Integer.parseInt(parsedData[3]));
				cpu.setIdle(Integer.parseInt(parsedData[4]));
				cpu.setIoWait(Integer.parseInt(parsedData[5]));
				cpu.setIrq(Integer.parseInt(parsedData[6]));
				cpu.setSoftIrq(Integer.parseInt(parsedData[7]));

				if (parsedData[0] != null && parsedData[0].equals("cpu")) {
					cpu.setCore(0);
				} else if (parsedData[0].length() == 4) {
					String coreString = parsedData[0].replace("cpu", "");
					cpu.setCore(Integer.parseInt(coreString) + 1);
				}

				cpus.add(cpu);
			}
		}

		return cpus;

	}

	public MemoryStatus getMemStatus(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(SHELL_DUMPSYS_MEMINFO_GREP_RAM);

		String[] memStatLines = adbResult.getData().split("\n");

		MemoryStatus memory = new MemoryStatus();
		for (String m : memStatLines) {
			String[] parsedData = m.split("\\s+");

			for (int j = 0; j < parsedData.length - 1; j++) {
				if (parsedData[j] != null && parsedData[j + 1] != null && parsedData[j + 2] != null) {
					if (parsedData[j].equals("Total") && parsedData[j + 1].equals("RAM:")) {
						memory.setTotal(Integer.parseInt(parsedData[j + 2]));
						break;
					} else if (parsedData[j].equals("Free") && parsedData[j + 1].equals("RAM:")) {
						memory.setFree(Integer.parseInt(parsedData[j + 2]));
						break;
					} else if (parsedData[j].equals("Used") && parsedData[j + 1].equals("RAM:")) {
						memory.setUsed(Integer.parseInt(parsedData[j + 2]));
						break;
					} else if (parsedData[j].equals("Lost") && parsedData[j + 1].equals("RAM:")) {
						memory.setLost(Integer.parseInt(parsedData[j + 2]));
						break;
					}
				}
			}
		}

		return memory;

	}

	public DiskStatus getDiskStatus(String serialNumber, String path) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(String.format(SHELL_DISKFREE, path));

		DiskStatus disk = new DiskStatus();
		if (adbResult.getData().contains(path)) {
			Matcher m = DISK_FREE_PATTERN.matcher(adbResult.getData());
			if (m.find()) {
				disk.setSize(Double.parseDouble(m.group(2)));
				disk.setUsed(Double.parseDouble(m.group(3)));
				disk.setFree(Double.parseDouble(m.group(4)));
				disk.setBlkSize(Integer.parseInt(m.group(5)));
			}
		}

		return disk;
	}

	public int getOrientation(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(String.format(SHELL_ORIENTATION));

		// orientation은 홈버튼을 하단으로 둔 상태가 0이고 시계방향으로 회전할 때 마다 1씩 증가함. 0~3의 값을 가짐
		int orientation = -1;
		Matcher m = ORIENTATION_PATTERN.matcher(adbResult.getData());
		if (m.find())
			orientation = Integer.parseInt(m.group(1));

		return orientation;
	}

	public int getBatteryLevel(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(String.format(SHELL_BATTERY_LEVEL));

		int batteryLevel = 0;
		if (adbResult.getData().contains("level")) {
			Matcher m = BATTERY_LEVEL_PATTERN.matcher(adbResult.getData());
			if (m.find()) {
				batteryLevel = Integer.parseInt(m.group(1));
			}
		}

		return batteryLevel;
	}

	public String getDeviceModel(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(String.format(SHELL_PRODUCT_MODEL));

		return adbResult.getData();
	}

	public String getDeviceName(String serialNumber) throws IOException {
		AdbResult result = adbServerComponent.sendDeviceMessage(String.format(SHELL_PRODUCT_NAME));

		return result.getData();
	}

	public int getSdkLevel(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(String.format(SHELL_SDK_LEVEL));

		int sdkLevel = Integer.parseInt(adbResult.getData().replace("\r", "").replace("\n", ""));
		return sdkLevel;
	}

	public String getReleaseVersion(String serialNumber) throws IOException {
		AdbResult adbResult = adbServerComponent.sendDeviceMessage(String.format(SHELL_RELEASE_VERSION));

		return adbResult.getData();
	}

	public FrameBuffer captureScreenshot(String serialNumber) throws IOException {
		return adbServerComponent.sendFramebufferMessage(SHELL_FRAMEBUFFER);
	}

	public ImageCompareResult imageCompare(String serialNumber, String screenImagePath, String buttonImagePath,
			String outputImagePath) throws IOException {
		Mat targetMat = Imgcodecs.imread(screenImagePath, 0);
		Mat templateMat = Imgcodecs.imread(buttonImagePath, 0);

		int result_cols = targetMat.cols() - templateMat.cols() + 1;
		int result_rows = targetMat.rows() - templateMat.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
		Imgproc.matchTemplate(targetMat, templateMat, result, Imgproc.TM_CCOEFF_NORMED);
		MinMaxLocResult mmr = Core.minMaxLoc(result);

		Point rightBottomPt = new Point((int) mmr.maxLoc.x + templateMat.width(),
				(int) mmr.maxLoc.y + templateMat.height());
		Imgproc.rectangle(targetMat, mmr.maxLoc, rightBottomPt, new Scalar(0, 0, 255));

		Imgcodecs.imwrite(outputImagePath, targetMat);

		return new ImageCompareResult(screenImagePath, buttonImagePath, outputImagePath,
				new java.awt.Point((int) mmr.maxLoc.x, (int) mmr.maxLoc.y), mmr.maxVal, mmr);
	}
}
