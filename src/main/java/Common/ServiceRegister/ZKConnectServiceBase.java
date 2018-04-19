package Common.ServiceRegister;

import Common.Net.BaseSessionC;
import Common.Net.BaseSessionS;

import javax.websocket.server.ServerEndpoint;
import java.util.function.Supplier;

/**
 * 自动链接逻辑
 */
public abstract class ZKConnectServiceBase extends ConnectService {

    /**
     * 发布服务<br/>
     * 使用serverName和{@link Common.Net.BaseSessionS}的子类上标注的{@link javax.websocket.server.ServerEndpoint}的值<br/>
     * 如ClientSession的 {@code @ServerEndpoint(value = "/ClientGate/ClientEntry")} <br/>
     * 生成唯一服务标识如:{@code /ManagerServer/Manager/RoomEntry}
     *
     * @param serverName 服务器名称,一般未tomcat的webapps下目录的名称如ManagerServer/LoginServer等
     *                   @param port 服务器端口,用于生成URL
     *                               @param cls BaseSession的子类如: {@code ClientGateSessionS.class}
     */


    protected <T extends BaseSessionS> boolean RegisterService(String serverName, String port, Class<T> cls) {
        // 判断类上是否有次注解
        if (!cls.isAnnotationPresent(ServerEndpoint.class)) {
            return false;
        }

        // 获取类上的注解
        ServerEndpoint annotation = cls.getAnnotation(ServerEndpoint.class);
        return ZKNodeMgr.getInstance().RegisterService(
                servicePath(serverName, annotation.value()),
                String.format(":%s/%s%s", port, serverName, annotation.value()));
    }


    /**
     * 生成服务唯一标识
     *
     * @param serverName 服务器名称
     * @param path       路径
     */
    protected String servicePath(String serverName, String path) {
        return String.format("%s%s", serverName, path);
//        return String.format("%s%s", serverName, path.replaceAll("/", "_"));
    }

    protected ServerConnector RegisterConsumer(String serverName, String path, Supplier<BaseSessionC> func, long time) {
        ServerConnector autoConn = new ServerConnector();
        autoConn.SetFunc(func, time);
        String servicePath = servicePath(serverName, path);
        ZKNodeMgr.getInstance().RegisterConsumer(servicePath, autoConn);
        return autoConn;
    }
}
