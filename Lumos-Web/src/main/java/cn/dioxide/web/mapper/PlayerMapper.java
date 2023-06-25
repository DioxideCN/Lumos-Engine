package cn.dioxide.web.mapper;

import cn.dioxide.web.entity.StaticPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
public interface PlayerMapper {

    StaticPlayer select(String name);

    void insert(StaticPlayer player);

    void update(StaticPlayer player);

    void delete(String name);

    StaticPlayer selectByQQ(@Nullable String qq);

}
