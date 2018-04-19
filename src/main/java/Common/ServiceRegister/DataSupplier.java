package Common.ServiceRegister;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * 延迟读取,单条
 */
public class DataSupplier implements Supplier<String> {
    ZkClient _client;
    String _path;
    String dataStr;

    public DataSupplier(ZkClient client, String path) {
        this._client = client;
        this._path = path;
    }

    synchronized void readData() {
        if (dataStr != null)
            return;
        try {
            byte[] data = _client.readData(this._path);
            if (data == null)
                return;
            dataStr = new String(data, Charset.forName("UTF-8"));
        } catch (ZkException e) {
            if (!ZkNodeExistsException.class.isInstance(e)) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get() {
        if (dataStr == null)
            readData();
        return this.dataStr;
    }
}
