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
public class SubgroupDiscoveryProgressWindow extends JFrame implements ActionListener//, MouseListener
{
	private SubgroupDiscovery itsSubgroupDiscovery;
	private JLabel jLabelProgress;
	private JButton itsCloseButton;
	
	public SubgroupDiscoveryProgressWindow()
	{
		//itsSubgroupDiscovery = aSubgroupDiscovery;
		//itsSubgroupDiscovery.setRunning(false);
		
		setTitle("Subgroup discovery running");
		
		setIconImage(MiningWindow.ICON);
		setSize(480, 100);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		//setLocation(100, 150);
		
		//setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(false);
		
		initComponents();
	}
	
	private Container itsContainer;
	
	private void initComponents()
	{
		if (itsContainer == null) {
			itsContainer = this.getContentPane();
		}
		//itsContainer.removeAll();
		
		final JPanel aProgressPanel = new JPanel();
		
		jLabelProgress = new JLabel("Loading...");
		jLabelProgress.setFont(GUI.DEFAULT_TEXT_FONT);
		aProgressPanel.add(jLabelProgress);
		
		itsContainer.add(aProgressPanel);
		
		
		
		// -------------------

		final JPanel aButtonPanel = new JPanel();

		
		itsCloseButton = GUI.buildButton("stop", 'C', "stop", this);
		aButtonPanel.add(itsCloseButton);
		
		itsContainer.add(aButtonPanel);

		GUI.focusComponent(itsCloseButton, this);
	}
	
	public void start() {
		jLabelProgress.setText("Loading...");
		initComponents();
		itsCloseButton.setText("stop");
		itsCloseButton.setEnabled(true);
		setVisible(true);
	}
	
	public void stop() {
		setVisible(false);
	}
	
	public void setSubgroupDiscoveryTask(SubgroupDiscovery aSubgroupDiscovery) {
		this.itsSubgroupDiscovery = aSubgroupDiscovery;
	}
	
	public void setProgress(SubgroupDiscovery aSubgroupDiscovery, String theProgress) {
		if (null != aSubgroupDiscovery) {
			this.itsSubgroupDiscovery = aSubgroupDiscovery;
		}
		//Log.logCommandLine("setProgress(): [" + theProgress + "]");
		jLabelProgress.setText(theProgress);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anEvent = theEvent.getActionCommand();
//		System.out.println(anEvent);

		// relies on only one comboBox being present
		if ("stop".equals(anEvent)) {
			if (itsSubgroupDiscovery != null) {
				this.itsSubgroupDiscovery.setRunning(false);
			}
			itsCloseButton.setText("Please wait");
			itsCloseButton.setEnabled(false);
			//dispose();
		}
	}

}
