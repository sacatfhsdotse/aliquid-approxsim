//         $Id: ActionGroup.java,v 1.1 2006/07/31 10:17:48 alexius Exp $
/*
 * @(#) ActionGroup.java
 */

package StratmasClient;

import java.awt.event.ActionEvent;

import java.util.Vector;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * ActionGroup group actions, allowing a hierarchy of actions.
 *
 * @version 1, $Date: 2006/07/31 10:17:48 $
 * @author  Per Alexius
*/
public class ActionGroup extends StratmasAbstractAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -148393534237020841L;

	/**
     * The Vector containing the members of this group.
     */
    Vector members;

    /**
     * Flag indicating submenuableness.
     */
    boolean isSubmenuable;

    /**
     * Creates a new ActionGroup with the specified name.
     *
     * @param name the name of this group.
     * @param isSubmenuable The initial submenuable state.
     */
    public ActionGroup(String name, boolean isMutator, boolean isSubmenuable) {
        super(name, isMutator);
        this.isSubmenuable = isSubmenuable;
        members = new Vector();
    }

    /**
     * Adds an ActionGroup to this ActionGroup.
     *
     * @param ag The ActionGroup to add.
     */
    public void add(ActionGroup ag) {
        members.add(ag);
    }


    /**
     * The action performed by this group, (which is nothing).
     */
    public void actionPerformed(ActionEvent event) {
    }

    /**
     * Accessor for the flag indicating submenuableness.
     *
     * @return True if this group is submenuable, false otherwise.
     */
    public boolean isSubmenuable() {
        return isSubmenuable;
    }

    /**
     * Convenience method for collecting the menu items corresponding
     * to the actions in this group. Used in order to create both
     * ordinary and popup menus.
     *
     * @param menuItems The Vector to add menu items to.
     * @param mutatePermission Flag indicating whether we have
     * permission to mutate or not.
     */
    private void collectMenuItems(Vector menuItems, boolean mutatePermission) {
        for (Iterator it = members.iterator(); it.hasNext(); ) {
            ActionGroup action = (ActionGroup)it.next();
            if (action.isSubmenuable()) {
                JMenu submenu = new JMenu((String)action.getValue(Action.NAME));
                action.addToMenu(submenu, mutatePermission);
                if (!mutatePermission && action.isMutator()) {
                    submenu.setEnabled(false);
                }
                menuItems.add(submenu);
            }
            else {
                if (!mutatePermission && action.isMutator()) {
                    action.setEnabled(false);
                }
                menuItems.add(new JMenuItem(action));
            }
        }
    }

    /**
     * Adds actions of this ActionGroup to the provided popup menu.
     *
     * @param menu The popup menu to add actions to.
     * @param mutatePermission Flag indicating whether we have
     * permission to mutate or not.
     */
    public void addToPopupMenu(JPopupMenu menu, boolean mutatePermission) {
        Vector menuItems = new Vector();
        collectMenuItems(menuItems, mutatePermission);
        for (Iterator it = menuItems.iterator(); it.hasNext(); ) {
            menu.add((JMenuItem)it.next());
        }
    }

    /**
     * Adds actions of this ActionGroup to the provided menu.
     *
     * @param menu The menu to add actions to.
     * @param mutatePermission Flag indicating whether we have
     * permission to mutate or not.
     */
    public void addToMenu(JMenu menu, boolean mutatePermission) {
        Vector menuItems = new Vector();
        collectMenuItems(menuItems, mutatePermission);
        for (Iterator it = menuItems.iterator(); it.hasNext(); ) {
            menu.add((JMenuItem)it.next());
        }
    }
}
