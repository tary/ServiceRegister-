package Common.ServiceRegister;

import Common.Log.Log;
import Common.Net.BaseSessionC;
import Common.Update.UpdateImpl;
import Common.Update.Updater;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by shuaifujian on 2018-03-28.
 */
public class ServerConnector implements IZKServiceEventListener, Updater {
    private Supplier<BaseSessionC> m_func = null;

    Supplier<String> dataSupplier;

    public void SetFunc(Supplier<BaseSessionC> func, long time) {
        m_func = func;
        m_ui.SetUpdateTime(time);
        sessionControl(sessionC -> {
            sessionC.m_uri = null;
        });
    }

    //保持
    UpdateImpl m_ui = new UpdateImpl() {
        public void doUpdate(long delTime) {
            sessionControl(sessionC -> {
                if ((dataSupplier == null && sessionC.m_uri == null) || sessionC.IsConnected() || sessionC.IsConnecting()) {
                    return;
                }

                //更新url
                if (dataSupplier != null) {
                    do {
                        String freshUrl = dataSupplier.get();
                        dataSupplier = null;
                        if (freshUrl == null || freshUrl.isEmpty())
                            break;
                        try {
                            final URI uri = new URI(freshUrl);
                            if (uri == null)
                                break;
                            if (uri.equals(sessionC.m_uri))
                                break;
                            sessionC.m_uri = uri;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    } while (false);
                }

                if (sessionC.IsClosed()) {
                    sessionC.Connect();
                }
            });
        }
    };

    @Override
    public void Update(long delta) throws IOException {
        if (null == m_func) return;
        m_ui.Update(delta);
    }


    @Override
    public void onNodeDeleted(String dataPath) {
        BaseSessionC bc = m_func.get();
        if (bc == null)
            return;
    }

    @Override
    public void onNodeDataChanged(String dataPath, Supplier<String> dataSupplier) {
        Log.Instance().Debug(String.format("-------DataChanged:%s", dataPath));
        this.dataSupplier = dataSupplier;
    }

    @Override
    public void onNodeDataRegisted(String dataPath, Supplier<String> dataSupplier) {
        Log.Instance().Debug(String.format("-------DataRegisted:%s", dataPath));
        this.dataSupplier = dataSupplier;
    }

    synchronized boolean sessionControl(Consumer<BaseSessionC> consumer) {
        BaseSessionC bc = m_func.get();
        if (bc == null)
            return false;

        consumer.accept(bc);
        return true;
    }
}
