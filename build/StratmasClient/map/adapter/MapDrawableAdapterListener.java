package StratmasClient.map.adapter;

import StratmasClient.object.StratmasObject;
import java.util.EventListener;

/**
 * MapDrawableAdapterListener's listens to changes in MapDrawableAdapters.
 *
 * @author Daniel Ahlin, Amir Filipovic
 */
public interface MapDrawableAdapterListener extends EventListener
{
    /**
     * Signaled when an MapDrawableAdapters object is removed.
     *
     * @param drawableAdapter the MapDrawableAdapter whose object is being removed.
     */
    public abstract void mapDrawableAdapterRemoved(MapDrawableAdapter drawableAdapter);
    
    /**
     * Signaled when displaylists in an MapDrawableAdapter needs to be
     * recompiled.
     *
     * @param drawableAdapter the MapDrawableAdapter that needs to be updated.
     */
    public abstract void mapDrawableAdapterUpdated(MapDrawableAdapter drawableAdapter);

    /**
     * Signaled when an object is added to an MapDrawableAdapters object.
     *
     * @param object the new object.
     */
    public abstract void mapDrawableAdapterChildAdded(StratmasObject object);
}
