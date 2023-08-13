package cn.dioxide.spigot.command;

import cn.dioxide.common.annotation.Executor;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.extension.Pair;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.custom.CustomItem;
import cn.dioxide.spigot.custom.CustomRegister;
import cn.dioxide.spigot.custom.structure.DefaultSkillHelper;
import cn.dioxide.spigot.custom.structure.Factor;
import cn.dioxide.spigot.custom.structure.ICustomSkillHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
@Executor(name = "custom")
public class CustomCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!label.equals("custom")) {
            return false;
        }
        if (args.length == 0) {
            return pluginHelper(player);
        }
        if (args.length == 1) {
            if ("temperature".equals(args[0])) {
                float temperature = Factor.getTemperatureFactor(player);
                // 要获取玩家所在生物群系的全路径名称需要NMS的辅佐
                ServerLevel nmsWorld = PlayerUtils.getNMSWorld(player);
                Biome biome = PlayerUtils.getBiome(player);
                if (nmsWorld == null || biome == null) return false;
                Optional<ResourceKey<Object>> resourceKey = nmsWorld.registryAccess()
                        .registry(ResourceKey.createRegistryKey(ResourceLocation.tryParse("worldgen/biome")))
                        .flatMap(registry -> registry.getResourceKey(biome));
                if (resourceKey.isPresent()) {
                    Format.use().player().noticePrefix(player,
                            resourceKey.get().location().toString() + " 的温度为 " + temperature);
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (args.length == 2) {
            if ("get".equals(args[0])) {
                if (CustomRegister.getKeySet().contains(args[1])) {
                    player.getInventory().addItem(CustomRegister.get(args[1]));
                } else {
                    Format.use().player().noticePrefix(player, "&c物品不存在");
                }
                return true;
            }
            if ("attach".equals(args[0])) {
                List<String> list = CustomRegister.get().keySet().stream().toList();
                if (list.contains(args[1])) {
                    Pair<String, ICustomSkillHandler> pair = CustomRegister.get().get(args[1]);
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    ItemStack newHand = DefaultSkillHelper.simpleAttach(mainHand, pair.right());
                    if (newHand != null) {
                        player.getInventory().setItemInMainHand(newHand);
                    } else {
                        Format.use().player().noticePrefix(player, "&c该强力附魔无法添加到你手持的物品类型上");
                    }
                } else {
                    Format.use().player().noticePrefix(player, "&c该强力附魔不存在");
                }
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabHelper = Arrays.asList("help", "get", "attach", "temperature");
        if (args.length == 1) {
            return tabHelper;
        }
        if (args.length == 2) {
            if ("get".equals(args[0])) {
                return CustomRegister.getKeySet();
            }
            if ("attach".equals(args[0])) {
                return CustomRegister.get().keySet().stream().toList();
            }
        }
        return null;
    }

    public static boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&7插件指南 &f版本: &71.0.0");
        Format.use().player().noticeCommand(p, "custom get <item>", "获取物品");
        return true;
    }

}
