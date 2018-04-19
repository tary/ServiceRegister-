package Common.ServiceRegister;

import Common.Update.Updater;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 自动链接逻辑
 * 封装 {@link Common.Net.Connecting}
 */
public abstract class ConnectService implements Updater {
    protected List<Updater> connectors = new CopyOnWriteArrayList<>();

    public abstract boolean Initialize();

    @Override
    public void Update(long delta) throws Exception {
        for (Updater child : this.connectors) {
            child.Update(delta);
        }
    }
}
