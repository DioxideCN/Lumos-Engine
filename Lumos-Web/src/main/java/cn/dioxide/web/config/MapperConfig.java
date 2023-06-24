package cn.dioxide.web.config;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Mybatis中Mapper映射文件处理核心
 * @author Dioxide.CN
 * @date 2023/5/8 23:34
 * @since 1.0
 */
public class MapperConfig {

    final SqlSession session;
    SqlSessionFactory sessionFactory = new MyBatisConfig().getSessionFactory();

    /**
     * 获取mapper
     * @param mapper 类型
     */
    public <T> T getInstance(Class<T> mapper) {
        return session.getMapper(mapper);
    }

    /**
     * 提交事务
     */
    public void commit() {
        session.commit();
    }

    protected volatile static MapperConfig INSTANCE = null;
    protected MapperConfig() {
        session = sessionFactory.openSession();
    }

    public static MapperConfig use() {
        if (INSTANCE == null) {
            synchronized (MapperConfig.class) {
                if (INSTANCE == null) INSTANCE = new MapperConfig();
            }
        }
        return INSTANCE;
    }

}
