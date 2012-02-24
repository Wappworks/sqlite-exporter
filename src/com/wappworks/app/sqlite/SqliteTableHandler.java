/* 
 * SQLite Table Handler
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.app.sqlite;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wappworks.common.log.LogWriter;

public class SqliteTableHandler
{
	private enum FieldType
	{
		INTEGER,
		FLOAT,
		STRING
	};
	
	private LogWriter						logWriter;
	private Connection						dbConn;
	private String 							tableName;
	private Hashtable<String, FieldType>	fieldInfo;
	
	public SqliteTableHandler( String inTableName, Connection inDbConn, LogWriter inLogWriter ) throws SQLException
	{
		logWriter = inLogWriter;
		dbConn = inDbConn;
		tableName = inTableName;
		fieldInfo = new Hashtable<String, FieldType>();
		
		logWriter.logAppend( String.format( "Reading column names for table '%1$s'", tableName) );
		
		Statement dbStat = dbConn.createStatement();
	    ResultSet rs = dbStat.executeQuery( String.format("PRAGMA table_info('%1$s');", tableName) );
	    try
	    {
		    while( rs.next() )
		    {
		    	fieldInfo.put( rs.getString("name"), fieldTypeStringToFieldType(rs.getString("type")) );
		    }
	    }	
	    finally
	    {
	    	rs.close();
	    }
	}
	
	public Boolean exportTableToXml( Writer fOut, String indent, SqliteConverterExportConfig config ) throws SQLException, IOException
	{
		List<String> excludeList = config.getExcludeList();
    	if( excludeList.contains( tableName ) )
    		return( true );
		if( excludeList.size() == 0 )
			excludeList = null;
    	
		fOut.write( String.format( "%1$s<%2$s>\n", indent, tableName) );

		String recordIndent = indent + "\t";
		exportRecordsToXml( fOut, recordIndent, excludeList );
		
		fOut.write( String.format( "%1$s</%2$s>\n", indent, tableName) );
		return( true );
	}
	
	private void exportRecordsToXml( Writer fOut, String indent, List<String> excludeList ) throws SQLException, IOException
	{
		Statement dbStat = dbConn.createStatement();
	    ResultSet rs = dbStat.executeQuery( String.format("select * from '%1$s';", tableName) );
	    try
	    {
		    while( rs.next() )
		    {
				fOut.write( String.format( "%1$s<%2$sRecord>\n", indent, tableName) );
				for( Map.Entry<String, FieldType> entry : fieldInfo.entrySet() )
				{
					String fieldName = entry.getKey();
					
					if( excludeList != null )
					{
						String fieldRefName = tableName + "." + fieldName;
						if( excludeList.contains( fieldRefName ) )
							continue;
					}

					fOut.write( String.format("%1$s\t<%2$s>%3$s</%2$s>\n", indent, fieldName, rs.getString( fieldName )) );
				}
				fOut.write( String.format( "%1$s</%2$sRecord>\n", indent, tableName) );
		    }
	    }	
	    finally
	    {
	    	rs.close();
	    }
	}
	
	public Boolean exportTableToJson( JSONObject jsonOut, SqliteConverterExportConfig config ) throws SQLException
	{
		List<String> excludeList = config.getExcludeList();
    	if( excludeList.contains( tableName ) )
    		return( true );
		if( excludeList.size() == 0 )
			excludeList = null;
		
		String primaryKey = config.getTablePrimaryKeys().get( tableName );
		if( primaryKey != null && !fieldInfo.containsKey(primaryKey) )
			primaryKey = null;
		
		Object jsonRecords = null;
		if( primaryKey == null )
			jsonRecords = getRecordsAsJsonArray( excludeList );
		else
			jsonRecords = getRecordsAsJsonObject( excludeList, primaryKey );
		
		if( jsonRecords != null )
		{
			try
			{
				jsonOut.put(tableName, jsonRecords);
			}
			catch (JSONException e)
			{
				addLogText( String.format("Error adding table '%1$s' to JSON output. Skipping record...", tableName) );
			}
		}
		
		return( true );
	}
	
	private JSONArray getRecordsAsJsonArray( List<String> excludeList ) throws SQLException
	{
		Statement dbStat = dbConn.createStatement();
	    ResultSet rs = dbStat.executeQuery( String.format("select * from '%1$s';", tableName) );
	    
	    JSONArray jsonRecords = new JSONArray();
	    try
	    {
		    while( rs.next() )
		    {
		    	Boolean saveRecord = true;
		    	JSONObject jsonRecord = new JSONObject();
		    	
				for( Map.Entry<String, FieldType> entry : fieldInfo.entrySet() )
				{
					String fieldName = entry.getKey();
					
					if( excludeList != null )
					{
						String fieldRefName = tableName + "." + fieldName;
						if( excludeList.contains( fieldRefName ) )
							continue;
					}

					try
					{
						addFieldToJson( jsonRecord, rs, entry );
					}
					catch (JSONException e)
					{
						addLogText( String.format("Error adding field '%2$s' in table '%1$s' to JSON output for record [%3$d]. Skipping record...", tableName, entry.getKey(), rs.getRow()) );
						saveRecord = false;
					}
				}
				
				if( saveRecord )
					jsonRecords.put( jsonRecord );
		    }
	    }	
	    finally
	    {
	    	rs.close();
	    }
	    
	    return( jsonRecords );
	}
	
	private JSONObject getRecordsAsJsonObject( List<String> excludeList, String primaryKey ) throws SQLException
	{
		Statement dbStat = dbConn.createStatement();
	    ResultSet rs = dbStat.executeQuery( String.format("select * from '%1$s';", tableName) );
	    
	    JSONObject	jsonRecords = new JSONObject();
	    int			recordIndex = 0;
	    try
	    {
		    while( rs.next() )
		    {
		    	Boolean saveRecord = true;
		    	JSONObject jsonRecord = new JSONObject();
		    	
				for( Map.Entry<String, FieldType> entry : fieldInfo.entrySet() )
				{
					String fieldName = entry.getKey();
					
					if( fieldName.equals(primaryKey) )
						continue;
					
					if( excludeList != null )
					{
						String fieldRefName = tableName + "." + fieldName;
						if( excludeList.contains( fieldRefName ) )
							continue;
					}

					try
					{
						addFieldToJson( jsonRecord, rs, entry );
					}
					catch (JSONException e)
					{
						addLogText( String.format("Error adding field '%2$s' in table '%1$s' to JSON output for record [%3$d]. Skipping record...", tableName, entry.getKey(), rs.getRow()) );
						saveRecord = false;
					}
				}
				
				if( saveRecord )
				{
					String recordName = rs.getString( primaryKey );
					if( recordName.length() <= 0 )
						recordName = "undef_" + recordIndex;
					if( jsonRecords.has( recordName ) )
						recordName += "_dupe_" + recordIndex;
					
					try
					{
						jsonRecords.put( recordName, jsonRecord );
					}
					catch (JSONException e)
					{
						addLogText( String.format("Error adding record '%2$s' in table '%1$s' to JSON output. Skipping record...", tableName, recordName) );
					}
				}
				
				recordIndex++;
		    }
	    }	
	    finally
	    {
	    	rs.close();
	    }
	    
	    return( jsonRecords );
		/*
		JSONObject jsonRecords = null;
		ISqlJetCursor cursor = tableData.open();
		int recordIndex = 0;
		
		try
		{
			if( !cursor.eof() )
			{
				jsonRecords = new JSONObject();
				
				do
				{
					try
					{
						JSONObject jsonRecordCurr = new JSONObject();
						boolean skipExport = false;
						for( String fieldName : fieldNames )
						{
							if( fieldName.equals(primaryKey) )
								continue;
							
							if( excludeList != null )
							{
								String fieldRefName = tableName + "." + fieldName;
								if( excludeList.contains( fieldRefName ) )
									continue;
							}
							
							SqlJetValueType cursorType = cursor.getFieldType(fieldName);
							switch( cursorType )
							{
								case INTEGER:
									jsonRecordCurr.put( fieldName, cursor.getInteger(fieldName) );
									break;
									
								case TEXT:
									jsonRecordCurr.put( fieldName, cursor.getString(fieldName) );
									break;
									
								case FLOAT:
									String fieldValueString = cursor.getString(fieldName);
									try
									{
										jsonRecordCurr.put( fieldName, Float.valueOf(fieldValueString) );
									}
									catch (NumberFormatException e)
									{
										addLogText( String.format("Encountered a bad number when exporting record %2$d in table %1$d. Problem field '%3$s' has value %4$s. Skipping record...", tableName, recordIndex, fieldName, fieldValueString) );
										skipExport = true;
									}
									break;
	
								case NULL:
									jsonRecordCurr.put( fieldName, true );
									break;
									
								default:
									// Ignore the other types...
									break;
							}
						}
						
						if( !skipExport )
						{
							String recordName = getFieldAsString( cursor, primaryKey );
							if( recordName == null )
								recordName = "undef_" + recordIndex;
							if( jsonRecords.has( recordName ) )
								recordName += "_dupe_" + recordIndex;
							
							jsonRecords.put( recordName, jsonRecordCurr );
						}
						
						recordIndex++;
					}
					catch (JSONException e) {
						addLogText( String.format("Encountered JSON exception while trying to export record %2$d in table %1$d. Skipping record...", tableName, recordIndex) );
					}
				}
				while( cursor.next() );
			}
		}
		finally
		{
			cursor.close();
		}
		
		return( jsonRecords );
		*/
	}
	
	public String getName()
	{
		return( tableName );
	}

	private void addFieldToJson( JSONObject jsonDest, ResultSet rs, Map.Entry<String, FieldType> fieldInfo ) throws JSONException, SQLException
	{
		String fieldName = fieldInfo.getKey();
		switch( fieldInfo.getValue() )
		{
			case INTEGER:
				jsonDest.put( fieldName, rs.getLong( fieldName ) );
				break;
				
			case FLOAT:
				jsonDest.put( fieldName, rs.getDouble(fieldName) );
				break;
				
			default:
				jsonDest.put( fieldName, rs.getString( fieldName ) );
				break;
		}
	}
	
	private FieldType fieldTypeStringToFieldType( String ident )
	{
		if( ident.equals("INTEGER") || ident.equals("NUMERIC") )
			return( FieldType.INTEGER );
		if( ident.equals("FLOAT") || ident.equals("REAL") )
			return( FieldType.FLOAT );
		
		return( FieldType.STRING );
	}

	private void addLogText( String logText )
	{
		logWriter.logAppend( logText );
	}
}
