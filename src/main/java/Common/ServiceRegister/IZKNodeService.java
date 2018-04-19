package Common.ServiceRegister;

public interface IZKNodeService {
    void Initialize() throws Exception;
    void Shutdown();
    boolean RegisterService(String servicePath, String serviceDetail);
    boolean RegisterConsumer(String servicePath, IZKServiceEventListener listener);
}
