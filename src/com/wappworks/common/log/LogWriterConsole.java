/* 
 * Log-to-console Log Writer
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.common.log;

public class LogWriterConsole implements LogWriter
{
	@Override
	public void logAppend(String logText)
	{
		System.out.print( logText );
	}

}
