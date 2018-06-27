package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.text.*;

import nl.liacs.subdisc.*;

/**
 * A BrowseWindow contains a {@link BrowseJTable BrowseJTable} that shows all
 * data in a {@link Table Table}, which in turn is read from a <code>File</code>
 * or database. For each {@link Column Column}, the header displays both the
 * name and the number of distinct values.
 */
public class DistributionWindow extends JFrame implements ActionListener//, MouseListener
{
	private static final long serialVersionUID = 1L;
	private Table itsTable;
	private DistributionTable itsBrowseJTable;
	private BitSet itsSubgroupMembers;	// for Table.save()
	private JComboBox itsColumnsBox;
	// problematic in case of double columnNames
	private Map<String, Integer> itsMap;
	
	// -2st -1.5st -1st -0.5st avg +0.5st +1st +1.5st +2st q0 q0.2 q0.25 q0.4 q0.5 q0.6 q0.75 q0.8 q1  
	private int itsRowNumber = 18;

	public DistributionWindow(Table theTable)
	{
		if (theTable == null)
		{
			Log.logCommandLine(
				"BrowseWindow Constructor: theTable can not be 'null'.");
			return;
		}
		else
		{
			calcDistributionTable(theTable);
			initComponents(null);

			setTitle("Distribution for: " + itsTable.getName());
			
			setIconImage(MiningWindow.ICON);
			setSize(GUI.WINDOW_DEFAULT_SIZE);
			
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
			//setLocation(100, 100);
			
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		}
	}
	
	public DistributionWindow(Table theTable, String theColumn)
	{
		if (theTable == null)
		{
			Log.logCommandLine(
				"BrowseWindow Constructor: theTable can not be 'null'.");
			return;
		}
		else
		{
			calcDistributionTable(theTable);
			initComponents(null);

			setTitle("Distribution for: " + itsTable.getName());

			setIconImage(MiningWindow.ICON);
			setLocation(100, 100);
			setSize(GUI.WINDOW_DEFAULT_SIZE);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
			

			// select the column
			itsColumnsBox.setSelectedItem(theColumn);
			updateItsColumnsBox();
		}
	}
	
	private Table calcDistributionTable(Table theTable) {
		// Rebuild theTable
		itsTable = new Table(theTable.getSource(), theTable.getName(), itsRowNumber, theTable.getNrColumns()+1);
		
		ArrayList<Column> columns = itsTable.getColumns();
		Column aDistributionIndexColumn = new Column("DistributionIndex",
				null,
				AttributeType.NOMINAL,
				0,
				itsRowNumber);
		aDistributionIndexColumn.add("Avg. -2 StDev.");
		aDistributionIndexColumn.add("Avg. -1.5 StDev.");
		aDistributionIndexColumn.add("Avg. -1 StDev.");
		aDistributionIndexColumn.add("Avg. -0.5 StDev.");
		aDistributionIndexColumn.add("Avg.");
		aDistributionIndexColumn.add("Avg. +0.5 StDev.");
		aDistributionIndexColumn.add("Avg. +1 StDev.");
		aDistributionIndexColumn.add("Avg. +1.5 StDev.");
		aDistributionIndexColumn.add("Avg. +2 StDev.");
		aDistributionIndexColumn.add("Percentile 0 (Min)");
		aDistributionIndexColumn.add("Percentile 20");
		aDistributionIndexColumn.add("Percentile 25 (Q1)");
		aDistributionIndexColumn.add("Percentile 40");
		aDistributionIndexColumn.add("Percentile 50 (Median)");
		aDistributionIndexColumn.add("Percentile 60");
		aDistributionIndexColumn.add("Percentile 75 (Q3)");
		aDistributionIndexColumn.add("Percentile 80");
		aDistributionIndexColumn.add("Percentile 100 (Max)");
		columns.add(aDistributionIndexColumn);
		
		return itsTable;
	}
	
	private void initComponents(Subgroup theSubgroup)
	{
		itsBrowseJTable = new DistributionTable(itsTable, null);
		//itsBrowseJTable.addMouseListener(this);
		getContentPane().add(new JScrollPane(itsBrowseJTable), BorderLayout.CENTER);

		final JPanel aButtonPanel = new JPanel();

		int aNrColumns = itsTable.getNrColumns();
		// TODO HistogramWindow uses a similar list, sync?
		itsMap = new LinkedHashMap<String, Integer>(aNrColumns);
		for (int i = 0, j = aNrColumns; i < j; ++i)
		{
			Column aColumn = itsTable.getColumn(i);
			itsMap.put(aColumn.getName(), aColumn.getIndex());
		}
		itsColumnsBox = GUI.buildComboBox(itsMap.keySet().toArray(), this);
		itsColumnsBox.setEditable(true);
		itsColumnsBox.setPreferredSize(GUI.BUTTON_DEFAULT_SIZE);
		aButtonPanel.add(itsColumnsBox);

		// always allow saving, useful if data is modified
		aButtonPanel.add(GUI.buildButton("Save", 'S', "save", this));

		// by default focus is on first (condition-)attribute
		if (theSubgroup == null)
			itsColumnsBox.setSelectedIndex(0);
		else
		{
			itsSubgroupMembers = theSubgroup.getMembers();
			String aName = theSubgroup.getConditions().get(0).getColumn().getName();
			((JTextComponent)itsColumnsBox.getEditor().getEditorComponent()).setText(aName);
			itsColumnsBox.setSelectedItem(aName);
		}

		final JButton aCloseButton = GUI.buildButton("Close", 'C', "close", this);
		aButtonPanel.add(aCloseButton);
		getContentPane().add(aButtonPanel, BorderLayout.SOUTH);

		GUI.focusComponent(aCloseButton, this);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anEvent = theEvent.getActionCommand();
//		System.out.println(anEvent);

		// relies on only one comboBox being present
		if("comboBoxEdited".equals(anEvent))
			;//comboBoxCheck();
		else if("comboBoxChanged".equals(anEvent))
			updateItsColumnsBox();
		else if ("save".equals(anEvent))
			itsTable.toFile(itsSubgroupMembers);
		else if ("close".equals(anEvent))
			dispose();
	}

	private void updateItsColumnsBox()
	{
		String aText = ((JTextComponent)itsColumnsBox.getEditor().getEditorComponent()).getText();

		if (itsMap.containsKey(aText))
			itsBrowseJTable.focusColumn(itsMap.get(aText));
		else
		{
			String aNewText = aText.toLowerCase();
			itsColumnsBox.removeActionListener(this);
			itsColumnsBox.setSelectedIndex(-1);
			itsBrowseJTable.clearSelection();
			itsColumnsBox.removeAllItems();

			// special case (aText == ""), true for all items, resets full list
			for (Entry<String, Integer> e : itsMap.entrySet())
				if (e.getKey().toLowerCase().startsWith(aNewText))
					itsColumnsBox.addItem(e.getKey());

			if (itsColumnsBox.getItemCount() > 0)
			{
				itsColumnsBox.setSelectedIndex(0);
				itsBrowseJTable.focusColumn(itsMap.get(itsColumnsBox.getItemAt(0)));
				//((JComponent)itsColumnsBox.getEditor().getEditorComponent()).setForeground(getForeground());
			}
			else
			{
				((JTextComponent)itsColumnsBox.getEditor().getEditorComponent()).setText(aText);
				// if no column startsWith aText, colour aText
				//((JTextField)itsColumnsBox.getEditor().getEditorComponent()).setForeground(GUI.RED);
			}
			itsColumnsBox.addActionListener(this);
		}
	}
}
