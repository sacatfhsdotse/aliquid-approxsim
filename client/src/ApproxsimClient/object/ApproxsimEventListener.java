package ApproxsimClient.object;

import java.util.EventListener;

public interface ApproxsimEventListener extends EventListener {
    public void eventOccured(ApproxsimEvent event);
}
