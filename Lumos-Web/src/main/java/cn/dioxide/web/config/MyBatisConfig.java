package cn.dioxide.web.config;

import cn.dioxide.common.extension.ApplicationConfig;
import cn.dioxide.web.entity.StaticPlayer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Mybatis配置中心
 * @author Dioxide.CN
 * @date 2023/5/8 23:16
 * @since 1.0
 */
public class MyBatisConfig {

    private SqlSessionFactory sessionFactory;

    @SuppressWarnings("all")
    public MyBatisConfig() {
        if (ApplicationConfig.use().enable) {
            // 使用HikariCP数据库连接池
            HikariConfig config = new HikariConfig();

            // 冲入常量池
            config.setJdbcUrl(ApplicationConfig.use().lumos.datasource.jdbcUrl);
            config.setDriverClassName(ApplicationConfig.use().lumos.datasource.driverClassName);
            config.setUsername(ApplicationConfig.use().lumos.datasource.username);
            config.setPassword(ApplicationConfig.use().lumos.datasource.password);
            config.setAutoCommit(true);
            // 实例化HikariCP连接池实现连接池复用
            HikariDataSource dataSource = new HikariDataSource(config);

            // 创建 MyBatis 的 Configuration 对象
            Configuration configuration = new Configuration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setEnvironment(new Environment("dev", new JdbcTransactionFactory(), dataSource));
            configuration.getTypeAliasRegistry().registerAlias("cn.dioxide.web.entity.StaticPlayer", StaticPlayer.class);
            // 注册 mapper
            configuration.addMapper(cn.dioxide.web.mapper.PlayerMapper.class);

            // 手动加载 PlayerMapper.xml 文件
            try {
                InputStream mapperInputStream = getClass().getClassLoader().getResourceAsStream("mapper/PlayerMapper.xml");
                if (mapperInputStream == null) {
                    throw new RuntimeException("Failed to load Mapper xml");
                }
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperInputStream, configuration, "mapper/PlayerMapper.xml", configuration.getSqlFragments());
                xmlMapperBuilder.parse();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load Mapper xml", e);
            }

            // 创建 MyBatis 的 SqlSessionFactory 对象
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            sessionFactory = builder.build(configuration);
        }
    }

    public SqlSessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
