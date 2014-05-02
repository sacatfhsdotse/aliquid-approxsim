// $Id: StratmasObjectImpl.java,v 1.6 2006/07/31 10:18:45 alexius Exp $
/*
 * @(#)StratmasObject.java
 */

package StratmasClient.object;

import StratmasClient.ActionGroup;
import StratmasClient.Client;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;

import javax.swing.event.EventListenerList;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StratmasObjectImpl is a convinience implementation of StratmasObject providing dynamic identifiers and parents as well as an
 * EventListener implementation and some actions.
 * 
 * @version 1, $Date: 2006/07/31 10:18:45 $
 * @author Daniel Ahlin
 */

abstract class StratmasObjectImpl extends StratmasObject {
    /**
     * The identifier of this object.
     */
    String identifier;

    /**
     * The listeners of this object.
     */
    List<StratmasEventListener> eventListenerList = null;

    /**
     * The container holding this object, or null if nothing is holding it.
     */
    StratmasObject parent = null;

    /**
     * Empty list to use for EventListenerList implementation.
     */
    static StratmasEventListener[] EMPTY_ARRAY = new StratmasEventListener[0];
    static List<StratmasEventListener> EMPTY_LIST = new ArrayList<StratmasEventListener>(0);
    
    /**
     * Creates a new StratmasObject.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     */
    StratmasObjectImpl(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Creates a new StratmasObject from a Declaration.
     * 
     * @param declaration the declaration for this object.
     */
    StratmasObjectImpl(Declaration declaration) {
        this(declaration.getName());
    }

    /**
     * Returns the parent of this object (or null if no parent).
     */
    public StratmasObject getParent() {
        return this.parent;
    }

    /**
     * Returns the identifier of this object.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Sets the identifier of this object.
     * 
     * @param identifier the new identifier of this object.
     */
    public void setIdentifier(String identifier) {
        String oldIdentifier = getIdentifier();
        this.identifier = identifier;
        if (getParent() instanceof StratmasList) {
            ((StratmasList) getParent()).childIdentifierChange(oldIdentifier,
                                                               identifier);
        }
        fireIdentifierChanged(oldIdentifier, null);
    }

    /**
     * Sets the parent of this object.
     * 
     * @param parent the new parent of this object.
     */
    protected void setParent(StratmasObject parent) {
        if (this.parent == null) {
            this.parent = parent;
            StratmasObjectFactory.attached(this);
        } else {
            this.parent = parent;
        }
    }

    /**
     * Returns a list of the listeners of this object.
     */
    protected List<StratmasEventListener> getEventListenerList() {
        if(eventListenerList == null){
            return EMPTY_LIST;
        }
        return this.eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    public void addEventListener(StratmasEventListener listener) {
        if (eventListenerList == null) {
            eventListenerList = new ArrayList<StratmasEventListener>();
        }
        eventListenerList.add(listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to remove.
     */
    public void removeEventListener(StratmasEventListener listener) {
        if (eventListenerList != null) {
            eventListenerList.remove(listener);
        }
    }

    /**
     * Notifies listeners that this object has been removed and should no longer be listened to.
     * 
     * @param initiator The initiator of the removal.
     */
    public void fireRemoved(Object initiator) {
        StratmasEvent event = StratmasEvent.getRemoved(this, initiator);
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
    }

    /**
     *
     */
    public void fireSelected(boolean selected) {
        StratmasEvent event;
        if (selected) {
            event = StratmasEvent.getSelected(this);
        } else {
            event = StratmasEvent.getUnselected(this);
        }
        
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
    }

    /**
     * Notifies listeners of on this object that the identifier has changed.
     * 
     * @param oldIdentifier the old identifier.
     * @param initiator the initiator the identifier change.
     */
    protected void fireIdentifierChanged(String oldIdentifier, Object initiator) {
        StratmasEvent event = StratmasEvent.getIdentifierChanged(this, oldIdentifier);
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
    }

    /**
     * Notifies listeners that a child object has changed.
     * 
     * @param changed The StratmasObject that has changed.
     */
    public void fireChildChanged(StratmasObject changed, Object initiator) {
        StratmasEvent event = StratmasEvent.getChildChanged(this,
                                                            initiator,
                                                            changed);

        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
    }

    /**
     * Notifies listeners that an object has been added to this object.
     * 
     * @param added The StratmasObject that has been added.
     * @param initiator The initiator of the event.
     */
    public void fireObjectAdded(StratmasObject added, Object initiator) {
        // Notify listeners about added object.
        StratmasEvent event = StratmasEvent.getObjectAdded(this, added,
                                                           initiator);
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
        
        // Tell our parent that we have changed.
        if (getParent() != null) {
            getParent().childChanged(this, null);
        }
    }

    /**
     * Replaces this object with the provided object.
     * <p>
     * author Per Alexius
     * 
     * @param toReplaceWith The object to replace this object with.
     * @param initiator The initiator of this replace action.
     */
    public void replace(StratmasObject toReplaceWith, Object initiator) {
        if (getParent() != null) {
            getParent().replaceChild(this, toReplaceWith, initiator);
        }
        fireReplaced(toReplaceWith, initiator);
    }

//     /**
//      * Returns actions associated with this object
//      */
//     public Vector getActions()
//     {
//         Vector res = new Vector();

//         final StratmasObject self = this;

//         StratmasAbstractAction deleteAction = 
//             new StratmasAbstractAction("Delete", true)
//             {
//                 public void actionPerformed(ActionEvent e)
//                 {
//                     remove();
//                 }
//             };
//         deleteAction.setEnabled(false);

//         if (this.getParent() != null && 
//             (this.getParent() instanceof StratmasList ||
//             (getDeclaration() != null && 
//              getDeclaration().getMinOccurs() == 0))) {
//             if (this.getParent() instanceof StratmasList) {
//                 StratmasList list = (StratmasList) this.getParent();
//                 if (list.getDeclaration() != null) {
//                     if((list.getChildCount() - 1) >= list.getDeclaration().getMinOccurs()) {
//                         deleteAction.setEnabled(true);
//                     }
//                     else {
//                         deleteAction.setEnabled(false);
//                     }
//                 }
//                 else {
//                     deleteAction.setEnabled(true);
//                 }
//             }
//             else {
//                 deleteAction.setEnabled(true);
//             }
//         }
//         res.add(deleteAction);

//         res.add(new StratmasAbstractAction("Save as", false)
//             {
//                 public void actionPerformed(ActionEvent e)
//                     {
//                         Client.exportToFile(self);
//                     }

//             });

//         if (!(this instanceof StratmasList) && 
//             getType().getAnnotations() != null &&
//             getType().getAnnotations().length != 0) {
//             res.add(new StratmasAbstractAction("Whats this?", false)
//                 {
//                     public void actionPerformed(ActionEvent e)
//                     {
//                         StringBuffer buf = new StringBuffer();
//                         Type type = self.getType();
//                         Type walker = type;
//                         while (walker != null && 
//                                !walker.getBaseType().getName().equals("anyType")) {
//                             if (walker.getAnnotations() != null) {
//                                 String[] annotations = 
//                                     walker.getAnnotations();                            
//                                 for (int i = 0; i < annotations.length; i++) {
//                                     if (annotations[i].length() != 0) {
//                                         buf.insert(0, annotations[i] + "\n");
//                                     }
//                                 }                                
//                             }
//                             walker = walker.getBaseType();
//                         }

//                         String annotation = buf.toString().replaceAll("<.*>\\s*", "");

//                         javax.swing.JOptionPane.showMessageDialog(null, annotation, 
//                                                       self.getType().toString(), 
//                                                       javax.swing.JOptionPane.INFORMATION_MESSAGE);
//                     }
//                 });
//         }

//         return res;
//     }

    /**
     * Returns actions associated with this object
     */
    public ActionGroup getActionGroup() {
        ActionGroup ag = new ActionGroup("Actions for " + getIdentifier(),
                false, true);

        final StratmasObject self = this;

        ActionGroup deleteAction = new ActionGroup("Delete", true, false) {
            /**
			 * 
			 */
            private static final long serialVersionUID = 6162649775446827051L;

            public void actionPerformed(ActionEvent e) {
                remove();
            }
        };
        deleteAction.setEnabled(false);

        if (this.getParent() != null
                && (this.getParent() instanceof StratmasList || (getDeclaration() != null && getDeclaration()
                        .getMinOccurs() == 0))) {
            if (this.getParent() instanceof StratmasList) {
                StratmasList list = (StratmasList) this.getParent();
                if (list.getDeclaration() != null) {
                    if ((list.getChildCount() - 1) >= list.getDeclaration()
                            .getMinOccurs()) {
                        deleteAction.setEnabled(true);
                    } else {
                        deleteAction.setEnabled(false);
                    }
                } else {
                    deleteAction.setEnabled(true);
                }
            } else {
                deleteAction.setEnabled(true);
            }
        }
        ag.add(deleteAction);

        ag.add(new ActionGroup("Save as", false, false) {
            /**
			 * 
			 */
            private static final long serialVersionUID = 6973529851127489894L;

            public void actionPerformed(ActionEvent e) {
                Client.exportToFile(self);
            }
        });

        if (!(this instanceof StratmasList)
                && getType().getAnnotations() != null
                && getType().getAnnotations().length != 0) {
            ag.add(new ActionGroup("Whats this?", false, false) {
                /**
				 * 
				 */
                private static final long serialVersionUID = 8603066280387680589L;

                public void actionPerformed(ActionEvent e) {
                    StringBuffer buf = new StringBuffer();
                    Type type = self.getType();
                    Type walker = type;
                    while (walker != null
                            && !walker.getBaseType().getName()
                                    .equals("anyType")) {
                        if (walker.getAnnotations() != null) {
                            String[] annotations = walker.getAnnotations();
                            for (int i = 0; i < annotations.length; i++) {
                                if (annotations[i].length() != 0) {
                                    buf.insert(0, annotations[i] + "\n");
                                }
                            }
                        }
                        walker = walker.getBaseType();
                    }

                    String annotation = buf.toString().replaceAll("<.*>\\s*",
                                                                  "");

                    javax.swing.JOptionPane
                            .showMessageDialog(null,
                                               annotation,
                                               self.getType().toString(),
                                               javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
        return ag;
    }

    /**
     * Notifies listeners that a child object has been replaced.
     */
    protected void fireReplaced(StratmasObject newObj, Object initiator) {
        StratmasEvent event = StratmasEvent.getReplaced(this, initiator,
                                                        newObj);
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
    }
}
