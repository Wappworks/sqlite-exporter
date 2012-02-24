/* 
 * File Selection widget
 *
 * Copyright 2011 Wappworks Studios
 */
package com.wappworks.common.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.wappworks.common.log.LogWriter;

public class PanelFileSelection extends JPanel implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField				textField;
	private JButton 				button;
	private FileNameExtensionFilter	fileNameFilter;
	private boolean					isInputFile;
	private LogWriter				logWriter;
	
	private EventListenerList		eventListeners;
	private ChangeEvent				changeEvent;
	
	public PanelFileSelection( String label, FileNameExtensionFilter inFileNameFilter, boolean inIsInputFile, int textFieldColumns )
	{
		super();
		
		eventListeners = new EventListenerList();
		changeEvent = new ChangeEvent( this );
		
		logWriter = null;
		
		fileNameFilter = inFileNameFilter;
		isInputFile = inIsInputFile;
		
		textField = new JTextField();
		textField.setEditable( false );
		textField.setColumns( textFieldColumns );
		button = new JButton( label );
		button.addActionListener( this );
		add( textField );
		add( button );
	}
	
	public PanelFileSelection( String label, FileNameExtensionFilter inFileNameFilter, boolean isInputFile )
	{
		this( label, inFileNameFilter, isInputFile, 48 );
	}
	
	public void addChangeListener( ChangeListener listener )
	{
		eventListeners.add( ChangeListener.class, listener );
	}
	
	public void removeChangeListener( ChangeListener listener )
	{
		eventListeners.remove( ChangeListener.class, listener );
	}
	
	public void setLogWriter( LogWriter inLogWriter )
	{
		logWriter = inLogWriter;
	}
	
	public void setLastSelectedFile( String filePath )
	{
		textField.setText( filePath );
	}
	
	public void setLastSelectedFile( File inFile )
	{
		textField.setText( inFile.getAbsolutePath() );
	}
	
	public File getLastSelectedFile()
	{
		String filePath = textField.getText();
		if( filePath.length() <= 0 )
			return( null );
	
		return( new File(filePath) );
	}
	
	public String getLastSelectedFilePath()
	{
		return( textField.getText() );
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if( evt.getSource() == button )
		{
			handleShowFileChooser();
		}
	}
	
	private void handleShowFileChooser()
	{
		JFileChooser fileChooser = new JFileChooser();
		if( fileNameFilter != null )
			fileChooser.setFileFilter( fileNameFilter );
		
		File lastSelectedFile = getLastSelectedFile();
		if( lastSelectedFile != null )
		{
			fileChooser.setSelectedFile( lastSelectedFile );
		}
		
		// Request the import file name
		int fcResult;
		if( isInputFile )
			fcResult = fileChooser.showOpenDialog(this);
		else
			fcResult = fileChooser.showSaveDialog(this);
		
		if( fcResult != JFileChooser.APPROVE_OPTION )
		{
			return;
		}

		File fcFile = fileChooser.getSelectedFile();
		if( isInputFile && !fcFile.exists() )
		{
			addLogText( String.format("Input file '%1$s' does not exist. Ignoring...", fcFile.getAbsolutePath()) );
			return;
		}
		
		String filePath = fcFile.getAbsolutePath();
		textField.setText( filePath );
		fireChangeEvent();
	}
	
	private void fireChangeEvent()
	{
		ChangeListener[] listeners = eventListeners.getListeners( ChangeListener.class );
		for( ChangeListener currListener : listeners )
		{
			currListener.stateChanged( changeEvent );
		}
	}
	
	private void addLogText( String logText )
	{
		if( logWriter == null )
			return;
		
		logWriter.logAppend( logText );
	}
}
