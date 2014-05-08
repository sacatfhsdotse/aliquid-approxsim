package StratmasClient.communication;

import java.lang.StringBuffer;

/**
 * Class representing the step message. The step message is the message that an active client sends to a server in order to advance the
 * simulation a specified number of timesteps.
 * 
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author Per Alexius
 */
public class StepMessage extends StratmasMessage {
    /** True if the step(s) should be executed detached. */
    private boolean mDetached;
    /** Number of timesteps */
    private int mSteps;

    /**
     * Creates a step message - with a specified number of timesteps - that may or may not be executed detached.
     * 
     * @param steps The number of timesteps
     * @param detached Should be set to true if the steps are to be executed detached.
     */
    public StepMessage(int steps, boolean detached) {
        mSteps = steps;
        mDetached = detached;
    }

    /**
     * Returns a string representation of the type of this message.
     * 
     * @return A string representation of the type of this message.
     */
    public String getTypeAsString() {
        return "StepMessage";
    }

    /**
     * Creates an XML representation of the body of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        b.append(NL).append("<numberOfTimesteps>").append(mSteps)
                .append("</numberOfTimesteps>");
        b.append(NL).append("<detached>").append(mDetached)
                .append("</detached>");
        return b;
    }
}
