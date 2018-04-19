###服务自动注册发现逻辑(仅简单测试)
  
  - 说明:
  
    * __一些无关依赖代码没有包含__
    * 目的取代resources下servers.properties内关于服务器地址的配置
    * 服务器之间连接自动发现,更换机器,不需要改配置
    * 需要配置Zookeeper, 需要每台部署的机器配置本机内网地址. 
    * 发布服务使用的是本机etc/hosts内localhost对应的IP发布,局域网内部署需要配置内网IP
    * 仅支持一对一连接
  - 替换逻辑: 
    
    * 消费者发现数据变更不会立即断开旧链接,而是等下个断开后重连流程
    * 两个相同的服务不能同时开,如Manager
    * 先开一个Manager,等其他服务器连接完成后再开另一个Manager是可行的,
    如果第一个Manager断开其他链接会重新连接到新Manager
    * 替换只考虑连接, 不能保证逻辑安全, 后开启的Manager跟前者一样,只是没有人连而已
    
  ZKConnectServiceBase的子类实现Initialize,填充订阅发布逻辑
  - 发布服务(被连接):
    ```
    RegisterService("ClientGateServer", port, ClientSessionS.class);
    ```
  - 订阅服务(连接请求方):
    ```
    ServerConnector connector = RegisterConsumer(
                    "DBCache", "/DBCache/RoomEntry",
                    () -> DBCacheSessionC.getInstance(), 5000);
    this.connectors.add(connector);
    ```
  - 示例:
    ```java
    package ManagerServer.Servlet;
    
    import Common.ServiceRegister.ZKConnectServiceBase;
    import Common.Update.Updater;
    import ManagerServer.Net.ClientGateSessionS;
    import ManagerServer.Net.DBCacheSessionC;
    import ManagerServer.Net.LoginSessionS;
    import ManagerServer.Net.RoomSessionS;
    
    public class ZKConnectService extends ZKConnectServiceBase {
        @Override
        public boolean Initialize() {
            Updater connector = RegisterConsumer(
                    "DBCache",
                    "/DBCache/RoomEntry",
                    () -> DBCacheSessionC.getInstance(), 5000);
    
            this.connectors.add(connector);
    
            String port = System.getenv("LOCAL_PORT");
            if (port == null || port.isEmpty()) {
                port = "17000";
            }
            RegisterService("ManagerServer", port, ClientGateSessionS.class);
            RegisterService("ManagerServer", port, LoginSessionS.class);
            RegisterService("ManagerServer", port, RoomSessionS.class);
    
            return true;
        }
    }
    ```


###自动连接服务整理

  - Zookeeper实现
    ```
    ConnectService connectService = new STConnectService(); //主要差异
    connectService.Initialize();
    UpdateMgr.Instance().AddUpdater(connectService);
    ```
   
    



