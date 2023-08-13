package cn.dioxide.common.extension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Dioxide.CN
 * @date 2023/7/27
 * @since 1.0
 */
@Getter
@Setter
public class SnakeConfig {
    private final String name;
    private final String world;
    private final String block;
    private final int length;
    private final List<Position> positions;

    public SnakeConfig(String name, String world, String block, int length, List<Position> positions) {
        this.name = name;
        this.world = world;
        this.block = block;
        this.length = length;
        this.positions = positions;
    }

    // 从 ConfigurationSection 解析 SnakeConfig
    public static SnakeConfig fromConfigurationSection(ConfigurationSection section) {
        String name = section.getString("name");
        String world = section.getString("world");
        String block = section.getString("block");
        int length = section.getInt("length");

        List<Position> positions = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<List<Integer>> rawPositions = (List<List<Integer>>) section.getList("positions");
        if (rawPositions != null) {
            for (List<Integer> rawPosition : rawPositions) {
                positions.add(new Position(rawPosition.get(0), rawPosition.get(1), rawPosition.get(2)));
            }
        }

        return new SnakeConfig(name, world, block, length, positions);
    }

    private int currentStep = 0; // 当前的移动步数
    private BukkitTask task;
    public void move() {
        if (this.positions.size() < this.length) {
            throw new IllegalStateException("The snake is longer than the path!");
        }
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
            // 将当前路径上的所有方块设置为空气，然后重置步数
            for (Position pos : this.positions) {
                Objects.requireNonNull(Bukkit.getWorld(this.world)).getBlockAt(pos.x(), pos.y(), pos.z()).setType(Material.AIR);
            }
            this.currentStep = 0;
        }
        Material snakeBlock = Material.valueOf(this.block); // 解析方块类型
        this.task = Bukkit.getScheduler().runTaskTimer(Config.get().plugin, () -> {
            // 遍历滑动窗口内的所有方块
            for (int i = 0; i <= this.length; i++) {
                int posIndex = this.currentStep - i;
                if (posIndex < 0 || posIndex >= this.positions.size()) {
                    continue; // 跳过窗口外的位置
                }
                Position pos = this.positions.get(posIndex);
                Block block = Objects.requireNonNull(Bukkit.getWorld(this.world)).getBlockAt(pos.x(), pos.y(), pos.z());
                if (i == this.length) {
                    // 如果是窗口的最后一个方块，设置为空气
                    block.setType(Material.AIR);
                } else {
                    // 其他方块设置为蛇的方块
                    block.setType(snakeBlock);
                }
            }
            this.currentStep++; // 移动步数增加
            // 如果蛇的尾部已经移出了路径的末尾，取消任务
            if (this.currentStep >= this.positions.size() + this.length) {
                this.currentStep = 0;
                this.task.cancel(); // 停止任务
                this.task = null;
            }
        }, 0L, 4L); // 开始延迟为0，间隔为8个tick（0.2秒）
    }
}
