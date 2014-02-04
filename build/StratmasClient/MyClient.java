package StratmasClient;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

import StratmasClient.object.StratmasObject;

public class MyClient extends Client {
     private StratmasObject mObj = null;

     public MyClient(String foo) {
          super(foo);
          System.err.println("Creating MyClient");
     }

     public void setActiveClient(boolean active) {
     }

     public void setProcessVariables(Vector pv) {
     }

     public void setNotify() {
          synchronized(this) {
               this.notify();
          }
     }
     
     public void set(StratmasObject o) {
          mObj = o;
     }
     
     public void setGrid(StratmasClient.communication.GridData gd) {
     }
     
     void tell() {
          if (mObj != null) {
               try {
                    String filename = "subed.xml";
                    PrintWriter pw = new PrintWriter(new FileWriter(filename));
                    System.err.println("Writing '" + mObj.getIdentifier() + "' to " + filename);
                    pw.print(mObj.toXML());
                    pw.close();
               } catch (IOException e) {
                    e.printStackTrace();
               }
          }
     }

     public void updateStatus(Hashtable errors, String msg_type) {
          // collect all messages
          Vector fatal = (Vector)errors.get("fatal");
          Vector general = (Vector)errors.get("general");
          Vector warning = (Vector)errors.get("warning");
          // fatal errors
          if (fatal != null && !fatal.isEmpty() || 
              general != null && !general.isEmpty() ||
              warning != null && !warning.isEmpty()) {
               System.err.println("Error occured");
          }
     }
}
