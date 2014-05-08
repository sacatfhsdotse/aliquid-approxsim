package ApproxsimClient.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

public class ToolActionListener implements ActionListener {

    private int pushed;
    private List<JButton> toolsButtons;
    private ToolMode whenpushed;
    private MapDrawer tobemoded;

    public ToolActionListener(List<JButton> toolsButtons2, int pushed,
            MapDrawer tobemoded, ToolMode whenpushed) {
        this.pushed = pushed;
        this.toolsButtons = toolsButtons2;
        this.whenpushed = whenpushed;
        this.tobemoded = tobemoded;
    }

    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < toolsButtons.size(); i++) {
            toolsButtons.get(i).setEnabled(i != pushed);
        }
        tobemoded.mode = whenpushed;
    }

}
