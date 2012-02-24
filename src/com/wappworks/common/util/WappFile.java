/* 
 * File related utilities
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class WappFile
{
	// Read the entire contents of a file as string. Returns null if the operation fails
	public static String readFileAsString( String filePath, int bytesMax )
	{
		return( readFileAsString(filePath, bytesMax, null) );
	}
	
	public static String readFileAsString( String filePath, int lengthMax, String charEncoding )
	{
		try
		{
			InputStreamReader inputStream;
			if( charEncoding == null )
				inputStream = new InputStreamReader( new FileInputStream(filePath) );
			else
				inputStream = new InputStreamReader( new FileInputStream(filePath), charEncoding );
			
			char[] fileContent = new char[ lengthMax + 1 ];
			int readLength = inputStream.read( fileContent, 0, lengthMax );
			inputStream.close();
			
			if( readLength < 0 )
				return( null );
			
			fileContent[ readLength ] = 0;
			return( new String(fileContent) );
		}
		catch( FileNotFoundException e )		{	return( null );		}
		catch( UnsupportedEncodingException e )	{	return( null );		}
		catch( IOException e )					{	return( null );		}
	}
}
