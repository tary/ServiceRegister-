package Common.ServiceRegister;

import Common.ServiceRegister.ZKNodeConfig.*;
import com.google.inject.AbstractModule;


/**
 * 依赖注入配置
 * */
public class ZKServiceConfig extends AbstractModule {
    @Override
    protected void configure() {

        bind(IZKNodeService.class).to(ZKNodeServiceZK.class);

        //如果使用数据库保存配置
//        bind(ISQLQuery.class).to(SQLQueryGameLog.class);
//        bind(IZKDataSource.class).to(ZKDataSourceMysql.class);

        //如果使用Toml保存配置
        bind(IZKDataSource.class).to(ZKDataSourceToml.class);

        //如果使用Redis保存配置
//        bind(IZKDataSource.class).to(ZKDataSourceRedis.class);
    }
}
