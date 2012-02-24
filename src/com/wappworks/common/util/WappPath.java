/* 
 * File path manipulation utilities
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WappPath
{
	// The following regular expression parts are:
	// ^(?:/*)?			Potentially starts with '/' but don't capture
	// (.*/)			Capture up to the last '/'
	private static final String REGEX_CLASSPATHNAMEMATCH = "^(?:/*)?(.*/)";
	
	private static Pattern classPathNamePattern = Pattern.compile( REGEX_CLASSPATHNAMEMATCH );
	
	public static String getClassPath( Class<?> targetClass )
	{
		String classFilePath = targetClass.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		Matcher pathMatcher = classPathNamePattern.matcher( classFilePath );
		if( pathMatcher.groupCount() > 0 )
			return( pathMatcher.group(1) );
		
		return( classFilePath );
	}

	public static String getWorkingPath()
	{
		return( System.getProperty("user.dir") );
	}
}
