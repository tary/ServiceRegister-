package Common.ServiceRegister;

/**
 * Created by shuaifujian on 2018-03-27.
 */
public interface IZKNodeService {
    void Initialize() throws Exception;
    void Shutdown();
    boolean RegisterService(String servicePath, String serviceDetail);
    boolean RegisterConsumer(String servicePath, IZKServiceEventListener listener);
}
