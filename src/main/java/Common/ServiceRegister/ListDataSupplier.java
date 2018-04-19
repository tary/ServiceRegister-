package Common.ServiceRegister;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 延迟读取
 */
public class ListDataSupplier implements Supplier<String> {
    ZkClient _client;
    String _path;
    List<String> _allChild;
    List<String> dataList;

    public ListDataSupplier setAllNode(List<String> allNode) {
        this._allChild = allNode;
        return this;
    }

    public List<String> getAllNode() {
        if (this._allChild != null)
            return this._allChild;

        if (this._client == null || this._path == null)
            return null;

        List<String> allNode = this._client.getChildren(this._path);
        synchronized (this) {
            setAllNode(allNode);
        }

        return this._allChild;
    }

    public ListDataSupplier(ZkClient client, String path) {
        this._client = client;
        this._path = path;
    }

    StringBuffer stringBuffer = new StringBuffer();

    synchronized void readData() {
        if (dataList != null)
            return;
        try {
            List<String> allChild = getAllNode();
            if (allChild == null || allChild.isEmpty())
                return;

            List<String> dataListTmp = new ArrayList<>();
            allChild.forEach(id -> {
                stringBuffer.setLength(0);
                stringBuffer.append(this._path);
                stringBuffer.append("/");
                stringBuffer.append(id);

                try {
                    byte[] data = _client.readData(stringBuffer.toString());
                    if (data != null) {
                        String dataStr = new String(data, Charset.forName("UTF-8"));
                        dataListTmp.add(dataStr);
                    }
                } catch (ZkException e) {
                    if (!ZkNodeExistsException.class.isInstance(e)) {
                        e.printStackTrace();
                    }
                }
            });

            this.dataList = dataListTmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get() {
        if (dataList == null)
            readData();

        if (this.dataList == null || this.dataList.isEmpty())
            return null;
        return this.dataList.get(0);
    }
}
