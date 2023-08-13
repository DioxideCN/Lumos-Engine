package cn.dioxide.spigot.custom;

import cn.dioxide.common.extension.Pair;
import cn.dioxide.common.util.ColorUtils;
import cn.dioxide.spigot.custom.structure.ICustomSkillHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        if (this.item.getItemMeta() == null) {
            throw new RuntimeException("Item doesn't have any meta");
        }
        Map<String, Pair<String, ICustomSkillHandler>> map = CustomRegister.get();
        // 寻找匹配的技能
        Optional<Pair<String, ICustomSkillHandler>> addingSkillOpt = map.values()
                .stream()
                .filter(pair -> skillLore.contains(pair.left()))
                .findFirst();
        if (addingSkillOpt.isEmpty()) {
            return this;
        }
        Pair<String, ICustomSkillHandler> addingSkill = addingSkillOpt.get();
        List<String> oldItemLore = this.item.getItemMeta().getLore();
        if (oldItemLore == null) oldItemLore = new ArrayList<>();
        // 计算匹配的技能词条数量
        long matchedCount = oldItemLore.stream()
                .filter(lore -> map.values().stream().anyMatch(pair -> lore.contains(pair.left())))
                .count();
        int indexToUpdate = -1;
        for (int i = 0; i < oldItemLore.size(); i++) {
            if (oldItemLore.get(i).contains(addingSkill.left())) {
                indexToUpdate = i;
                break;
            }
        }
        if (indexToUpdate != -1 || matchedCount < 3) {
            this.loreList.add(0, ColorUtils.replace(skillLore));
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
            this.meta.setLore(removeDuplicatesBasedOnSecondElement(oldLore));
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

    private List<String> removeDuplicatesBasedOnSecondElement(List<String> oldLore) {
        // 使用LinkedHashMap来维护插入顺序
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        for (String str : oldLore) {
            String[] parts = str.split(" ");
            if (parts.length >= 2) {
                map.put(parts[1], str);
            } else {
                map.put(str, str); // 对于没有空格的情况，直接使用整个字符串作为键
            }
        }

        // LinkedHashMap会按照插入顺序返回values，所以最后一个插入的值（即最新的值）会被保留
        return new ArrayList<>(map.values());
    }

}
