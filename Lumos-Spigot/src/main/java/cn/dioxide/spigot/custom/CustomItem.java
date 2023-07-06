package cn.dioxide.spigot.custom;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.common.util.ColorUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
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

    private @NotNull ItemStack item;
    private @NotNull final ItemMeta meta;

    private CustomItem(Material material) {
        this.item = new ItemStack(material);
        // 确保meta不为null
        if (item.hasItemMeta()) {
            this.meta = Objects.requireNonNull(this.item.getItemMeta());
        } else {
            throw new NullPointerException("Material " + material + " must has a meta");
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

    @Unsafe(proposer = "Dioxide_CN")
    public CustomItem glow() {
        // glow光效在spigot-api中并没有显示的方法 所以只能使用NMS来合并NBT
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        // Compound是一个可能为空的值对象 这里使用Optional来处理NPE
        CompoundTag tag = Optional.ofNullable(nmsItem.getTag()).orElse(new CompoundTag());
        // 预备合并的CompoundTag
        CompoundTag ench = new CompoundTag(), merge = new CompoundTag();
        merge.putShort("id", (short) -1);
        merge.putShort("lvl", (short) 0);
        ench.put("ench", merge);
        // 原生tag与enchTag进行合并并代回nmsItem
        tag.merge(ench);
        nmsItem.setTag(tag);
        // 将NMS物品转换回CraftBukkit可识别物品
        this.item = CraftItemStack.asBukkitCopy(nmsItem);
        return this;
    }

    public CustomItem recipe() {
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }

}
