/* 
 * SQLite Exporter primary class
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.app;

import java.io.File;

import com.wappworks.app.sqlite.SqliteConverter;
import com.wappworks.app.sqlite.SqliteConverterExportConfig;
import com.wappworks.common.log.LogWriter;
import com.wappworks.common.log.LogWriterConsole;

public class SqliteExporter
{
	private LogWriter		logWriter = new LogWriterConsole();
	private SqliteConverter	converter = null;
	
	public SqliteExporter()
	{
	}
	
	public void setLogWriter( LogWriter inLogWriter )
	{
		logWriter = inLogWriter;
	}
	
	protected boolean beginExport( File dbFile )
	{
		if( converter != null )
		{
			addLogText( "Attempting to start another export while an existing one is in progress. Skipping..." );
			return( false );
		}
		
		converter = new SqliteConverter( logWriter );
		if( !converter.init( dbFile ) )
		{
			addLogText( "Sqlite DB file is invalid. Aborting..." );
			return( false );
		}
		
		return( true );
	}
	
	protected boolean export( File exportFile, SqliteConverter.ExportFormat format, SqliteConverterExportConfig exportConfig )
	{
		if( converter == null )
		{
			addLogText( String.format("Trying to export to file '%s' without starting up export. Aborting... ", exportFile.getAbsolutePath()) );
			return( false );
		}
		
		if( !converter.export( exportFile, format, exportConfig ) )
		{
			addLogText( "Failed to export data to file " + exportFile.getAbsolutePath() );
			return( false );
		}
		
		return( true );
	}
	
	protected void endExport()
	{
		if( converter == null )
			return;
		
		converter.deInit();
		converter = null;
	}
	
	protected void addLogText( String logText )
	{
		if( logWriter == null )
			return;
		
		logWriter.logAppend( logText );
	}
}
