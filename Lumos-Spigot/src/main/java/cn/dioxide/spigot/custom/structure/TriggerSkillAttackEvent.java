package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.extension.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dioxide.CN
 * @date 2023/7/12
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class TriggerSkillAttackEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final LivingEntity entity;
    private final ItemStack weapon;
    private final String lore;
    private final Pair<String, ICustomSkillHandler> pair;
    private final double damage;
    private final EntityDamageByEntityEvent damageEvent;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
