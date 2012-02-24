/* 
 * SQLite Exporter GUI
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;
import org.json.JSONObject;

import com.wappworks.app.sqlite.SqliteConverterExportConfig;
import com.wappworks.app.sqlite.SqliteConverter.ExportFormat;
import com.wappworks.common.log.LogWriter;
import com.wappworks.common.swing.PanelFileSelection;
import com.wappworks.common.util.WappFile;

public class SqliteExporterFrame extends SqliteExporter implements ActionListener, ChangeListener
{
	private static final long serialVersionUID = 2118422283602674154L;
	
	private static final int CONFIGFILE_SIZE_MAX		= 32 * 1024;
	
	private static final String PREF_FILEPATH_DB 		= "filepath_db";
	private static final String PREF_FILEPATH_CONFIG 	= "filepath_config";
	private static final String PREF_FILEPATH_XML 		= "filepath_xml";
	private static final String PREF_FILEPATH_JSON 		= "filepath_json";

	private static final FileNameExtensionFilter 	fileFilterSqlite = new FileNameExtensionFilter( "Sqlite DB (s3db)", "s3db" );
	private static final FileNameExtensionFilter 	fileFilterConfig = new FileNameExtensionFilter( "Config (txt, conf, cfg)", "txt", "conf", "cfg" );
	private static final FileNameExtensionFilter 	fileFilterXml = new FileNameExtensionFilter( "XML (xml, txt)", "xml", "txt" );
	private static final FileNameExtensionFilter 	fileFilterJson = new FileNameExtensionFilter( "JSON (txt, js, json)", "txt", "js", "json" );

	private class LogWindowWriter implements LogWriter
	{
		private JTextArea log;
		
		public LogWindowWriter( JTextArea inLog )
		{
			log = inLog;
		}
		
		public void logAppend( String logText )
		{
			log.append( logText + "\n" );
			log.setCaretPosition( log.getDocument().getLength() );
		}
	}
	
	private JTextArea			log;
	private LogWindowWriter		logWriter;
	
	private JPanel				window;
	
	private JButton 			exportButton;
	
	private PanelFileSelection 	dbFileInSelection;
	private PanelFileSelection 	configFileInSelection;
	private PanelFileSelection 	xmlFileOutSelection;
	private PanelFileSelection 	jsonFileOutSelection;
	
	private Preferences			prefs;
	
	public static void main(String args[])
	{
        //Create and set up the window.
        JFrame frame = new JFrame("SQLite Exporter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        SqliteExporterFrame exporterFrame = new SqliteExporterFrame();
        frame.add( exporterFrame.window );
 
        //Display the window.
        frame.pack();
        centerWindow( frame );
        frame.setVisible(true);
	}
	
	static void centerWindow( JFrame frame )
	{
		// Get the size of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		 
		// Determine the new location of the window
		int w = frame.getSize().width;
		int h = frame.getSize().height;
		int x = (screenSize.width - w) / 2;
		int y = (screenSize.height - h) / 2;
		 
		// Move the window
		frame.setLocation(x, y);
	}

	SqliteExporterFrame()
	{
		super();
		
		window = new JPanel( new BorderLayout() );
		setupLogger();
		setupButtons();
		
		restorePrefs();
	}
	
	private void setupButtons()
	{
		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BoxLayout(topPanel, BoxLayout.PAGE_AXIS) );
		
		dbFileInSelection = new PanelFileSelection( "Input DB...", fileFilterSqlite, true );
		dbFileInSelection.addChangeListener( this );
		dbFileInSelection.setLogWriter( logWriter );
		topPanel.add( dbFileInSelection );
		
		configFileInSelection = new PanelFileSelection( "Config File...", fileFilterConfig, true );
		configFileInSelection.addChangeListener( this );
		configFileInSelection.setLogWriter( logWriter );
		topPanel.add( configFileInSelection );
		
		xmlFileOutSelection = new PanelFileSelection( "Output XML...", fileFilterXml, false );
		xmlFileOutSelection.addChangeListener( this );
		xmlFileOutSelection.setLogWriter( logWriter );
		topPanel.add( xmlFileOutSelection );
		
		jsonFileOutSelection = new PanelFileSelection( "Output JSON...", fileFilterJson, false );
		jsonFileOutSelection.addChangeListener( this );
		jsonFileOutSelection.setLogWriter( logWriter );
		topPanel.add( jsonFileOutSelection );
		
		exportButton = new JButton( "Sqlite Export" );
		exportButton.addActionListener( this );
		topPanel.add( exportButton );

		window.add( topPanel, BorderLayout.PAGE_START );
	}
	
	private void setupLogger()
	{
        log = new JTextArea(20,40);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        log.setLineWrap( true );
        log.setWrapStyleWord( true );
        JScrollPane logScrollPane = new JScrollPane(log);
        window.add(logScrollPane, BorderLayout.CENTER);
        
        setLogWriter( new LogWindowWriter( log ) );
	}
	
	private void restorePrefs()
	{
		prefs = Preferences.userNodeForPackage(this.getClass());
		
		dbFileInSelection.setLastSelectedFile( prefs.get(PREF_FILEPATH_DB, "") );
		configFileInSelection.setLastSelectedFile( prefs.get(PREF_FILEPATH_CONFIG, "") );
		xmlFileOutSelection.setLastSelectedFile( prefs.get(PREF_FILEPATH_XML, "") );
		jsonFileOutSelection.setLastSelectedFile( prefs.get(PREF_FILEPATH_JSON, "") );
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if( evt.getSource() == exportButton )
			handleExportGui();
	}

	@Override
	public void stateChanged(ChangeEvent evt)
	{
		if( evt.getSource() == dbFileInSelection )
		{
			addLogText( String.format("Selected SQLite file '%1$s' for import", dbFileInSelection.getLastSelectedFilePath()) );
			prefs.put( PREF_FILEPATH_DB, dbFileInSelection.getLastSelectedFilePath() );
			return;
		}
		
		if( evt.getSource() == configFileInSelection )
		{
			addLogText( String.format("Selected file '%1$s' to configure export options", configFileInSelection.getLastSelectedFilePath()) );
			prefs.put( PREF_FILEPATH_CONFIG, configFileInSelection.getLastSelectedFilePath() );
			return;
		}
		
		if( evt.getSource() == xmlFileOutSelection )
		{
			addLogText( String.format("Selected XML export target as '%1$s'", xmlFileOutSelection.getLastSelectedFilePath()) );
			prefs.put( PREF_FILEPATH_XML, xmlFileOutSelection.getLastSelectedFilePath() );
			return;
		}
		
		if( evt.getSource() == jsonFileOutSelection )
		{
			addLogText( String.format("Selected JSON export target as '%1$s'", jsonFileOutSelection.getLastSelectedFilePath()) );
			prefs.put( PREF_FILEPATH_JSON, jsonFileOutSelection.getLastSelectedFilePath() );
			return;
		}
	}
	
	public void handleExportGui()
	{
		// Set up the common export configuration
		SqliteConverterExportConfig commonConfig = new SqliteConverterExportConfig();
		JSONObject exportConfig = getExportConfig();
		if( exportConfig != null )
		{
			JSONObject jsonConfigCommon = exportConfig.optJSONObject( "common" );
			if( jsonConfigCommon != null )
				commonConfig.add( jsonConfigCommon );
		}
		
		// Begin the export
		File dbFileIn = dbFileInSelection.getLastSelectedFile();
		if( dbFileIn == null )
		{
			addLogText( "Sqlite DB file not specified. Aborting..." );
			return;
		}
		
		if( !beginExport( dbFileIn ) )
		{
			addLogText( "Cannot initiate DB export. Aborting..." );
			return;
		}
		
		File xmlFileOut = xmlFileOutSelection.getLastSelectedFile();
		if( xmlFileOut != null )
		{
			SqliteConverterExportConfig currConfig = commonConfig;
			if( exportConfig != null )
			{
				JSONObject jsonConfigCurr = exportConfig.optJSONObject( "xml" );
				if( jsonConfigCurr != null )
				{
					currConfig = new SqliteConverterExportConfig( commonConfig );
					currConfig.add( jsonConfigCurr );
				}
			}
			
			export( xmlFileOut, ExportFormat.XML, currConfig );
		}
		
		File jsonFileOut = jsonFileOutSelection.getLastSelectedFile();
		if( jsonFileOut != null )
		{
			SqliteConverterExportConfig currConfig = commonConfig;
			if( exportConfig != null )
			{
				JSONObject jsonConfigCurr = exportConfig.optJSONObject( "json" );
				if( jsonConfigCurr != null )
				{
					currConfig = new SqliteConverterExportConfig( commonConfig );
					currConfig.add( jsonConfigCurr );
				}
			}
			
			export( jsonFileOut, ExportFormat.JSON, currConfig );
		}
		
		endExport();
	}
	
	private JSONObject getExportConfig()
	{
		File configFileIn = configFileInSelection.getLastSelectedFile();
		if( configFileIn == null )
			return( null );
		
		String configString = WappFile.readFileAsString(configFileIn.getAbsolutePath(), CONFIGFILE_SIZE_MAX );
		if( configString == null )
			return( null );
		
		try
		{
			JSONObject jsonConfig = new JSONObject( configString );
			return( jsonConfig );
		}
		catch (JSONException e)
		{
			addLogText( String.format("WARNING: Ignoring configuration file '%1$s' because of invalid data: [%2$s]", configFileIn.getAbsolutePath(), e.getMessage()) );
			return( null );
		}
	}
}
