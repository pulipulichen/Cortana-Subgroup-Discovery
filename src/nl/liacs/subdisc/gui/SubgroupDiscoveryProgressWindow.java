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
		itsContainer.removeAll();
		
		final JPanel aProgressPanel = new JPanel();
		
		if (jLabelProgress == null) {
			jLabelProgress = new JLabel("Loading...");
		}
		jLabelProgress.setFont(GUI.DEFAULT_TEXT_FONT);
		aProgressPanel.add(jLabelProgress);
		
		itsContainer.add(aProgressPanel, BorderLayout.NORTH);
		
		// -------------------

		final JPanel aButtonPanel = new JPanel();

		if (itsCloseButton == null) {
			itsCloseButton = GUI.buildButton("stop", 'C', "stop", this);
		}
		aButtonPanel.add(itsCloseButton);
		
		itsContainer.add(aButtonPanel, BorderLayout.SOUTH);

		GUI.focusComponent(itsCloseButton, this);
	}
	
	public void start() {
		jLabelProgress.setText("Loading...");
		//initComponents();
		itsCloseButton.setText("stop");
		itsCloseButton.setEnabled(true);

		try {
			Thread.sleep(10);
		} catch (Exception e) {}
		
		setVisible(true);
	}
	
	public void stop() {
		setVisible(false);
	}
	
	public void setSubgroupDiscoveryTask(SubgroupDiscovery aSubgroupDiscovery) {
		this.itsSubgroupDiscovery = aSubgroupDiscovery;
	}
	
	private String itsProgress;
	
	public void setProgress(SubgroupDiscovery aSubgroupDiscovery, String theProgress) {

		itsSubgroupDiscovery = aSubgroupDiscovery;
		itsProgress = theProgress;
		
		SwingUtilities.invokeLater(new Runnable() {

	        @Override
	        public void run() {
	        	jLabelProgress.setText(itsProgress);
	        }

	    });
		
		//Log.logCommandLine("setProgress(): [" + theProgress + "]");
		
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
