package com.seoul.kysersozelee.adbController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.seoul.kysersozelee.adbController.adb.AdbResult;
import com.seoul.kysersozelee.adbController.adb.AdbServerCommunicator;

/**
 * Hello world!
 *
 */
public class App 
{
	
    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException
    {
    	AdbServerCommunicator adbServerCommunicator = new AdbServerCommunicator("1215fc23340f0803");

    	AdbResult adbResult = adbServerCommunicator.sendHostMessage("host:devices-l");
    	System.out.println(adbResult);
    	adbResult = adbServerCommunicator.sendHostMessage("host:devices-l");
    	System.out.println(adbResult);
    	adbResult = adbServerCommunicator.sendDeviceMessage("shell:getprop ro.build.version.sdk");
    	System.out.println(adbResult);
    	
    }
}
