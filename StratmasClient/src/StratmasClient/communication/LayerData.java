package ApproxsimClient.communication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import org.apache.xerces.impl.dv.util.Base64;
import ApproxsimClient.Debug;
import ApproxsimClient.object.primitive.Reference;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.ProcessVariableDescription;
import ApproxsimClient.object.primitive.Timestamp;

/**
 * A class that represents a layer of data for a certain number of cells in the grid. A layer is simply the value for a processvariable and
 * a faction for a number of cells.
 * 
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author Per Alexius
 */
public class LayerData {
    private Vector mListeners = new Vector();
    private int mSize;
    private String mLayer;
    private ProcessVariableDescription mPvd;
    private Reference mFaction;
    private double[] mData;
    private Timestamp mTimestamp;

    public LayerData(ProcessVariableDescription pvd, Reference faction,
            double[] data) {
        mSize = data.length;
        mLayer = pvd.getName();
        mFaction = faction;
        mData = data;
        mPvd = pvd;
    }

    public double[] getData() {
        return mData;
    }

    public ProcessVariableDescription getProcessVariableDescription() {
        return mPvd;
    }

    public String getLayer() {
        return mLayer;
    }

    public Reference getFaction() {
        return mFaction;
    }

    public Timestamp getTimestamp() {
        return mTimestamp;
    }

    public void addListener(ApproxsimEventListener listener) {
        mListeners.add(listener);
    }

    public void removeListerner(ApproxsimEventListener listener) {
        mListeners.remove(listener);
    }

    public synchronized void update(org.w3c.dom.Element n, Timestamp t) {
        mTimestamp = t;
        byte[] rawData = Base64.decode(XMLHandler.getString(n, "layerData"));
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
                rawData));
        try {
            for (int i = 0; i < mSize; i++) {
                mData[i] = dis.readDouble();
            }
            Debug.err.println("Read " + mSize + " doubles");
        } catch (EOFException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            Debug.err
                    .println("This indicates that the Client has a larger grid than the server.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        ApproxsimEvent event = ApproxsimEvent.getGeneric(this);
        for (Iterator it = mListeners.iterator(); it.hasNext();) {
            ((ApproxsimEventListener) it.next()).eventOccured(event);
        }
    }
}
