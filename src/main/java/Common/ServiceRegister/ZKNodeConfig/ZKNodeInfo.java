package Common.ServiceRegister.ZKNodeConfig;

import java.io.Serializable;

public class ZKNodeInfo implements Serializable {
    static final long serialVersionUID = 434584734786457882L;

    String host;
    Integer port;
    Long priority;

    public ZKNodeInfo() {
    }

    public ZKNodeInfo(String host, Integer port, Long priority) {
        this.host = host;
        this.port = port;
        this.priority = priority;
    }

    public Integer getPort() {
        return port;
    }

    public Long getPriority() {
        return priority;
    }


    /**
     * 修改优先级,范围[0,Constants.ZK_PRIORITY_MAX]
     *
     * @param diff 差值
     */
    public Long ModifyPriority(Long diff) {
        priority = Math.max(0L, priority + diff);
        priority = Math.min(Constants.ZK_PRIORITY_MAX, priority);
        return priority;
    }

    public String getHost() {
        return host;
    }

    public String toURL() {
        return String.format("%s:%d,", this.getHost(), this.getPort());
    }

    @Override
    public String toString() {
        return toURL();
    }
}
