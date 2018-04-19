package Common.ServiceRegister;

import java.util.function.Supplier;

public interface IZKServiceEventListener {
    void onNodeDeleted(String dataPath);
    void onNodeDataChanged(String dataPath, Supplier<String> dataSupplier);
    void onNodeDataRegisted(String dataPath, Supplier<String> dataSupplier);
}
