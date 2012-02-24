/* 
 * SQLite Converter
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.app.sqlite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.wappworks.common.log.LogWriter;

public class SqliteConverter
{
	public enum ExportFormat
	{
		XML,
		JSON
	};
	
	private LogWriter					logWriter;
	private Connection					dbConn = null;
	private Vector<SqliteTableHandler> 	tableHandlers;
	
	public SqliteConverter( LogWriter inLogWriter )
	{
		logWriter = inLogWriter;
	}
	
	public Boolean init( File dbFile )
	{
		logWriter.logAppend( "Sqlite database load commencing" );
		
	    try
	    {
			Class.forName("org.sqlite.JDBC");
			dbConn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
			
			tableHandlers = processSchema();
		}
	    catch (ClassNotFoundException e)
	    {
			logWriter.logAppend( String.format("Class not found exception occurred while initializing converter:\n%1$s", e.toString()) );
	    	return( false );	
	    }
	    catch (SQLException e)
	    {
			logWriter.logAppend( String.format("Class not found exception occurred while initializing converter:\n%1$s", e.toString()) );
	    	return( false );
	    }

		logWriter.logAppend( "Sqlite database load complete" );
		
		return( true );
	}
	
	public Boolean deInit()
	{
		try
		{
			if( dbConn != null )
				dbConn.close();
		}
	    catch (SQLException e)				{	return( false );	}
		
		dbConn = null;
		tableHandlers = null;
		
		logWriter.logAppend( "Sqlite database closed" );
		
		return( true );
	}
	
	private Vector<SqliteTableHandler> processSchema() throws SQLException
	{
		Vector<SqliteTableHandler> tableHandlers = new Vector<SqliteTableHandler>();
		
		// Get the table names
		Statement dbStat = dbConn.createStatement();
	    ResultSet rs = dbStat.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;" );
	    try
	    {
		    while (rs.next())
		    {
		    	String tableName = rs.getString( "name" );
	    		logWriter.logAppend( String.format( "Processing schema for table '%1$s'", tableName) );
	        	SqliteTableHandler tableHandler = new SqliteTableHandler( tableName, dbConn, logWriter );
	        	tableHandlers.add( tableHandler );
		    }
	    }
	    finally
	    {
	    	rs.close();
	    }
	    
        return( tableHandlers );
	}
	
	public Boolean export( File outFile, ExportFormat format )
	{
		return( export( outFile, format, new SqliteConverterExportConfig() ));
	}
	
	public Boolean export( File outFile, ExportFormat format, SqliteConverterExportConfig config )
	{
		switch( format )
		{
			case XML:
				return( exportToXml(outFile, config) );
				
			case JSON:
				return( exportToJson(outFile, config) );
			default:
				return( false );
		}
	}
	
	private Boolean exportToXml( File xmlFile, SqliteConverterExportConfig config )
	{
		logWriter.logAppend( "XML export commencing" );
		
		// Handle the export...
		try
		{
			FileWriter		fStream = new FileWriter( xmlFile );
			BufferedWriter	fOut	= new BufferedWriter( fStream );
			
			fOut.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
			fOut.write( "<database>\n" );
			exportTablesToXml( fOut, "\t", config );
			fOut.write( "</database>\n" );
			
			fOut.close();
			fStream.close();
		}
		catch( IOException e )
		{
			logWriter.logAppend( String.format("I/O error encountered trying to output database to file '%1$s'", xmlFile.getAbsolutePath()) );
			return( false );
		}
		
		logWriter.logAppend( "XML export complete" );
		return( true );
	}
	
	private void exportTablesToXml( Writer fOut, String indent, SqliteConverterExportConfig config )
	{
        for( SqliteTableHandler tableHandler : tableHandlers )
        {
        	try
        	{
        		logWriter.logAppend( String.format("Exporting contents for table '%1$s'", tableHandler.getName()) );
        		tableHandler.exportTableToXml( fOut, indent, config );
        	}
        	catch( SQLException e )
        	{
        		logWriter.logAppend( String.format("SQL exception occured trying to export table '%1$s'", tableHandler.getName()) );
        	}
        	catch( IOException e )
        	{
        		logWriter.logAppend( String.format("I/O exception occured trying to export table '%1$s'", tableHandler.getName()) );
        	}
        }
	}

	private Boolean exportToJson( File jsonFile, SqliteConverterExportConfig config )
	{
		logWriter.logAppend( "JSON export commencing" );
		
		// Handle the export...
		try
		{
			FileWriter		fStream = new FileWriter( jsonFile );
			BufferedWriter	fOut	= new BufferedWriter( fStream );
			JSONObject		jsonOut	= new JSONObject();
			
			exportTablesToJson( jsonOut, config );
			fOut.write( jsonOut.toString(2) );
			
			fOut.close();
			fStream.close();
		}
		catch( IOException e )
		{
			logWriter.logAppend( String.format("I/O error encountered trying to output database to file '%1$s'", jsonFile.getAbsolutePath()) );
			return( false );
		}
		catch (JSONException e)
		{
			logWriter.logAppend( String.format("Error encoding database file '%1$s' into JSON", jsonFile.getAbsolutePath()) );
		}
		
		logWriter.logAppend( "JSON export complete" );
		return( true );
	}
	
	private void exportTablesToJson( JSONObject jsonOut, SqliteConverterExportConfig config )
	{
        for( SqliteTableHandler tableHandler : tableHandlers )
        {
        	try
        	{
        		logWriter.logAppend( String.format("Exporting contents for table '%1$s'", tableHandler.getName()) );
        		tableHandler.exportTableToJson( jsonOut, config );
        	}
        	catch( SQLException e )
        	{
        		logWriter.logAppend( String.format("SQL exception occured trying to export table '%1$s'", tableHandler.getName()) );
        	}
        }
	}
}
