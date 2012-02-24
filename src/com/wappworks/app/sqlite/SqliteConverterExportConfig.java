/* 
 * SQLite Converter Configuration Handler
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.app.sqlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SqliteConverterExportConfig
{
	List<String> excludeList;
	Map<String, String> tablePrimaryKeys;
	
	public SqliteConverterExportConfig( JSONObject jsonConfig )
	{
		init( null, null );
		add( jsonConfig );
	}
	
	public SqliteConverterExportConfig( SqliteConverterExportConfig src )
	{
		excludeList = new ArrayList<String>( src.excludeList );
		tablePrimaryKeys = new Hashtable<String, String>( src.tablePrimaryKeys );
	}
	
	public SqliteConverterExportConfig( List<String> inExcludeList, Map<String, String> inTablePrimaryKeys )
	{
		init( inExcludeList, inTablePrimaryKeys );
	}

	public SqliteConverterExportConfig( String[] inExcludeList, Map<String, String> inTablePrimaryKeys )
	{
		init( Arrays.asList( inExcludeList ), inTablePrimaryKeys );
	}
	
	public SqliteConverterExportConfig( Map<String, String> inTablePrimaryKeys )
	{
		init( null, inTablePrimaryKeys );
	}
	
	public SqliteConverterExportConfig( List<String> inExcludeList )
	{
		init( inExcludeList, null );
	}
	
	public SqliteConverterExportConfig( String[] inExcludeList )
	{
		init( Arrays.asList( inExcludeList ), null );
	}
	
	public SqliteConverterExportConfig()
	{
		init( null, null );
	}
	
	public void add( SqliteConverterExportConfig source )
	{
		tablePrimaryKeys.putAll( source.tablePrimaryKeys );
		excludeList.addAll( source.excludeList );
	}
	
	public void add( JSONObject jsonConfig )
	{
		// Import the key map information (if available)...
		JSONObject keyMap = jsonConfig.optJSONObject( "keys" );
		if( keyMap != null )
		{
			Iterator<String> keys = keyMap.keys();
			while( keys.hasNext() )
			{
				String currKey = keys.next();
				try
				{
					tablePrimaryKeys.put( currKey, keyMap.getString(currKey) );
				}
				catch (JSONException e)	{}
			}
		}
			
		// Import the filter list information (if available)...
		JSONArray excludes = jsonConfig.optJSONArray( "excludes" );
		if( excludes != null )
		{
			int excludesNum = excludes.length();
			for( int excludeIndex = 0; excludeIndex < excludesNum; excludeIndex++ )
			{
				try
				{
					String currExclude = excludes.getString( excludeIndex );
					excludeList.add( currExclude );
				}
				catch (JSONException e)	{}
			}
		}
	}
	
	public List<String> getExcludeList()
	{
		return( excludeList );
	}

	public Map<String, String> getTablePrimaryKeys()
	{
		return( tablePrimaryKeys );
	}
	
	private void init( List<String> inExcludeList, Map<String, String> inTablePrimaryKeys )
	{
		excludeList = inExcludeList;
		if( excludeList == null )
			excludeList = new ArrayList<String>();
		
		tablePrimaryKeys = inTablePrimaryKeys;
		if( tablePrimaryKeys == null )
			tablePrimaryKeys = new Hashtable<String, String>();
	}
}