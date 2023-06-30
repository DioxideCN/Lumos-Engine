package cn.dioxide.spigot;

import cn.dioxide.common.annotation.ScanPackage;
import cn.dioxide.common.extension.ApplicationConfig;
import cn.dioxide.common.extension.BeanHolder;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.extension.Config;
import cn.dioxide.web.infra.LocalWebEngine;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
@ScanPackage({"cn.dioxide.web"})
public class LumosStarter extends JavaPlugin implements Listener {

    public static LumosStarter INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Format.init(this, "&7[&3&lLumos&b&lEngine&7]");
        Config.init(this, true);
        BeanHolder.init(this);
        if (ApplicationConfig.use().enable) {
            LocalWebEngine.init(this);
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        LocalWebEngine.stop();
        Format.use().plugin().info("&cPlugin has been disabled");
        super.onDisable();
    }

    @EventHandler
    public void test(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack content : contents) {
            if (content.getType() != Material.AIR) {
                player.sendMessage(getItemNBTAsJson(content));
                break;
            }
        }
    }

    public static String getItemNBTAsJson(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = nmsCopy.save(new NBTTagCompound());
        return nbtTagCompound.toString();
    }

}
