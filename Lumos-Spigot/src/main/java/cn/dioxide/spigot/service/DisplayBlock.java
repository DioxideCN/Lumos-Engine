package cn.dioxide.spigot.service;

import cn.dioxide.common.extension.Format;
import cn.dioxide.common.util.CalcUtils;
import cn.dioxide.common.util.MatrixUtils;
import cn.dioxide.common.extension.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/6/19
 * @since 1.0
 */
public class DisplayBlock {

    public boolean placeItemDisplay(Player p, String[] args, boolean canRecycle) {
        if (!p.hasPermission("lumos.display.block.place")) {
            Format.use().player().noticePrefix(p, "&c你没有放置方块展示实体的权限");
            return true;
        }
        // display item [x/help [1]] [y [2]] [z [3]] [r1 [4]] [r2 [5]] [r3 [6]] [s [7]] [t [8]] [t/f [9]]
        double x, y, z, sx, sy, sz; // x,y,z可以为负
        int rx, ry, rz; // 可为负数
        try {
            // 尝试将索引 1，2，3 上的字符串解析为 double 类型
            if (args[1].startsWith("~")) {
                if (args[1].length() > 1) {
                    x = p.getLocation().getX() + Double.parseDouble(args[1].substring(1));
                } else {
                    x = p.getLocation().getX();
                }
            } else {
                x = Double.parseDouble(args[1]);
            }

            if (args[2].startsWith("~")) {
                if (args[2].length() > 1) {
                    y = p.getLocation().getY() + Double.parseDouble(args[2].substring(1));
                } else {
                    y = p.getLocation().getY();
                }
            } else {
                y = Double.parseDouble(args[2]);
            }

            if (args[3].startsWith("~")) {
                if (args[3].length() > 1) {
                    z = p.getLocation().getZ() + Double.parseDouble(args[3].substring(1));
                } else {
                    z = p.getLocation().getZ();
                }
            } else {
                z = Double.parseDouble(args[3]);
            }
            sx = Double.parseDouble(args[7]);
            sy = Double.parseDouble(args[8]);
            sz = Double.parseDouble(args[9]);

            // 尝试将索引 4，5 上的字符串解析为 int 类型
            rx = Integer.parseInt(args[4]);
            ry = Integer.parseInt(args[5]);
            rz = Integer.parseInt(args[6]);
        } catch (NumberFormatException e) {
            Format.use().player().noticePrefix(p, "&c参数类型错误，请确保你的小数和整数部分是正确的");
            return true;
        }
        // 限制范围
        if (CalcUtils.isOutOfRange(p, x, y, z, Config.get().display.block.placeRadius)) {
            Format.use().player().noticePrefix(p, "&c放置的半径不能超过" + Config.get().display.block.placeRadius + "格");
            return true;
        }
        if (sx < Config.get().display.block.scale[0][0] || sx > Config.get().display.block.scale[0][1]) {
            Format.use().player().noticePrefix(p, "&cX轴缩放的比例不能小于 &f" +
                    String.format("%.1f", Config.get().display.item.scale[0][0]) +
                    " &c也不能大于 &f" +
                    String.format("%.1f", Config.get().display.item.scale[0][1]));
            return true;
        }
        if (sy < Config.get().display.block.scale[1][0] || sy > Config.get().display.block.scale[1][1]) {
            Format.use().player().noticePrefix(p, "&cY轴缩放的比例不能小于 &f" +
                    String.format("%.1f", Config.get().display.item.scale[1][0]) +
                    " &c也不能大于 &f" +
                    String.format("%.1f", Config.get().display.item.scale[1][1]));
            return true;
        }
        if (sz < Config.get().display.block.scale[2][0] || sz > Config.get().display.block.scale[2][1]) {
            Format.use().player().noticePrefix(p, "&cZ轴缩放的比例不能小于 &f" +
                    String.format("%.1f", Config.get().display.item.scale[2][0]) +
                    " &c也不能大于 &f" +
                    String.format("%.1f", Config.get().display.item.scale[2][1]));
            return true;
        }
        ItemStack itemInHand = p.getInventory().getItemInMainHand(); // 获取玩家主手中的物品
        if (itemInHand.getType().equals(Material.AIR)) {
            Format.use().player().noticePrefix(p, "&c手持物品不能为空气");
            return true;
        }
        if (itemInHand.getType().isBlock()) {
            Matrix4f matrix = MatrixUtils.getMatrix(rx, ry, rz, sx, sy, sz); // 生成仿射变换矩阵
            Material material = itemInHand.getType(); // 获取物品的类型
            BlockData blockData = material.createBlockData(); // 创建BlockData实例
            World world = p.getWorld(); // 获取玩家所在维度
            Location location = new Location(world, x, y, z); // 创建新的位置
            BlockDisplay display = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
            display.setTransformationMatrix(matrix);
            display.setBlock(blockData);
            // owner.true.Dioxide_CN
            display.addScoreboardTag("owner." + canRecycle + "." + p.getName());
            Format.use().player().noticePrefix(p, "&a已放置方块展示实体");
            if (!p.isOp() || !Config.get().display.block.consume) {
                // 非OP需要扣除
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            }
        } else {
            Format.use().player().noticePrefix(p, "&c手持物品不属于方块类型");
        }
        return true;
    }

    public boolean recycleItemDisplay(Player p) {
        if (!p.hasPermission("lumos.display.block.recycle")) {
            Format.use().player().noticePrefix(p, "&c你没有回收方块展示实体的权限");
            return true;
        }
        List<Entity> nearbyEntities = p.getNearbyEntities(2, 1, 2);
        int success = 0, fail = 0;
        for (Entity entity : nearbyEntities) {
            // 只遍历ItemDisplay
            if (entity instanceof BlockDisplay display) {
                // 这里最多只会遍历一次
                for (String tag : display.getScoreboardTags()) {
                    if (tag.contains("owner")) {
                        String[] split = tag.split("\\.");
                        if (split.length != 3) {
                            fail++;
                            break;
                        }
                        boolean canRecycle = Boolean.parseBoolean(split[1]);
                        String playerName = split[2];
                        if (p.getName().equals(playerName) || canRecycle || p.isOp()) {
                            BlockData blockData = display.getBlock();
                            // 从BlockData获取Material来创建新的ItemStack
                            ItemStack item = new ItemStack(blockData.getMaterial());
                            // 掉落物品
                            display.getWorld().dropItemNaturally(display.getLocation(), item);
                            display.remove();
                            success++;
                        } else {
                            fail++;
                        }
                        break;
                    }
                }
            }
        }
        Format.use().player().noticePrefix(p, "&7已回收附近的方块展示实体 &a成功 &f" + success + " 个 &c失败 &f" + fail + " 个");
        return true;
    }

    public boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&fBlock &7创建方块展示实体指南");
        Format.use().player().noticeCommand(p, "display block <x y z> <rx ry rz> <sx sy sz> [<bool>]", "生成方块展示实体");
        Format.use().player().noticePermission(p, "lumos.display.block.place");
        Format.use().player().noticeCommand(p, "display block recycle",  "回收距离自己" +
                Config.get().display.block.placeRadius +
                "格范围内的方块展示实体");
        Format.use().player().noticePermission(p, "lumos.display.block.recycle");
        return true;
    }

    public static ArrayList<String> tab(@NotNull String[] args, Player player) {
        ArrayList<String> blockHelper = new ArrayList<>();
        // display block [x/help/recycle/template] [y] [z] [r1] [r2] [r3] [scale] [bool]
        String x = String.format("%.2f", player.getLocation().getX());
        String y = String.format("%.2f", player.getLocation().getY());
        String z = String.format("%.2f", player.getLocation().getZ());
        // [x/help]
        if (args.length == 2) {
            blockHelper.add("help");
            blockHelper.add("recycle");
            blockHelper.add("~");
            blockHelper.add("~ ~");
            blockHelper.add("~ ~ ~");
            blockHelper.add(x);
            blockHelper.add(x + " " + y);
            blockHelper.add(x + " " + y + " " + z);
        }
        if (!"help".equals(args[1])) {
            switch (args.length) {
                case 3 -> { // [y]
                    blockHelper.add("~");
                    blockHelper.add("~ ~");
                    blockHelper.add(y);
                    blockHelper.add(y + " " + z);
                }
                case 4 -> { // [z]
                    blockHelper.add("~");
                    blockHelper.add(z);
                }
                case 11 -> {
                    blockHelper.add("true");
                    blockHelper.add("false");
                }
                default -> {}
            }
        }
        return blockHelper;
    }

}
