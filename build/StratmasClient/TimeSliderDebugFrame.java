package StratmasClient;


import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.DecimalFormat;

import StratmasClient.Debug;


/**
 * Debug frame for the time slider.
 *
 * @version 1.0
 * @author Per Alexius
 */
public class TimeSliderDebugFrame extends JFrame {
     /**
      * The time to wait between two consecutive time step messages.
      */
     private int mWaitTime = 0;
     /**
      * The execution time of the ten last time steps (where execution
      * time is counted from the message is sent to the answer is
      * handled by the XMLHandler).
      */
     private long [] mStepTimes = new long[10];
     /**
      * The number of valid times in the mStepTimes array.
      */
     private int mNumStepTimes = 0;
     /**
      * The index in the mStepTimes array to put the next execution
      * time in.
      */
     private int mCurrentIndex = 0;
     /**
      * The mean step time among the times in the mStepTimes array.
      */
     private double mMeanStepTime = 0;
     /**
      * The label showing the approximate wall clock time between time
      * steps.
      */
     JLabel mLabel;
     /**
      * Formater for label output.
      */
     DecimalFormat mFormat = new DecimalFormat("####0.00");

     /**
      * Constructor.
      */
     public TimeSliderDebugFrame() {
	  super("TimeSliderDebugFrame");

	  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	  final TimeSliderDebugFrame self = this;
	  JSlider timeSlider = new JSlider(0, 10000, 0);
	  mLabel = new JLabel(mFormat.format(mMeanStepTime));
	  timeSlider.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent e) {
			 JSlider source = (JSlider) e.getSource();
			 if (!source.getValueIsAdjusting()) {
			      mLabel.setText(mFormat.format((mMeanStepTime + source.getValue()) / 1000.0));
			      mWaitTime = source.getValue();
			 }
		    }
	       });
	  JPanel timeSliderPanel = new JPanel();
	  timeSliderPanel.setLayout(new BoxLayout(timeSliderPanel, BoxLayout.Y_AXIS));
	  timeSliderPanel.add(timeSlider);
	  timeSliderPanel.add(mLabel);

	  getContentPane().add(timeSliderPanel);
     }

     /**
      * Register the execution time of a timestep.
      *
      * @param timeMs The step time in milliseconds.
      */
     public void registerStepTime(long timeMs) {
	  mStepTimes[mCurrentIndex] = timeMs;
	  if (mNumStepTimes < mStepTimes.length) {
	       mNumStepTimes++;
	  }
	  mCurrentIndex = (mCurrentIndex + 1) % mStepTimes.length;
	  double sum = 0;
	  for (int i = 0; i < mNumStepTimes; i++) {
	       sum += mStepTimes[i];
	  }
	  mMeanStepTime = sum / mNumStepTimes;
	  mLabel.setText(mFormat.format((mMeanStepTime + mWaitTime) / 1000.0));
     }

     /**
      * Accessor for the value of the slider.
      *
      * @return The current value of the slider, i.e. the time to wait
      * between two consecutive step messages.
      */
     public long getWaitTimeMs() {
	  return mWaitTime;
     }

     public static TimeSliderDebugFrame openTimeSliderDebugFrame() {
	  final TimeSliderDebugFrame frame = new TimeSliderDebugFrame();
	  
	  javax.swing.SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			 frame.pack();
			 frame.setVisible(true);
		    }
	       });
	  return frame;
     }
}
