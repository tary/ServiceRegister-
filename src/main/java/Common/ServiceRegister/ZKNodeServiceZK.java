package Common.ServiceRegister;

import Common.ServiceRegister.ZKNodeConfig.Constants;
import Common.ServiceRegister.ZKNodeConfig.IZKDataSource;
import Common.ServiceRegister.ZKNodeConfig.ZKNodeInfo;
import com.google.common.collect.HashMultimap;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


public class ZKNodeServiceZK implements IZKNodeService, IZkChildListener, IZkDataListener {


    /**
     * @param source 支持依赖注入的配置源, {@link Common.ServiceRegister.ZKServiceConfig}
     * */
    @Inject
    public ZKNodeServiceZK(IZKDataSource source) {
        this.configDataSource = source;
    }

    public void Initialize() throws Exception {
        ReloadNodeInfo();
        createZkClient();
    }


    public synchronized void Shutdown() {
    }


    IZKDataSource configDataSource;

    public boolean ReloadNodeInfo() {
        return true;
    }

    private ZkClient _client = null;

    synchronized boolean createZkClient() throws Exception {
        List<ZKNodeInfo> configResult = configDataSource.getNodeInfoList();
        if (configResult.isEmpty())
            return false;

        StringBuilder stringBuilder = new StringBuilder();
        configResult.forEach(stringBuilder::append);
        _client = new ZkClient(stringBuilder.toString(), Constants.SESSION_TIMEOUT);
        if (!_client.exists(ROOT_Z_NODE_PATH)) {
            try {
                _client.createPersistent(ROOT_Z_NODE_PATH);
            } catch (ZkException e) {
            }
        }

        _client.subscribeChildChanges(ROOT_Z_NODE_PATH, this);

        //注册事件
        evtListeners.keySet().forEach(key -> {
            _client.subscribeChildChanges(key, this);
            _client.subscribeDataChanges(key, this);
        });

        return true;
    }

    HashMultimap<String, IZKServiceEventListener> evtListeners = HashMultimap.create();
//    Set<String> listenedParentPath = new ConcurrentSkipListSet<>();

    //根节点路径
    public static final String ROOT_Z_NODE_PATH = "/servers";
    public static final String SERVICE_PATH_TEMP = "/servers/%s";


    /**
     * 注册服务提供者
     *
     * @param serviceName 服务标识
     * @param uri         地址
     */
    public synchronized boolean RegisterService(String serviceName, String uri) {
        final String servicePath = pathGenerate(serviceName);
        if (!_client.exists(servicePath))
            _client.createPersistent(servicePath, true);

//        _client.writeData(servicePath, uri.getBytes(Charset.forName("UTF-8")));
        String childPath = String.format("%s/st%d", servicePath, uri.hashCode());
        if (_client.exists(childPath)) {
            try {
                _client.delete(childPath);
            } catch (ZkException e) {}
        }
        _client.createEphemeral(String.format("%s/st%d", servicePath, uri.hashCode()), uri.getBytes(Charset.forName("UTF-8")));
        return true;
    }


    /**
     * 注册消费者
     *
     * @param serviceName 服务标识
     * @param listener    监听器
     */
    public synchronized boolean RegisterConsumer(String serviceName, IZKServiceEventListener listener) {
        String servicePath = pathGenerate(serviceName);
        synchronized (this.evtListeners) {
            if (!evtListeners.containsKey(servicePath)) {
                if (!_client.exists(servicePath)) {
                    try {
                        _client.createPersistent(servicePath, true);
                    } catch (ZkException e) {
                        if (!ZkNodeExistsException.class.isInstance(e)) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                _client.subscribeChildChanges(servicePath, this);
                Supplier<String> dataSupplier = new ListDataSupplier(_client, servicePath);
                listener.onNodeDataRegisted(servicePath, dataSupplier);

            }
            evtListeners.put(servicePath, listener);
        }
        return true;
    }

    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        synchronized (this.evtListeners) {
            Set<IZKServiceEventListener> listeners = evtListeners.get(parentPath);
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            Supplier<String> dataSupplier = new ListDataSupplier(_client, parentPath).setAllNode(currentChilds);
            listeners.forEach(listener -> listener.onNodeDataRegisted(parentPath, dataSupplier));
        }
    }

    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {
        Set<IZKServiceEventListener> listeners = evtListeners.get(dataPath);
        if (listeners == null || listeners.isEmpty())
            return;

        Supplier<String> dataSupplier = new ListDataSupplier(_client, dataPath);
        listeners.forEach(listener -> listener.onNodeDataChanged(dataPath, dataSupplier));
    }

    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        handleDataChange(dataPath, null);
    }

    /**
     * 生成服务url
     */
    protected String pathGenerate(String serviceName) {
        return String.format(SERVICE_PATH_TEMP, serviceName);
    }
}



