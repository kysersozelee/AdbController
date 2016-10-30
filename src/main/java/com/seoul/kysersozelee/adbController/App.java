package com.seoul.kysersozelee.adbController;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.seoul.kysersozelee.adbController.adb.AdbClientComponent;
import com.seoul.kysersozelee.adbController.adb.AdbServerComponent;
import com.seoul.kysersozelee.adbController.domain.AdbResult;

/**
 * Hello world!
 *
 */
public class App 
{
	
    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException
    {
    	AdbClientComponent adbClientComponent = new AdbClientComponent("HC3B2MP00398");
    	
    	Set<String> connectedDevices = adbClientComponent.getDevices();
    	
    	
    	AdbServerComponent adbServerCommunicator = new AdbServerComponent("HC3B2MP00398");

    	AdbResult adbResult = adbServerCommunicator.sendHostMessage("host:devices-l");
    	System.out.println(adbResult);
    	adbResult = adbServerCommunicator.sendHostMessage("host:devices-l");
    	System.out.println(adbResult);
    	adbResult = adbServerCommunicator.sendDeviceMessage("shell:getprop ro.build.version.sdk");
    	System.out.println(adbResult);
    	
    }
}
