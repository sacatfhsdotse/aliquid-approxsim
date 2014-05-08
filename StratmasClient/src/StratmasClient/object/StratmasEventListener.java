package StratmasClient.object;

import java.util.EventListener;

public interface StratmasEventListener extends EventListener {
    public void eventOccured(StratmasEvent event);
}
