package org.eclipse.export.util;

import java.io.IOException;
import java.util.Properties;


public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Properties props=System.getProperties(); //获得系统属性集    
//		String osName = props.getProperty("os.name"); 
//		System.out.println(osName.equals("Mac OS X"));
		//FileUtil.copyFile("/workspace/runtime-EclipseApplication/TestPlugin/bin/com/test/qqq/", "/workspace/ClassCode/TestPlugin/bin/com/test/qqq/","Test.class");
		
		try {
			Runtime.getRuntime().exec("open -a finder /workspace/ClassCode/TestPlugin/bin/com/test/qqq/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
