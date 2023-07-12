package cn.dioxide.spigot.custom;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.common.util.ColorUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link CustomItem}采用建造者模式进行编写
 * 自定义物品从{@link CustomItem#with(Material)}方法开始通过响应式链进行构造
 * 正确的构造链式用法应当为{@code CustomItem.with().name().lore().enchant()...}
 *
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
public class CustomItem {

    ItemStack item;
    ItemMeta meta;
    final ArrayList<String> loreList = new ArrayList<>();

    private CustomItem(Material material) {
        this.item = new ItemStack(material);
        // 获取或创建物品的元数据
        this.meta = this.item.getItemMeta();
        if (this.meta == null) {
            this.meta = Bukkit.getItemFactory().getItemMeta(material);
            if (this.meta == null) {
                throw new NullPointerException("Failed to create ItemMeta for material " + material);
            }
            this.item.setItemMeta(this.meta);
        }
    }

    private CustomItem(ItemStack itemStack) {
        this.item = itemStack;
        // 获取或创建物品的元数据
        this.meta = itemStack.getItemMeta();
        if (this.meta == null) {
            throw new NullPointerException("No item meta provided");
        }
    }

    public static CustomItem with(Material material) {
        return new CustomItem(material);
    }

    public static CustomItem with(ItemStack itemStack) {
        return new CustomItem(itemStack);
    }

    public CustomItem lore(String... lore) {
        // Lore使用存储式延迟设置策略
        loreList.addAll(List.of(lore));
        return this;
    }

    public CustomItem name(String displayName) {
        this.meta.setDisplayName(ColorUtils.replace(displayName));
        return this;
    }

    public CustomItem enchant(@NotNull Enchantment enchant, int level, boolean glow) {
        this.meta.addEnchant(enchant, level, glow);
        return this;
    }

    public CustomItem attribute(@NotNull String name, double amount, @NotNull AttributeModifier.Operation operation, @Nullable EquipmentSlot slot, @NotNull Attribute attribute) {
        meta.addAttributeModifier(attribute, new AttributeModifier(
                UUID.randomUUID(),
                name,
                amount,
                operation,
                slot));
        return this;
    }

    public CustomItem attach(String skillLore) {
        if (this.item.getItemMeta() == null) throw new RuntimeException("Item doesn't have any meta");
        List<String> oldItemLore = this.item.getItemMeta().getLore();
        // 标记是否找到并替换了技能
        int foundIndex = -2;
        if (oldItemLore != null) {
            // 从skillLore中提取技能名称
            String skillName = skillLore.split(" ")[1];
            // 遍历loreList如果已经存在此次合成不修改词条
            for (int i = 0; i < oldItemLore.size(); i++) {
                if (oldItemLore.get(i).contains(ColorUtils.replace(skillName))) {
                    foundIndex = -1;
                    break;
                }
                if (oldItemLore.get(i).contains(ColorUtils.replace("§8[")) &&
                        oldItemLore.get(i).contains("§e") &&
                        oldItemLore.get(i).contains("§7§o") &&
                        oldItemLore.get(i).contains("§8]")) {
                    foundIndex = i;
                    break;
                }
            }
        }
        // >= 0 表示有可替换的选项
        if (foundIndex >= 0) {
            oldItemLore.set(foundIndex, ColorUtils.replace(skillLore));
            this.item.getItemMeta().setLore(oldItemLore);
        }
        // -2 表示oldItemLore为null或未进行过任何合成
        else if (foundIndex == -2) {
            loreList.add(0, ColorUtils.replace(skillLore));
        }
        return this;
    }

    public ItemStack build() {
        List<String> processedLoreList = loreList.stream()
                .map(ColorUtils::replace)
                .map(CustomItem::resolveExpression)
                .distinct()
                .collect(Collectors.toList());
        List<String> oldLore = this.meta.getLore();
        if (oldLore != null) {
            oldLore.addAll(processedLoreList);
            this.meta.setLore(oldLore.stream().distinct().toList());
        } else {
            this.meta.setLore(processedLoreList);
        }

        this.item.setItemMeta(this.meta);
        return this.item;
    }

    /**
     * 解析式函数，使用正则取出表达式 %r() 中的范围并代回替换
     */
    protected static String resolveExpression(String input) {
        StringBuilder output = new StringBuilder();
        Random random = new Random();
        // 定义正则表达式
        String patternString = "%r\\((\\d+)-(\\d+)\\)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);
        int lastIndex = 0;
        // 循环匹配替换
        while (matcher.find()) {
            // 添加非表达式部分
            output.append(input, lastIndex, matcher.start());
            // 获取表达式中的数字
            double minValue = Double.parseDouble(matcher.group(1));
            double maxValue = Double.parseDouble(matcher.group(2));
            // 随机数复用正态分布x~N(μ,σ^2)
            double mean = (maxValue + minValue) / 3.0;  // 均值 (max + min) / 3 中轴线偏向最小值
            double stdDev = (maxValue - minValue) / 5.0; // 标准差 (max + min) / 5 分布偏陡峭
            double randomValue = (int) (random.nextGaussian() * stdDev + mean);
            randomValue = Math.max(minValue, Math.min(maxValue, randomValue)); // 限制在[minValue, maxValue]范围内
            output.append(String.format("%.1f", randomValue)); // 保留一位小数
            lastIndex = matcher.end();
        }
        // 添加剩余部分
        output.append(input.substring(lastIndex));
        return output.toString();
    }

}
