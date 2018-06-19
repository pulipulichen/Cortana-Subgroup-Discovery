package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.*;

import nl.liacs.subdisc.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.title.*;
import org.jfree.data.category.*;
import org.jfree.ui.*;

public class HistogramWindow extends JFrame implements ActionListener, ChangeListener
{
	private static final long serialVersionUID = 1L;
	// do not show legend when target has many values
	private static final int TARGET_VALUES_MAX = 30;
	private static final int ATTRIBUTE_VALUES_MAX = 100;
	// use smaller font if nr columns > ATTRIBUTE_VALUES_MAX
	private static final Font SMALL_FONT = new Font("Dialog", 0, 8);
	private static final int MIN_NR_BINS = 1; // all data
	private static final int DEFAULT_NR_BINS = 8;
	private static final int MAX_NR_BINS = 32;

	private Table itsTable;
	private ChartPanel itsChartPanel;
	private JComboBox itsAttributeColumnsBox;
	private JComboBox itsTargetColumnsBox;
	private Map<?, Integer> itsAMap;
	private Map<?, Integer> itsTMap;
	private JButton itsAttributePlotButton;
	private JButton itsAttributeBrowseButton;
	private JButton itsTargetPlotButton;
	private JButton itsTargetBrowseButton;

	// TODO use configurable sliders for Numeric Attribute/Target
	private JSlider itsAttributeBinsSlider;
	private JSlider itsTargetBinsSlider;

	// TODO should be tied to parent window
	// TODO supply TargetColumn if set in MainWindow
	public HistogramWindow(Table theTable)
	{
		if (theTable == null || theTable.getNrColumns() == 0)
			return;

		itsTable = theTable;

		initComponents();

		setTitle("Histogram");
		setIconImage(MiningWindow.ICON);
		setLocation(50, 50);
		setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//pack();
		setVisible(true);
	}
	
	private JLabel jLabelAvgStDev;
	private JTextField jTextFieldAverage;
	private JTextField jTextFieldStDev;
	
	private JLabel jLabelAvgHalfStDev;
	private JTextField jTextFieldAvgMinusHalfStDev;
	private JTextField jTextFieldAvgPlusHalfStDev;
	
	private JLabel jLabelAvgOneStDev;
	private JTextField jTextFieldAvgMinusOneStDev;
	private JTextField jTextFieldAvgPlusOneStDev;
	
	private JLabel jLabelMedian;
	private JTextField jTextFieldMedian;
	private JTextField jTextFieldMedianHidden;
	
	private JLabel jLabelMinMax;
	private JTextField jTextFieldMin;
	private JTextField jTextFieldMax;
	
	private JLabel jLabelQ1Q3;
	private JTextField jTextFieldQ1;
	private JTextField jTextFieldQ3;
	
	private String[] itsColumnNames; 

	private void initComponents()
	{
		setLayout(new BorderLayout());

		// SOUTH PANEL, initialises comboBoxes used by createHistogram
		JPanel aSouthPanel = new JPanel();
		aSouthPanel.setLayout(new GridLayout(1, 2));
		// TODO create permanent 'columnName-columnIndex map' in Table?
		// map can also be used directly by MiningWindow/ BrowseWindow
		String[] sa = new String[itsTable.getNrColumns()];
		for (int i = 0, j = itsTable.getNrColumns(); i < j; ++i) {
			sa[i] = itsTable.getColumn(i).getName();
		}
		itsColumnNames = sa;
		
		JPanel anAttributeGridPanel = new JPanel();
		anAttributeGridPanel.setLayout(new GridLayout(2, 1));
		aSouthPanel.add(anAttributeGridPanel);
		

		// ATTRIBUTE PANEL
		JPanel anAttributePanel = new JPanel();
		anAttributePanel.setLayout(new BoxLayout(anAttributePanel, BoxLayout.Y_AXIS));
		anAttributePanel.setBorder(GUI.buildBorder("Select Attribute"));
		itsAttributeColumnsBox = GUI.buildComboBox(sa, this);
		itsAttributeColumnsBox.setPreferredSize(GUI.BUTTON_DEFAULT_SIZE);
		anAttributePanel.add(itsAttributeColumnsBox);
		setupSlider(itsAttributeBinsSlider = new JSlider());
		anAttributePanel.add(itsAttributeBinsSlider);
		
		JPanel anAttributeButtonGridPanel = new JPanel();
		anAttributeButtonGridPanel.setLayout(new GridLayout(1, 2));
		anAttributePanel.add(anAttributeButtonGridPanel);
		
		itsAttributePlotButton = GUI.buildButton("Plot Attribute", 'A', "attributeplot", this);
		anAttributeButtonGridPanel.add(itsAttributePlotButton);

		itsAttributeBrowseButton = GUI.buildButton("Browse Attribute", 'B', "attributebrowse", this);
		anAttributeButtonGridPanel.add(itsAttributeBrowseButton);
		
		anAttributeGridPanel.add(anAttributePanel);

		// TARGET PANEL (duplicate code)
		JPanel aTargetPanel = new JPanel();
		aTargetPanel.setLayout(new BoxLayout(aTargetPanel, BoxLayout.Y_AXIS));
		aTargetPanel.setBorder(GUI.buildBorder("Select Target"));
		itsTargetColumnsBox = GUI.buildComboBox(sa, this);
		itsTargetColumnsBox.setPreferredSize(GUI.BUTTON_DEFAULT_SIZE);
		aTargetPanel.add(itsTargetColumnsBox);
		setupSlider(itsTargetBinsSlider = new JSlider());
		aTargetPanel.add(itsTargetBinsSlider);
		
		JPanel aTargetButtonGridPanel = new JPanel();
		aTargetButtonGridPanel.setLayout(new GridLayout(1, 2));
		aTargetPanel.add(aTargetButtonGridPanel);
		
		itsTargetPlotButton = GUI.buildButton("Plot Target", 'T', "targetplot", this);
		aTargetButtonGridPanel.add(itsTargetPlotButton);
		
		itsTargetBrowseButton = GUI.buildButton("Browse Target", 'B', "targetbrowse", this);
		aTargetButtonGridPanel.add(itsTargetBrowseButton);
		
		
		anAttributeGridPanel.add(aTargetPanel);

		// MISC PANEL
		JPanel aMiscPanel = new JPanel();
		//aMiscPanel.setLayout(new BoxLayout(aMiscPanel, BoxLayout.Y_AXIS));
		aMiscPanel.setLayout(new GridLayout(7, 3));
		aMiscPanel.setBorder(GUI.buildBorder("Distribution"));

		jLabelAvgStDev = initJLabel("Avg. / StDev.");
		aMiscPanel.add(jLabelAvgStDev);
		jTextFieldAverage = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldAverage);
		jTextFieldStDev = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldStDev);
		
		jLabelAvgHalfStDev = initJLabel("Avg. กำ 0.5 StDev.");
		aMiscPanel.add(jLabelAvgHalfStDev);
		jTextFieldAvgMinusHalfStDev = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldAvgMinusHalfStDev);
		jTextFieldAvgPlusHalfStDev = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldAvgPlusHalfStDev);
		
		jLabelAvgOneStDev = initJLabel("Avg. กำ 1 StDev.");
		aMiscPanel.add(jLabelAvgOneStDev);
		jTextFieldAvgMinusOneStDev = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldAvgMinusOneStDev);
		jTextFieldAvgPlusOneStDev = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldAvgPlusOneStDev);
		
		jLabelMedian = initJLabel("Median");
		aMiscPanel.add(jLabelMedian);
		jTextFieldMedian = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldMedian);
		jTextFieldMedianHidden = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldMedianHidden);
		jTextFieldMedianHidden.setVisible(false);
		
		jLabelMinMax = initJLabel("Min / Max");
		aMiscPanel.add(jLabelMinMax);
		jTextFieldMin = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldMin);
		jTextFieldMax = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldMax);
		
		jLabelQ1Q3 = initJLabel("Q1 / Q3");
		aMiscPanel.add(jLabelQ1Q3);
		jTextFieldQ1 = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldQ1);
		jTextFieldQ3 = GUI.buildTextField("0");
		aMiscPanel.add(jTextFieldQ3);
		
		//aMiscPanel.add(GUI.buildButton("Save", "save", this));
		//aMiscPanel.add(GUI.buildButton("Print", "print", this));
		aMiscPanel.add(GUI.buildButton("CrossTable...", 'R', "crosstable", this));
		
		JButton aButton = GUI.buildButton("Close", 'C', "close", this);
		GUI.focusComponent(aButton, this);
		aMiscPanel.add(aButton);
		
		aSouthPanel.add(aMiscPanel);
		
		add(aSouthPanel, BorderLayout.SOUTH);

		// MAPS
		updateMap(true);
		updateMap(false);
		// CHART PANEL
		itsChartPanel = new ChartPanel(null);
		updateChartPanel();
		add(new JScrollPane(itsChartPanel), BorderLayout.CENTER);
	}
	
	// TODO MM GUI.buildLabel(), link Label and Field
	private static JLabel initJLabel(String theName)
	{
		JLabel aJLable = new JLabel(theName);
		aJLable.setFont(GUI.DEFAULT_TEXT_FONT);
		return aJLable;
	}

	private void setupSlider(JSlider theSlider) {
		theSlider.setMinimum(MIN_NR_BINS);
		theSlider.setMaximum(MAX_NR_BINS);
		theSlider.setValue(DEFAULT_NR_BINS);
		//theSlider.setMajorTickSpacing(8);
		theSlider.setMinorTickSpacing(1);
		theSlider.setPaintTicks(true);
		theSlider.setPaintLabels(true);
		// labels (within range of MIN-MAX)
		Hashtable<Integer, JLabel> aTable = new Hashtable<Integer, JLabel>();
		aTable.put(new Integer(1), new JLabel(String.valueOf(1)));
		aTable.put(new Integer(8), new JLabel(String.valueOf(8)));
		aTable.put(new Integer(16), new JLabel(String.valueOf(16)));
		aTable.put(new Integer(24), new JLabel(String.valueOf(24)));
		aTable.put(new Integer(32), new JLabel(String.valueOf(32)));
		theSlider.setLabelTable(aTable);
		theSlider.setFont(GUI.DEFAULT_BUTTON_FONT);
		theSlider.addChangeListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anEvent = theEvent.getActionCommand();

		if ("comboBoxChanged".equals(anEvent))
			update(itsAttributeColumnsBox.equals(theEvent.getSource()));
		else if ("crosstable".equals(anEvent))
			new CrossTableWindow(((CategoryPlot)itsChartPanel.getChart().getPlot()).getDataset());
		else if ("attributeplot".equals(anEvent))
			new PlotWindow(itsTable.getColumn(itsAttributeColumnsBox.getSelectedIndex()));
		else if ("attributebrowse".equals(anEvent))
			new BrowseWindow(itsTable, null, (String) itsAttributeColumnsBox.getSelectedItem());
		else if ("targetplot".equals(anEvent))
			new PlotWindow(itsTable.getColumn(itsTargetColumnsBox.getSelectedIndex()));
		else if ("targetbrowse".equals(anEvent))
			new BrowseWindow(itsTable, null, (String) itsTargetColumnsBox.getSelectedItem());
		else if ("close".equals(anEvent))
			dispose();
	}

	@Override
	public void stateChanged(ChangeEvent theEvent)
	{
		if (!((JSlider)theEvent.getSource()).getValueIsAdjusting())
			// assumes only 2 sources exist (Attribute and Target slider)
			update(theEvent.getSource() == itsAttributeBinsSlider);
	}

	// start point for updating
	private void update(boolean isAttributeChanged)
	{
		updateMap(isAttributeChanged);
		updateChartPanel();
	}

	// NOTE BINARY uses String for now, allows for uniform createDataset() code
	// with 4 use cases, instead of 9 (NOMINAL, NUMERIC, BINARY)^2
	private void updateMap(boolean isAttributeChanged)
	{
		JComboBox aBox = isAttributeChanged ? itsAttributeColumnsBox : itsTargetColumnsBox;
		Column aColumn = itsTable.getColumn(aBox.getSelectedIndex());
		AttributeType aType = aColumn.getType();

		if (isAttributeChanged) {
			itsAttributeBinsSlider.setEnabled(aType == AttributeType.NUMERIC);
			itsAttributePlotButton.setEnabled(aType == AttributeType.NUMERIC);

			switch (aType) {
				case NOMINAL : itsAMap = new LinkedHashMap<String, Integer>(); break;
				case ORDINAL : break;	// no use case yet
				case NUMERIC : itsAMap = new LinkedHashMap<Float, Integer>(); break;
				case BINARY : itsAMap = new LinkedHashMap<Boolean, Integer>(); break;
				default : {
					unknownAttributeType("updateMap", aType);
					return;
				}
			}
		}
		else {
			itsTargetBinsSlider.setEnabled(aType == AttributeType.NUMERIC);
			itsTargetPlotButton.setEnabled(aType == AttributeType.NUMERIC);

			switch (aType) {
				case NOMINAL : itsTMap = new LinkedHashMap<String, Integer>(); break;
				case ORDINAL : break;	// no use case yet
				case NUMERIC : itsTMap = new LinkedHashMap<Float, Integer>(); break;
				case BINARY : itsTMap = new LinkedHashMap<Boolean, Integer>(); break;
				default : {
					unknownAttributeType("updateMap", aType);
					return;
				}
			}
		}

		@SuppressWarnings("unchecked")
		Map<? super Object, Integer> aMap =
			(Map<? super Object, Integer>) (isAttributeChanged ? itsAMap : itsTMap);

		switch (aType) {
			case NOMINAL : {
				// all possible attribute values are stored in an (ordered) Map
				Iterator<String> anIterator = aColumn.getDomain().iterator();
				int idx = -1;
				while (anIterator.hasNext()) {
					aMap.put(anIterator.next(), ++idx);
				}
				resetDistribution();
				break;
			}
			case ORDINAL: {
				// no use case yet
				resetDistribution();
				break;
			}
			case NUMERIC : {
				safariiHisto(aColumn, aMap);
				setDistribution(aColumn, aMap);
				break;
			}
			case BINARY : {
				// NOTE map is not used for count-mapping in createDataset
				// but order [0, 1] is still essential
				aMap.put(Boolean.FALSE, 0);
				aMap.put(Boolean.TRUE, 1);
				resetDistribution();
				break;
			}
			default : {
				unknownAttributeType("updateMap", aType);
				resetDistribution();
				return;
			}
		}
	}

	private void updateChartPanel()
	{
		final CategoryDataset aDataset = createDataset();

		// TODO create GUI toggle
		boolean showLegend = aDataset.getRowCount() < TARGET_VALUES_MAX;

		final JFreeChart aChart =
			ChartFactory.createStackedBarChart(null, // no title
												null, // no x-axis label
												"", // no y-axis label
												aDataset,
												PlotOrientation.VERTICAL,
												showLegend,
												true,
												false);

		final CategoryPlot aPlot = aChart.getCategoryPlot();
		aPlot.setBackgroundPaint(Color.WHITE);
		aPlot.setRangeGridlinePaint(Color.BLACK);
		aPlot.getDomainAxis().setTickMarkStroke(new BasicStroke(0.1f));
		aPlot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		final BarRenderer aRenderer = (BarRenderer)aPlot.getRenderer();
		aRenderer.setBarPainter(new StandardBarPainter());
		aRenderer.setShadowVisible(false);


		int aNrColumns = aDataset.getColumnCount();
		if (aNrColumns > ATTRIBUTE_VALUES_MAX)
		{
			if (showLegend)
			{
				final LegendTitle aLegend = aChart.getLegend();
				aLegend.setFrame(new LineBorder(Color.BLACK, new BasicStroke(0), new RectangleInsets()));
				aLegend.setItemFont(SMALL_FONT);
			}

			aPlot.setRangeGridlinesVisible(false);
			aPlot.getDomainAxis().setTickLabelsVisible(false);
			//aDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
			aPlot.getRangeAxis().setTickLabelFont(SMALL_FONT);
	}

		itsChartPanel.setPreferredSize(new Dimension(aNrColumns*20, 300));
		itsChartPanel.setChart(aChart);
		itsChartPanel.revalidate();
	}

	// relies on up-to-date itsAMap and itsTMap
	private CategoryDataset createDataset()
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Column a = itsTable.getColumn(itsAttributeColumnsBox.getSelectedIndex());
		Column t = itsTable.getColumn(itsTargetColumnsBox.getSelectedIndex());
		int[][] counts = new int[itsAMap.size()][itsTMap.size()];

		// 16 possibilities (NOMINAL, ORDINAL, NUMERIC, BINARY)^2
		switch (a.getType()) {
			// Attribute NOMINAL
			case NOMINAL : {
				switch (t.getType()) {
					case NOMINAL : {
						// loops over all values in Attribute Column
						// for each possible Attribute value the corresponding Target value
						// count is incremented
						// uses aMap and tMap for fast indexing (O(1))
						// (compared to linear array/ TreeSet lookups (O(n)))
						for (int i = 0, j = a.size(); i < j; ++i) {
							++counts[itsAMap.get(a.getNominal(i))][itsTMap.get(t.getNominal(i))];
						}
						
						int aMax = 0;
						for (int i = 0; i < counts.length; i++) {
							for (int j = 0; j < counts[i].length; j++) {
								if (counts[i][j] > aMax) {
									aMax = counts[i][j]; 
								}
							}
						}
						
						jLabelAvgStDev.setText("Mode");
						jLabelAvgStDev.setVisible(true);
						jTextFieldAverage.setText("" + aMax);
						jTextFieldAverage.setVisible(true);
						break;
					}
					case ORDINAL : {
						unknownAttributeType("createDataset", t.getType());
						break;
					}
					case NUMERIC : {
						Float[] tBins = itsTMap.keySet().toArray(new Float[0]);

						for (int i = 0, j = a.size(); i < j; ++i) {
							// binarySearch could be more efficient
							int ti = Arrays.binarySearch(tBins, t.getFloat(i));
							++counts[itsAMap.get(a.getNominal(i))][ti < 0 ? -ti-1: ti];
						}
						break;
					}
					case BINARY : {
						for (int i = 0, j = a.size(); i < j; ++i)
							// NOTE order of Boolean map should be [0, 1]
							++counts[itsAMap.get(a.getNominal(i))][t.getBinary(i) ? 1 : 0];
						break;
					}
					default : {
						unknownAttributeType("createDataset", t.getType());
						break;
					}
				}
				break;
			}
			// Attribute ORDINAL
			case ORDINAL : {
				unknownAttributeType("createDataset", a.getType());
				break;
			}
			// Attribute NUMERIC
			case NUMERIC : {
				Float[] aBins = itsAMap.keySet().toArray(new Float[0]);

				switch (t.getType()) {
					case NOMINAL : {
						for (int i = 0, j = a.size(); i < j; ++i) {
							int ai = Arrays.binarySearch(aBins, a.getFloat(i));
							++counts[ai < 0 ? -ai-1: ai][itsTMap.get(t.getNominal(i))];
						}
						break;
					}
					case ORDINAL : {
						unknownAttributeType("createDataset", t.getType());
						break;
					}
					case NUMERIC : {
						Float[] tBins = itsTMap.keySet().toArray(new Float[0]);

						for (int i = 0, j = a.size(); i < j; ++i) {
							int ai = Arrays.binarySearch(aBins, a.getFloat(i));
							int ti = Arrays.binarySearch(tBins, t.getFloat(i));
							++counts[ai < 0 ? -ai-1: ai][ti < 0 ? -ti-1: ti];
						}
						break;
					}
					case BINARY : {
						for (int i = 0, j = a.size(); i < j; ++i) {
							int ai = Arrays.binarySearch(aBins, a.getFloat(i));
							++counts[ai < 0 ? -ai-1: ai][t.getBinary(i) ? 1 : 0];
						}
						break;
					}
					default : {
						unknownAttributeType("createDataset", t.getType());
						break;
					}
				}
				break;
			}
			// Attribute BINARY
			case BINARY : {
				switch (t.getType()) {
					case NOMINAL : {
						for (int i = 0, j = a.size(); i < j; ++i)
							++counts[a.getBinary(i) ? 1 : 0][itsTMap.get(t.getNominal(i))];
						break;
					}
					case ORDINAL : {
						unknownAttributeType("createDataset", t.getType());
						break;
					}
					case NUMERIC : {
						Float[] tBins = itsTMap.keySet().toArray(new Float[0]);

						for (int i = 0, j = a.size(); i < j; ++i) {
							// binarySearch could be more efficient
							int ti = Arrays.binarySearch(tBins, t.getFloat(i));
							++counts[a.getBinary(i) ? 1 : 0][ti < 0 ? -ti-1: ti];
						}
						break;
					}
					case BINARY : {
						for (int i = 0, j = a.size(); i < j; ++i)
							++counts[a.getBinary(i) ? 1 : 0][t.getBinary(i) ? 1 : 0];
						break;
					}
					default : {
						unknownAttributeType("createDataset", t.getType());
						break;
					}
				}
				break;
			}
			// Attribute unknown
			default : {
				unknownAttributeType("createDataset", a.getType());
				break;
			}
		}

		for (Entry<?, Integer> ae : itsAMap.entrySet())
			for (Entry<?, Integer> te : itsTMap.entrySet())
				dataset.addValue(counts[ae.getValue()][te.getValue()], te.getKey().toString(), ae.getKey().toString());

		return dataset;
	}

	private void unknownAttributeType(String theSource, AttributeType theType) {
		Log.logCommandLine(String.format("%s.%s(), unknown AttributeType: '%s'",
											getClass().getSimpleName(),
											theSource,
											theType));
	}

	// NOTE not save for overflow/ NaN
	private void safariiHisto(Column theColumn, Map<? super Object, Integer> theMap)
	{
		// assumes only 2 maps exist (Attribute and Target map)
		int aNrBins = (theMap == itsAMap ? itsAttributeBinsSlider.getValue() :
											itsTargetBinsSlider.getValue());
			
		float aSum = 0.0f;
		for (int i = 0, j = theColumn.size(); i < j; ++i) {
			aSum += theColumn.getFloat(i);
		}
		float anAvg = aSum / theColumn.size();
		
		float aStDev = 0.0f;
		for (int i = 0, j = theColumn.size(); i < j; ++i) {
			aStDev += Math.pow(anAvg-theColumn.getFloat(i), 2.0);
		}
		aStDev = (float) Math.sqrt(aStDev / (theColumn.size() - 1));

		float aStart = Math.max(anAvg - 2.3f * aStDev, theColumn.getMin());
		float aStop = Math.min(anAvg + 2.3f * aStDev, theColumn.getMax());
		if (aStart == aStop)
		{
			aStart--;
			aStop++;
		}
		float aValue = aStart;
		float aStep = aNrBins > 2 ? (aStop - aStart)/(aNrBins-2) : 0.0f;

		for(int i = 0; i < aNrBins-1; ++i) {
			theMap.put(aValue + i*aStep, i);
		}
		theMap.put(Float.POSITIVE_INFINITY, aNrBins-1);
	}
	
	// NOTE not save for overflow/ NaN
	private void setDistribution(Column theColumn, Map<? super Object, Integer> theMap)
	{
		float aMin = 0f;
		float aQ1 = 0f;
		float aQ2 = 0f;
		float aQ3 = 0f;
		float aMax = 0f;
		
		float[] aDataList = new float[theColumn.size()];
		for (int i = 0, j = theColumn.size(); i < j; ++i) {
			aDataList[i] = theColumn.getFloat(i);
			
			if (0f == aMin) {
				aMin = theColumn.getFloat(i);
			}
			else if (theColumn.getFloat(i) < aMin) {
				aMin = theColumn.getFloat(i);
			}
			
			if (0f == aMax) {
				aMax = theColumn.getFloat(i);
			}
			else if (theColumn.getFloat(i) > aMax) {
				aMax = theColumn.getFloat(i);
			}
		}
		
		float aSum = 0.0f;
		for (int i = 0, j = theColumn.size(); i < j; ++i) {
			aSum += theColumn.getFloat(i);
		}
		float anAvg = aSum / theColumn.size();
		
		float aStDev = 0.0f;
		for (int i = 0, j = theColumn.size(); i < j; ++i) {
			aStDev += Math.pow(anAvg - theColumn.getFloat(i), 2.0);
		}
		aStDev = (float) Math.sqrt(aStDev / (theColumn.size() - 1));

		// ---------------------
		
		NumberFormat aFormatter = NumberFormat.getNumberInstance();
		aFormatter.setMaximumFractionDigits(4);
		
		jLabelAvgStDev.setVisible(true);
		jLabelAvgStDev.setText("Avg. / StDev.");
		jTextFieldAverage.setVisible(true);
		jTextFieldStDev.setVisible(true);
		jTextFieldAverage.setText("" + aFormatter.format(anAvg));
		jTextFieldStDev.setText("" + aFormatter.format(aStDev));
		
		jLabelAvgHalfStDev.setVisible(true);
		jTextFieldAvgMinusHalfStDev.setVisible(true);
		jTextFieldAvgPlusHalfStDev.setVisible(true);
		jTextFieldAvgMinusHalfStDev.setText("" + aFormatter.format(anAvg - (0.5 * aStDev)));
		jTextFieldAvgPlusHalfStDev.setText("" + aFormatter.format(anAvg + (0.5 * aStDev)));
		
		jLabelAvgOneStDev.setVisible(true);
		jTextFieldAvgMinusOneStDev.setVisible(true);
		jTextFieldAvgPlusOneStDev.setVisible(true);
		jTextFieldAvgMinusOneStDev.setText("" + aFormatter.format(anAvg - (1 * aStDev)));
		jTextFieldAvgPlusOneStDev.setText("" + aFormatter.format(anAvg + (1 * aStDev)));
		
		jLabelMedian.setVisible(true);
		jTextFieldMedian.setVisible(true);
		jTextFieldMedian.setText("" + aFormatter.format(q2(aDataList)));
		
		jLabelMinMax.setVisible(true);
		jTextFieldMin.setVisible(true);
		jTextFieldMax.setVisible(true);
		jTextFieldMin.setText("" + aFormatter.format(aMin));
		jTextFieldMax.setText("" + aFormatter.format(aMax));
		
		jLabelQ1Q3.setVisible(true);
		jTextFieldQ1.setVisible(true);
		jTextFieldQ3.setVisible(true);
		jTextFieldQ1.setText("" + aFormatter.format(q1(aDataList)));
		jTextFieldQ3.setText("" + aFormatter.format(q3(aDataList)));
	}
	
	private void resetDistribution()
	{
		jLabelAvgStDev.setVisible(false);
		jTextFieldAverage.setVisible(false);
		jTextFieldStDev.setVisible(false);
		
		jLabelAvgHalfStDev.setVisible(false);
		jTextFieldAvgMinusHalfStDev.setVisible(false);
		jTextFieldAvgPlusHalfStDev.setVisible(false);
		
		jLabelAvgOneStDev.setVisible(false);
		jTextFieldAvgMinusOneStDev.setVisible(false);
		jTextFieldAvgPlusOneStDev.setVisible(false);
		
		jLabelMedian.setVisible(false);
		jTextFieldMedian.setVisible(false);
		
		jLabelMinMax.setVisible(false);
		jTextFieldMin.setVisible(false);
		jTextFieldMax.setVisible(false);
		
		jLabelQ1Q3.setVisible(false);
		jTextFieldQ1.setVisible(false);
		jTextFieldQ3.setVisible(false);
	}
	
	public double q1(float[] m) {
	    int q1 = (m.length)/4;

	    if (m.length%2 == 1) {
	        return m[q1];
	    }
	    else {
	        return (m[q1-1] + m[q1]) / 2.0;
	    }
	}
	
	public double q2(float[] m) {
	    int q1 = (m.length)/2;

	    if (m.length%2 == 1) {
	        return m[q1];
	    }
	    else {
	        return (m[q1-1] + m[q1]) / 2.0;
	    }
	}
	
	public double q3(float[] m) {
		double q = (m.length)*.75;
	    int q3 = (int) q;

	    if (m.length%2 == 1) {
	        return m[q3];
	    }
	    else {
	        return (m[q3-1] + m[q3]) / 2.0;
	    }
	}

	/*
	 * CODE BELOW IS OBSOLETE NOW
	 */

	// TODO binarySearch also works on Object[], change aBins/tBins to Float[]
	private Float[] createBins(Map<?, Integer> theMap)
	{
		return theMap.keySet().toArray(new Float[0]);
        /*
		float[] aBins = new float[theMap.size()];
		Iterator<? super Object> iter = (Iterator<? super Object>) theMap.keySet().iterator();
		for (int i = 0, j = theMap.size(); i < j; ++i)
			aBins[i] = (Float) iter.next();

		return aBins;
        */
	}

	// alternative could be a targetValues Map for each attributeValue
	// Map<AttributeValues, Map<TargetValues, index>> but it would be bigger
	//
	// TODO for numeric attributes all values are now used as separate value
	// should use bins
	private CategoryDataset createDatasetObsolete()
	{
		Column a = itsTable.getColumn(itsAttributeColumnsBox.getSelectedIndex());
		Column t = itsTable.getColumn(itsTargetColumnsBox.getSelectedIndex());

		// although only one map needs to be updated at selection change
		// it would require permanent storage of both aMap and tMap

		// all possible attribute values are stored in an (ordered) Map
		Map<String, Integer> aMap = new LinkedHashMap<String, Integer>();
		Iterator<String> anIterator = a.getDomain().iterator();
		int idx = -1;
		while (anIterator.hasNext())
			aMap.put(anIterator.next(), ++idx);

		// all possible target values are stored in an (ordered) Map
		Map<String, Integer> tMap = new LinkedHashMap<String, Integer>();
		anIterator = t.getDomain().iterator();
		idx = -1;
		while (anIterator.hasNext())
			tMap.put(anIterator.next(), ++idx);

		int[][] counts = new int[aMap.size()][tMap.size()];
		// loops over all values in Attribute Column
		// for each possible Attribute value the corresponding Target value
		// count is incremented
		// uses aMap and tMap for fast indexing (O(1))
		// (compared to linear array/ TreeSet lookups (O(n)))
		for (int i = 0, j = a.size(); i < j; ++i)
			++counts[aMap.get(a.getString(i))][tMap.get(t.getString(i))];

		// TODO for testing only
		if (a.getType() == AttributeType.NUMERIC && t.getType() == AttributeType.NUMERIC)
			createDatasetForNumeric();
		System.out.print(tMap.keySet().toString());
		System.out.println("\t< target values/ attribute values v");
		String[] sa = aMap.keySet().toArray(new String[0]);
		int i = -1;
		for (int[] ia : counts)
			System.out.println(Arrays.toString(ia) + "\t" + sa[++i]);

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Entry<String, Integer> ae : aMap.entrySet())
			for (Entry<String, Integer> te : tMap.entrySet())
				dataset.addValue(counts[ae.getValue()][te.getValue()], te.getKey(), ae.getKey());

		return dataset;
	}

	private void createDatasetForNumeric() {
		Column a = itsTable.getColumn(itsAttributeColumnsBox.getSelectedIndex());
		Column t = itsTable.getColumn(itsTargetColumnsBox.getSelectedIndex());

		int DEFAULT_NR_BINS = 8;
		double min = a.getMin();
		double range = (a.getMax()-min) / (DEFAULT_NR_BINS-1);

		double[] aBins = new double[DEFAULT_NR_BINS];
		for (int i = 0, j = DEFAULT_NR_BINS; i < j; ++i)
			aBins[i] = min + (range*i);

		min = t.getMin();
		range = (t.getMax()-min) / (DEFAULT_NR_BINS-1);
		double[] tBins = new double[DEFAULT_NR_BINS];
		for (int i = 0, j = DEFAULT_NR_BINS; i < j; ++i)
			tBins[i] = min + (range*i);

		int[][] counts = new int[DEFAULT_NR_BINS][DEFAULT_NR_BINS];
		for (int i = 0, j = a.size(); i < j; ++i) {
			int ai = Arrays.binarySearch(aBins, a.getFloat(i));
			int ti = Arrays.binarySearch(tBins, t.getFloat(i));
			++counts[ai < 0 ? -ai-1: ai][ti < 0 ? -ti-1: ti];
		}

		// TODO for testing only
		System.out.println("***** BOTH NUMERIC *****");
		System.out.print(Arrays.toString(tBins));
		System.out.println("\t< target values/ attribute values v");
		int i = -1;
		for (int[] ia : counts)
			System.out.println(Arrays.toString(ia) + "\t" + aBins[++i]);
		System.out.println("***** END BOTH NUMERIC *****");
	}
}
