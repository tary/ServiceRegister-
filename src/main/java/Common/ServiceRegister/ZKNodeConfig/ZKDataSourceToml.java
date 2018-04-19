package Common.ServiceRegister.ZKNodeConfig;

import Common.utils.TomlReaderWriter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZKDataSourceToml implements IZKDataSource {


    class AllZKNodeConfig implements Serializable {
        static final long serialVersionUID = 57882L;
        List<ZKNodeInfo> NodeInfo;
    }

    @Override
    public List<ZKNodeInfo> getNodeInfoList() throws Exception {
        TomlReaderWriter reader = new TomlReaderWriter();
        AllZKNodeConfig allCfg = reader.readResourcesConfig(
                "zk_node_config.toml",
                AllZKNodeConfig.class);

        return allCfg.NodeInfo;
    }

    @Override
    public void generateTestData(String path) throws Exception {
        TomlReaderWriter reader = new TomlReaderWriter();
        AllZKNodeConfig config = new AllZKNodeConfig();
        config.NodeInfo = new ArrayList<>();
        config.NodeInfo.add(new ZKNodeInfo("192.168.1.85", 2181, 500L));
        config.NodeInfo.add(new ZKNodeInfo("192.168.1.86", 2181, 200L));
        config.NodeInfo.add(new ZKNodeInfo("192.168.1.79", 2181, 200L));
        reader.saveConfig(config, new File(path));
    }
}
