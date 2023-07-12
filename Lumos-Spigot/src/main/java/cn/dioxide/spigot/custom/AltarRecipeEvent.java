package cn.dioxide.spigot.custom;

import cn.dioxide.spigot.custom.structure.Altar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public class AltarRecipeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack weapon;
    private final Altar altar;

    public AltarRecipeEvent(Player player, ItemStack weapon, Altar altar) {
        this.player = player;
        this.weapon = weapon;
        this.altar = altar;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public Altar getAltar() {
        return altar;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
