/**
 * TODO use fileNameExtension class.
 */
package nl.liacs.subdisc;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import nl.liacs.subdisc.cui.*;
import nl.liacs.subdisc.gui.*;

public class FileHandler
{
	// 20180616 預設的檔案路徑
	//private String aTestFilePath = null;
	//private String aTestFilePath = "reg.csv";
	//private String aTestFilePath = "anvoca_sm_ancova.csv";
	
	
	
	public static enum Action
	{
		OPEN_FILE, OPEN_DATABASE, SAVE
	}

	// remember the directory of the last used file, defaults to users'
	// (platform specific) home directory if the the path cannot be resolved
	private static String itsLastFileLocation = ".";

	private Table itsTable;
	private SearchParameters itsSearchParameters;
	private File itsFile;
	private String itsDefaultFileName;
	private FileType itsDefaultType;

	// Main FileHandler
	public FileHandler(Action theAction)
	{
		doAction(theAction);
	}
	
	private void doAction(Action theAction) {
		switch(theAction)
		{
			case OPEN_FILE :
			{
				showFileChooser(theAction);
				openFile();
				break;
			}
			case OPEN_DATABASE : openDatabase(); break;
			case SAVE : {
				save(FileType.ALL_DATA_FILES); 
				break;
			}
			default : break;
		}
	}
	

	// Main FileHandler
	public FileHandler(Action theAction, String theDefaultFileName)
	{
		itsDefaultFileName = theDefaultFileName;
		doAction(theAction);
	}
	

	// Main FileHandler
	public FileHandler(Action theAction, String theDefaultFileName, FileType theType)
	{
		itsDefaultFileName = theDefaultFileName;
		itsDefaultType = theType;
		doAction(theAction);
	}

	//save in a specific format
	public FileHandler(FileType theType)
	{
		save(theType);
	}

	// Add CUI domain to existing Table
	public FileHandler(Table theTable, EnrichmentType theType)
	{
		if (theTable == null)
		{
			Log.logCommandLine(
				"FileHandler Constructor: parameter can not be 'null'.");
		}
		else
		{
			itsTable = new FileLoaderGeneRank(theTable, theType).getTable();
			printLoadingInfo();
		}
	}

	// Populate Table from XML file
	public FileHandler(File theFile, Table theTable)
	{
		if (theFile == null || !theFile.exists())
		{
			ErrorLog.log(theFile, new FileNotFoundException(""));
			return;
		}
		else if (theTable == null)
		{
			Log.logCommandLine(
				"FileHandler Constructor: Table is 'null', trying normal loading.");
			openFile();
		}
		else
		{
			itsFile = theFile;
			itsTable = theTable;
			openFile();
		}
	}

	private void openFile()
	{
		Log.logCommandLine("openFile()");
		if (itsFile == null || !itsFile.exists())
		{
			ErrorLog.log(itsFile, new FileNotFoundException());
			return;
		}

		FileType aFileType = FileType.getFileType(itsFile);
		Timer aTimer = new Timer();

		JFrame aLoaderDialog = null;
		if (!GraphicsEnvironment.isHeadless() && aFileType != FileType.XML) {
			aLoaderDialog = showFileLoaderDialog(itsFile);
		}

		Log.logCommandLine("File type: " + aFileType);
		switch (aFileType)
		{
			case TXT :
			{
				// regular loading
				if (itsTable == null )
				{
					itsTable = new DataLoaderTXT(itsFile).getTable();
					//itsTable = new FileLoaderTXT(itsFile).getTable();
				}
				// load from XML, see Table(XMLNode, pathToXML)
				else
					new DataLoaderTXT(itsFile, itsTable);
					//new FileLoaderTXT(itsFile, itsTable);
				break;
			}
			case ARFF :
			{
				// regular loading
				if (itsTable == null )
					itsTable = new FileLoaderARFF(itsFile).getTable();
				// load from XML, see Table(XMLNode, pathToXML)
				else
					new FileLoaderARFF(itsFile, itsTable);
				break;
			}
			case XML :
			{
				FileLoaderXML aLoader = new FileLoaderXML(itsFile);
				itsTable = aLoader.getTable();
				itsSearchParameters = aLoader.getSearchParameters();
				//itsTable.update();
				return; // does not printLoadingInfo/ ~Time
			}
			// unknown FileType, log error
			default :
			{
				Log.logCommandLine(
					String.format("FileHandler: unknown FileType for File '%s'.",
							itsFile.getName()));
				return;
			}
		}
		Log.logCommandLine(String.format("loading time '%s': %s",
							itsFile.getPath(),
							aTimer.getElapsedTimeString()));
		printLoadingInfo();
		if (aLoaderDialog != null) {
			aLoaderDialog.setVisible(false);
		}
	}

	private void openDatabase()
	{

	}

	private JFrame showFileLoaderDialog(File theFile)
	{
		final JOptionPane aPane =
			new JOptionPane(String.format("<html>Loading file:<br><br>'%s'<br><br>(Window closes when loading completes.)</html>",
							theFile.getAbsolutePath()),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new Object[] {},
					null);

		final JFrame aFrame = new JFrame("File Loading in progress...");
		aFrame.setContentPane(aPane);
		aFrame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				aFrame.setTitle("Window closes automatically after loading completes");
			}
		});
		aFrame.setIconImage(MiningWindow.ICON);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		aFrame.setLocation(dim.width/2-aFrame.getSize().width/2, dim.height/2-aFrame.getSize().height/2);
		//aFrame.setLocation(100, 100);
		
		aFrame.pack();
		aFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		aFrame.setVisible(true);

		return aFrame;
	}

	private File save(FileType theType)
	{
		if (theType == FileType.ALL_DATA_FILES) {
			showFileChooser(Action.SAVE);
		}
		else {
			saveFileChooser(theType);
		}
		return itsFile;
	}

	private void saveFileChooser(FileType theType)
	{
		JFrame aFrame = new JFrame();
		aFrame.setIconImage(MiningWindow.ICON);
		JFileChooser aChooser = new JFileChooser(new File(itsLastFileLocation));
		aChooser.addChoosableFileFilter(new FileTypeFilter(theType));
		aChooser.setFileFilter(new FileTypeFilter(theType));

		int theOption = aChooser.showSaveDialog(aFrame);
		if (theOption == JFileChooser.APPROVE_OPTION)
		{
			itsFile = aChooser.getSelectedFile();
			itsLastFileLocation = itsFile.getParent();
		}
	}
	
	private static boolean hasDefaultLoaded = false;

	private void showFileChooser(Action theAction)
	{
		// dummy frame to pass ICON to showXXXXDialog
		// this class is no longer a JFrame (avoids HeadlessExceptions)
		JFrame aFrame = new JFrame();
		aFrame.setIconImage(MiningWindow.ICON);
		JFileChooser aChooser = new JFileChooser(new File(itsLastFileLocation));
		aChooser.addChoosableFileFilter(new FileTypeFilter(FileType.TXT));
		aChooser.addChoosableFileFilter(new FileTypeFilter(FileType.ARFF));
		aChooser.addChoosableFileFilter(new FileTypeFilter(FileType.XML));
		
		if (null != itsDefaultType) {
			aChooser.setFileFilter(new FileTypeFilter(itsDefaultType));
		}
		else {
			aChooser.setFileFilter(new FileTypeFilter(FileType.ALL_DATA_FILES));
		}
		
		if (null != itsDefaultFileName) {
			aChooser.setSelectedFile(new File(itsDefaultFileName));
		}

		String aDefaultLoadFile = ConfigIni.get("global", "DefaultLoadFile");
		if (null != aDefaultLoadFile && false == FileHandler.hasDefaultLoaded) {
			try {
				String aJarPath = new File(FileHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
				//Log.logCommandLine("FileHandle showFileChooser() error: " + aJarPath);
				//return;
				itsFile = new File(aJarPath + "/" + aDefaultLoadFile);
				if (itsFile.exists() == true) {
					itsLastFileLocation = itsFile.getParent();
					FileHandler.hasDefaultLoaded = true;
					return;
				}
			}
			catch (Exception e) {
				Log.logCommandLine("FileHandle showFileChooser() error: " + e.getMessage());
			}
		}
		
		int theOption = -1;

		if (theAction == Action.OPEN_FILE)
			theOption = aChooser.showOpenDialog(aFrame);
		else if (theAction == Action.SAVE)
			theOption = aChooser.showSaveDialog(aFrame);
		
		if (theOption == JFileChooser.APPROVE_OPTION)
		{
			itsFile = aChooser.getSelectedFile();
			Log.logCommandLine("itsFile: " + itsFile);
			itsLastFileLocation = itsFile.getParent();
			Log.logCommandLine("itsLastFileLocation: " + itsLastFileLocation);
		}
	}

	private void printLoadingInfo()
	{
		itsTable.update();

		Log.logCommandLine(
			String.format(
					"Table '%s' has %d columns and %d rows.",
					itsTable.getName(),
					itsTable.getNrColumns(),
					itsTable.getNrRows()));
	}

	/**
	 * If a <code>JFileChooser</code> dialog was shown and a <code>File</code>
	 * was selected, use this method to retrieve it.
	 *
	 * @return a <code>File</code>, or <code>null</code> if no approved
	 * selection was made.
	 */
	public File getFile() { return itsFile; };

	/**
	 * If this FileHandler successfully loaded a {@link Table Table} from a
	 * <code>File</code> or a database, use this method to retrieve it.
	 *
	 * @return the <code>Table</code> if present, <code>null</code> otherwise.
	 */
	public Table getTable() { return itsTable; };

	/**
	 * If this FileHandler successfully loaded the
	 * {@link SearchParameters SearchParameters} from a <code>File</code>, use
	 * this method to retrieve them.
	 *
	 * @return the <code>SearchParameters</code> if present, <code>null</code>
	 * otherwise.
	 */
	public SearchParameters getSearchParameters()
	{
		return itsSearchParameters;
	};
}
