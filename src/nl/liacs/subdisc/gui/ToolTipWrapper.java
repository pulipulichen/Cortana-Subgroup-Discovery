package nl.liacs.subdisc.gui;

public class ToolTipWrapper implements ToolTipProvider {
    final String value;
    final String toolTip;

    public ToolTipWrapper(String value, String toolTip) {
        this.value = value;
        this.toolTip = toolTip;
    }

    @Override
    public String getToolTip() {
        return toolTip; 
    }

    @Override
    public String toString() {
        return value.toString();
    }

}