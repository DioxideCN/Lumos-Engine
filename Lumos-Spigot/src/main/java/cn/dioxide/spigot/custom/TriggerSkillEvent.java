package cn.dioxide.spigot.custom;

import cn.dioxide.common.extension.Pair;
import cn.dioxide.spigot.custom.skill.DefaultSkillHelper;
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
    private final String lore;
    private final Pair<String, Class<?>> pair;
    private final double damage;

    public TriggerSkillEvent(Player player, ItemStack weapon, String lore, double damage) {
        this.player = player;
        this.weapon = weapon;
        this.lore = lore;
        this.pair = DefaultSkillHelper.getSkill(lore);
        this.damage = damage;
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

    public String getLore() {
        return lore;
    }

    public double getDamage() {
        return damage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
