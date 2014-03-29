package StratmasClient.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

public class ToolActionListener implements ActionListener {
	
	private int pushed;
	List<JButton> toolsButtons;
	
    public ToolActionListener(List<JButton> toolsButtons2, int pushed) {
        this.pushed = pushed;
        this.toolsButtons = toolsButtons2;
    }
    
	public void actionPerformed(ActionEvent e) {
		for(int i = 0; i < toolsButtons.size(); i++) {
			toolsButtons.get(i).setEnabled(i != pushed);
		}
	}

}
