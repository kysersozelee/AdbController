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
    	AdbClientComponent adbClientComponent = new AdbClientComponent("1215fc23340f0803");
    	
    	Set<String> connectedDevices = adbClientComponent.getDevices();
    }
}
