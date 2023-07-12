package cn.dioxide.spigot.custom;

import cn.dioxide.common.extension.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dioxide.CN
 * @date 2023/7/12
 * @since 1.0
 */
public class TriggerSkillEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final ItemStack weapon;
    private final Pair<String, Class<?>> pair;

    public TriggerSkillEvent(Player player, ItemStack weapon, Pair<String, Class<?>> pair) {
        this.player = player;
        this.weapon = weapon;
        this.pair = pair;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public Pair<String, Class<?>> getPair() {
        return pair;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
