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

    private ItemStack item;
    private ItemMeta meta;

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

    public static CustomItem with(Material material) {
        return new CustomItem(material);
    }

    public CustomItem lore(String... lore) {
        this.meta.setLore(
                Arrays.stream(lore)
                        .map(ColorUtils::replace)
                        .collect(Collectors.toList()));
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

    @Deprecated
    @Unsafe(proposer = "Dioxide_CN")
    public CustomItem glow() {
        // glow光效在spigot-api中并没有显示的方法 所以只能使用NMS来合并NBT
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        // 预备合并的CompoundTag
        CompoundTag merge = new CompoundTag() {{
            putShort("id", (short) -1);
            putShort("lvl", (short) 0);
        }}, ench = new CompoundTag() {{
            put("ench", merge);
        }};
        // Compound是一个可能为空的值对象 这里使用Optional来处理NPE
        nmsItem.setTag(
                Optional.ofNullable(nmsItem.getTag())
                        .orElse(new CompoundTag())
                        // 原生tag与enchTag进行合并并代回nmsItem
                        .merge(ench));
        // 将NMS物品转换回CraftBukkit可识别物品
        this.item = CraftItemStack.asBukkitCopy(nmsItem);
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }

}
