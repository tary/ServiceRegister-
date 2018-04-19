package Common.ServiceRegister;

import Common.Log.Log;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class ZKNodeMgr implements IZKNodeService {
    private static ZKNodeMgr ourInstance = null;

    IZKNodeService zkService;

    @Inject
    private ZKNodeMgr(IZKNodeService zkService) {
        this.zkService = zkService;
    }

    public static synchronized ZKNodeMgr getInstance() {
        if (ourInstance == null) {
            Injector injector = Guice.createInjector(new ZKServiceConfig());
            IZKNodeService service = injector.getInstance(IZKNodeService.class);
            if (null == service)
                return null;
            ourInstance = new ZKNodeMgr(service);
            try {
                ourInstance.Initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ourInstance;
    }

    @Override
    public void Initialize() throws Exception {
        localAddress = getLocalINetAddress();
        this.zkService.Initialize();
    }

    @Override
    public void Shutdown() {
        this.zkService.Shutdown();
    }

    @Override
    public boolean RegisterService(String servicePath, String serviceDetail) {
        Log.Instance().Debug("####RegisterService:" + servicePath);
        return this.zkService.RegisterService(servicePath, String.format("ws://%s%s", localAddress, serviceDetail));
    }

    @Override
    public boolean RegisterConsumer(String servicePath, IZKServiceEventListener listener) {
        Log.Instance().Debug("++++RegisterConsumer:" + servicePath);
        return this.zkService.RegisterConsumer(servicePath, listener);
    }


    String localAddress;//192.168.1.x

    String getLocalINetAddress() throws UnknownHostException {
        InetAddress netAddress = InetAddress.getLocalHost();
        if (null == netAddress) {
            return null;
        }

        return netAddress.getHostAddress(); //get the ip address
    }
}
