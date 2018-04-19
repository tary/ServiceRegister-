package Common.ServiceRegister.ZKNodeConfig;

import java.util.List;


public interface IZKDataSource {
    List<ZKNodeInfo> getNodeInfoList() throws Exception;

    void generateTestData(String path) throws Exception;
}
