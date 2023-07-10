package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.annotation.LoopThis;
import cn.dioxide.spigot.LumosStarter;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * @author Dioxide.CN
 * @date 2023/6/26
 * @since 1.0
 */
@Event
public class HorrifyWardenFeature implements Listener {

    private static final Map<UUID, Long> wardenCheckStartTime = new HashMap<>();
    private static final Map<UUID, Boolean> isWardenConsistentlyTrapped = new HashMap<>();

    // @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.WARDEN) {
            Warden warden = (Warden) event.getEntity();
            Location spawnLocation = warden.getLocation();
            // 21x21x21内的尖啸体设置为不可用
            List<Block> blocks = getRegionBlocks(spawnLocation, 10, 10, 10);
            for (Block block : blocks) {
                if (block.getType() == Material.SCULK_SHRIEKER) {
                    if (block.getBlockData() instanceof SculkShrieker sculkShrieker) {
                        sculkShrieker.setCanSummon(false);
                        block.setBlockData(sculkShrieker);
                    }
                }
            }
        }
    }

    /**
     * 监守者不会受到爆炸、冻伤、仙人掌、凋零、中毒、窒息、挤压、摔落、重力方块类型的伤害
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // 检查受害实体是否是坚守者
        if (event.getEntityType() == EntityType.WARDEN) {
            // 检查伤害是否是爆炸伤害
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.FREEZE ||
                    event.getCause() == EntityDamageEvent.DamageCause.WITHER ||
                    event.getCause() == EntityDamageEvent.DamageCause.POISON ||
                    event.getCause() == EntityDamageEvent.DamageCause.CONTACT ||
                    event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION ||
                    event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                    event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
                // 取消爆炸伤害和冷冻伤害
                event.setCancelled(true);
            }
        }
    }

    /**
     * 箭矢伤害负相关函数
     */
    @EventHandler
    public void onWardenDamageByArrow(EntityDamageByEntityEvent event) {
        // 检查是否是监守者受到箭的伤害
        if (event.getEntity() instanceof Warden warden && event.getDamager() instanceof AbstractArrow arrow) {
            // 检查箭是否有已知的发射源
            if (arrow.getShooter() instanceof Entity shooter) {
                // 计算发射源与监守者之间的距离
                double distance = shooter.getLocation().distance(warden.getLocation());
                // 根据距离调整伤害
                double damage = event.getDamage();
                if (distance > 6) {
                    // 距离超过6时，伤害开始递减
                    double scale = Math.max(1 - (distance - 6) / 9, 0); // 在距离为15时，比例为0
                    damage *= scale;
                }
                // 设置新的伤害值
                event.setDamage(damage);
            }
        }
    }

    /**
     * 监守者无法穿过地狱门或末地门
     */
    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        // 检查实体是否是监守者
        if (event.getEntityType() == EntityType.WARDEN) {
            // 取消事件，阻止监守者穿过传送门
            event.setCancelled(true);
        }
    }

    /**
     * 监守者在死亡后会产生强度为10的无火焰的范围性爆炸
     * 并在爆炸后的爆炸中心生成一个奖励箱
     * 奖励箱会包含5-16个铁锭、5-8颗钻石、2-3本附魔书（必定包含1-2本经验修补）、1-3片回响碎片、0-1个幽静模板、3-8个附魔之瓶
     */
    public static void onWardenDeath(EntityDeathEvent event) {
        // 检查死亡的实体是否是监守者
        if (event.getEntity() instanceof Warden warden) {
            if (warden.getScoreboardTags().contains("warden.slave")) return;
            Location location = warden.getLocation();
            replaceBlocks(location);
            if (location.getWorld() == null) return;
            location.getWorld().createExplosion(location, 10.0F, false, true);
            generateSculkShrieker(location);
            createRewardChest(warden, location);
            // 清除任务
            wardenCheckStartTime.remove(warden.getUniqueId());
            isWardenConsistentlyTrapped.remove(warden.getUniqueId());
            wardenAgitatedTaskIDs.remove(warden.getUniqueId());
        }
    }

    // ***************** 定时任务

    // @LoopThis
    public static void wardenScheduler() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Warden warden) {
                    angerAtPlayerNearby(warden);
                    if (!warden.getScoreboardTags().contains("warden.slave")) {
                        wardenAngryAbility(warden);
                        handleWardenTrapped(warden);
                        evaporativeFluid(warden);
                        // 从这里开始恶心玩家
                        wardenAgitatedAction(warden);
                        wardenAngryAction(warden);
                    }
                }
            }
        }
    }

    /**
     * 监守者会对附近的玩家表现出异常的愤怒，它每秒会对6x6x6范围内的每个玩家增加16点愤怒值
     */
    public static void angerAtPlayerNearby(Warden warden) {
        for (Entity nearbyEntity : warden.getNearbyEntities(6, 6, 6)) {
            if (nearbyEntity instanceof Player player) {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    warden.increaseAnger(player, 10);
                }
            }
        }
    }

    private static final Map<UUID, Integer> wardenAgitatedTaskIDs = new HashMap<>();

    /**
     * 监守者的最大愤怒值达到40时会每隔18秒为半径20格内的玩家加持13秒的缓慢I、黑暗、虚弱I药水效果
     */
    public static void wardenAgitatedAction(Warden warden) {
        if (warden.getScoreboardTags().contains("warden.slave")) return;
        if (warden.getAngerLevel() == Warden.AngerLevel.AGITATED && !wardenAgitatedTaskIDs.containsKey(warden.getUniqueId())) {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            int taskID = scheduler.scheduleSyncRepeatingTask(LumosStarter.INSTANCE, () -> {
                if (warden.getAnger() >= 30 && warden.getAnger() <= 50) {
                    for (Entity nearbyEntity : warden.getNearbyEntities(20, 20, 20)) {
                        if (nearbyEntity instanceof Player player) {
                            effectDebuff2Player(player, 13);
                        }
                    }
                }
            }, 0L, 300L);
            wardenAgitatedTaskIDs.put(warden.getUniqueId(), taskID);
        } else if (warden.getAngerLevel() != Warden.AngerLevel.AGITATED && wardenAgitatedTaskIDs.containsKey(warden.getUniqueId())) {
            // Warden is no longer angry, cancel the task
            int taskID = wardenAgitatedTaskIDs.get(warden.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskID);
            wardenAgitatedTaskIDs.remove(warden.getUniqueId());
        }
    }

    /**
     * 蒸发11x11x11范围内的水源、含水方块、岩浆
     */
    private static void evaporativeFluid(Warden warden) {
        if(warden.getAngerLevel() == Warden.AngerLevel.ANGRY) {
            List<Block> blocks = getRegionBlocks(warden.getLocation(), 5, 5, 5);
            for (Block block : blocks) {
                if (block.getType() == Material.WATER ||
                        block.getType() == Material.LAVA) {
                    block.setType(Material.AIR);
                }
                if (block.getBlockData() instanceof Waterlogged waterlogged) {
                    // 检查方块是否为含水方块
                    if (waterlogged.isWaterlogged()) {
                        // 设置含水状态为false并更新方块
                        waterlogged.setWaterlogged(false);
                        block.setBlockData(waterlogged);
                    }
                }
            }
        }
    }

    private static final Map<UUID, Integer> wardenAngryTaskIDs = new HashMap<>();

    /**
     * 监守者的最大愤怒值达到80时会每隔18秒破坏21x21x21范围内的所有钟、发射器、投掷器、黑曜石、哭泣黑曜石、矿车、船
     * 同时增加其20%~80%的伤害抗性，并为半径21格内的玩家加持13秒的缓慢I、黑暗、虚弱药水效果
     * 如果24x24x24范围内没有warden.slave则尝试生成一个
     */
    public static void wardenAngryAction(Warden warden) {
        if (warden.getScoreboardTags().contains("warden.slave")) return;
        if (warden.getAngerLevel() == Warden.AngerLevel.ANGRY && !wardenAngryTaskIDs.containsKey(warden.getUniqueId())) {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            int taskID = scheduler.scheduleSyncRepeatingTask(LumosStarter.INSTANCE, () -> {
                List<Block> blocks = getRegionBlocks(warden.getLocation(), 10, 10, 10);
                for (Block block : blocks) {
                    if (block.getType() == Material.OBSIDIAN ||
                            block.getType() == Material.CRYING_OBSIDIAN ||
                            block.getType() == Material.BELL ||
                            block.getType() == Material.REPEATER ||
                            block.getType() == Material.DISPENSER) {
                        block.setType(Material.SCULK);
                    }
                }
                int playerCount = 0;
                for (Entity nearbyEntity : warden.getNearbyEntities(21, 21, 21)) {
                    if (nearbyEntity instanceof Player player) {
                        playerCount++;
                        effectDebuff2Player(player, 15);
                    }
                    if (nearbyEntity.getType() == EntityType.BOAT ||
                            nearbyEntity.getType() == EntityType.CHEST_BOAT ||
                            nearbyEntity.getType() == EntityType.MINECART ||
                            nearbyEntity.getType() == EntityType.MINECART_CHEST ||
                            nearbyEntity.getType() == EntityType.MINECART_COMMAND ||
                            nearbyEntity.getType() == EntityType.MINECART_FURNACE ||
                            nearbyEntity.getType() == EntityType.MINECART_HOPPER ||
                            nearbyEntity.getType() == EntityType.MINECART_TNT ||
                            nearbyEntity.getType() == EntityType.MINECART_MOB_SPAWNER) {
                        nearbyEntity.remove();
                    }
                }
                // 20%~100%伤害抗性10s
                warden.addPotionEffect(new PotionEffect(
                        PotionEffectType.DAMAGE_RESISTANCE,
                        260,
                        // 0-1 80% 2 60% 3 40% > 3 20%
                        playerCount <= 1 ? 3 : playerCount == 2 ? 2 : playerCount == 3 ? 1 : 0,
                        true,
                        false,
                        false));
                splitWarden(warden);
                wardenLifeRecovery(warden);
            }, 0L, 480L);
            wardenAngryTaskIDs.put(warden.getUniqueId(), taskID);
        } else if (warden.getAngerLevel() != Warden.AngerLevel.ANGRY && wardenAngryTaskIDs.containsKey(warden.getUniqueId())) {
            // Warden is no longer angry, cancel the task
            int taskID = wardenAngryTaskIDs.get(warden.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskID);
            wardenAngryTaskIDs.remove(warden.getUniqueId());
        }
    }

    /**
     * warden会从slaveWarden上吸血
     */
    private static void wardenLifeRecovery(Warden warden) {
        if (warden.getHealth() < 250) {
            for (Entity nearbyEntity : warden.getNearbyEntities(16, 16, 16)) {
                if (nearbyEntity instanceof Warden slaveWarden
                        && slaveWarden.getScoreboardTags().contains("warden.slave")
                        && slaveWarden.getHealth() > 160) {
                    // 抽取25%的生命值
                    double amountToTransfer = 50.0D;
                    if (slaveWarden.getHealth() - amountToTransfer < 160) {
                        amountToTransfer = slaveWarden.getHealth() - 160;
                    }
                    slaveWarden.setHealth(slaveWarden.getHealth() - amountToTransfer);
                    warden.setHealth(warden.getHealth() + amountToTransfer);
                    slaveWarden.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
                    break;
                }
            }
        }
    }

    /**
     * 处于愤怒的监守者会每24秒尝试生成一个分裂体，分裂体是普通的监守者不会具有监守者本体的能力，但同样会免疫一些伤害和不合理事件
     */
    public static void splitWarden(Warden warden) {
        if (warden.getScoreboardTags().contains("warden.slave")) return;
        if (warden.getAngerLevel() == Warden.AngerLevel.ANGRY) {
            int nearbyPlayersCount = 0;
            int nearbySlaveWardensCount = 0;
            for (Entity nearbyEntity : warden.getNearbyEntities(24, 24, 24)) {
                if (nearbyEntity instanceof Player) {
                    nearbyPlayersCount++;
                }
                if (nearbyEntity instanceof Warden slaveWarden) {
                    if (slaveWarden.getScoreboardTags().contains("warden.slave")) {
                        nearbySlaveWardensCount++;
                    }
                }
            }
            if (nearbySlaveWardensCount == 0) {
                splitWardenAction(warden);
            }
        }
    }

    /**
     * 没有slaveWarden存在，则尝试生成一个血量为250，伤害为15，tag为"warden.slave"的warden
     */
    private static void splitWardenAction(Warden warden) {
        // 获取warden面朝的方向并计算左右的方向
        Vector direction = warden.getLocation().getDirection();
        Vector leftDirection = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Vector rightDirection = new Vector(direction.getZ(), 0, -direction.getX()).normalize();
        // 获取新的位置
        Location newWardenLocation = warden.getLocation().clone().add(rightDirection);
        // 生成新的warden
        Warden newWarden = (Warden) warden.getWorld().spawnEntity(newWardenLocation, EntityType.WARDEN);
        // 设置新的warden的属性
        AttributeInstance maxHealthAttribute = newWarden.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(250);
            newWarden.setHealth(250);
        }
        AttributeInstance attackDamageAttribute = newWarden.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            attackDamageAttribute.setBaseValue(15);
        }
        newWarden.addScoreboardTag("warden.slave");
        // 给原始的warden和新的warden添加推力
        double pushAmount = 0.6;
        warden.setVelocity(leftDirection.multiply(pushAmount));
        newWarden.setVelocity(rightDirection.multiply(pushAmount));
        // 播放声音
        warden.getWorld().playSound(newWardenLocation,
                Sound.ENTITY_WARDEN_EMERGE,
                SoundCategory.BLOCKS,
                2.0f,
                1.0f);
    }

    /**
     * 监守者的最大愤怒值超过80会无视并破坏阻挡其前进路径上的所有非基岩、非强化深板岩方块
     */
    private static void wardenAngryAbility(Warden warden) {
        if (warden.getAngerLevel() == Warden.AngerLevel.ANGRY) {
            Location wardenLocation = warden.getLocation();
            Vector direction = wardenLocation.getDirection().normalize();
            // 计算要检查的区域的中心位置
            Location centerLocation = wardenLocation.clone().add(direction).add(0, 1, 0);
            // 获取世界对象以便访问方块
            World world = wardenLocation.getWorld();
            // 遍历3x3平面的方块
            int dx, dz;
            if (Math.abs(direction.getX()) > Math.abs(direction.getZ())) {
                dx = 0;
                dz = 1;
            } else {
                dx = 1;
                dz = 0;
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = 0; y <= 2; y++) {
                        // 计算每个方块的位置
                        Location blockLocation = centerLocation.clone().add(x * dx, y, z * dz);
                        assert world != null;
                        Block block = world.getBlockAt(blockLocation);
                        // 检查方块是否是基岩或强化深板岩，如果不是则破坏
                        if (block.getType() != Material.BEDROCK &&
                                block.getType() != Material.REINFORCED_DEEPSLATE) {
                            block.breakNaturally();
                        }
                    }
                }
            }
        }
    }

    /**
     * 若监守者被困于坑洞中则会尝试传送到8x8x8范围内任意一个玩家的背后
     */
    public static void handleWardenTrapped(Warden warden) {
        // warden愤怒的对象
        LivingEntity entityAngryAt = warden.getEntityAngryAt();
        if (entityAngryAt == null) return;

        if (entityAngryAt instanceof Player) {
            UUID wardenId = warden.getUniqueId();
            if (!wardenCheckStartTime.containsKey(wardenId)) {
                wardenCheckStartTime.put(wardenId, System.currentTimeMillis());
                isWardenConsistentlyTrapped.put(wardenId, true);
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        // 检查3秒内是否一直被困
                        if (!isWardenTrapped(warden, (Player) entityAngryAt)) {
                            isWardenConsistentlyTrapped.put(wardenId, false);
                        }
                        ticks++;
                        // 6秒后执行传送并移除缓存
                        if (ticks >= 120) {
                            if (isWardenConsistentlyTrapped.getOrDefault(wardenId, false)) {
                                teleportWardenNearPlayer(warden, (Player) entityAngryAt);
                            }
                            wardenCheckStartTime.remove(wardenId);
                            isWardenConsistentlyTrapped.remove(wardenId);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(LumosStarter.INSTANCE, 0L, 1L);
            }
        }
    }

    /**
     * 检查监守者到玩家间是否有方块阻挡
     */
    private static boolean isWardenTrapped(Warden warden, Player player) {
        Location wardenLocation = warden.getLocation();
        Location playerLocation = player.getLocation();
        for (double fraction = 0.1; fraction <= 1.0; fraction += 0.1) {
            double x = wardenLocation.getX() + fraction * (playerLocation.getX() - wardenLocation.getX());
            double y = wardenLocation.getY() + fraction * (playerLocation.getY() - wardenLocation.getY());
            double z = wardenLocation.getZ() + fraction * (playerLocation.getZ() - wardenLocation.getZ());
            Location checkLocation = new Location(wardenLocation.getWorld(), x, y, z);
            if (checkLocation.getBlock().getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将监守者传送到player身后
     */
    private static void teleportWardenNearPlayer(Warden warden, Player player) {
        Location playerLocation = player.getLocation();
        if (player.getHealth() < 8) {
            return;
        }
        Random random = new Random();
        for (int attempts = 0; attempts < 10; attempts++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dx = Math.cos(angle) * 5;
            double dz = Math.sin(angle) * 5;
            Location targetLocation = playerLocation.clone().add(dx, 0, dz);
            if (isSafeLocation(targetLocation)) {
                double distance = targetLocation.distance(playerLocation);
                if (distance <= 8) {
                    warden.teleport(targetLocation);
                    return;
                }
            }
        }
    }

    /**
     * 检查传送位置是否安全
     */
    private static boolean isSafeLocation(Location location) {
        // 检查传送位置是否安全
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block ground = feet.getRelative(BlockFace.DOWN);

        return ground.getType().isSolid() && isNonSolid(feet.getType()) && isNonSolid(head.getType());
    }

    /**
     * 位置是否是流体
     */
    private static boolean isNonSolid(Material material) {
        return material.isAir() || !material.isSolid() || material == Material.WATER || material == Material.LAVA;
    }

    // ***************** 私有工具

    /**
     * 给玩家施加黑暗、缓慢、虚弱DEBUFF
     */
    private static void effectDebuff2Player(Player player, int duration) {
        if (player.getGameMode() == GameMode.SURVIVAL) {
            player.addPotionEffects(new ArrayList<>() {{
                // 黑暗
                add(new PotionEffect(PotionEffectType.DARKNESS,
                        20 * duration,
                        0,
                        true,
                        false,
                        false));
                // 缓慢
                add(new PotionEffect(PotionEffectType.SLOW,
                        20 * duration,
                        0,
                        true,
                        false,
                        false));
                // 虚弱
                add(new PotionEffect(PotionEffectType.WEAKNESS,
                        20 * duration,
                        0,
                        true,
                        false,
                        false));
            }});
        }
    }

    /**
     * 从对角loc1到对角loc2对称区域内的所有方块
     */
    private static List<Block> getRegionBlocks(Location location, int fx, int fy, int fz) {
        Location loc1 = location.clone().add(fx, fy, fz);
        Location loc2 = location.clone().subtract(fx, fy, fz);
        return getRegionBlocks(loc1, loc2);
    }

    /**
     * 从对角loc1到对角loc2区域内的所有方块
     */
    private static List<Block> getRegionBlocks(Location loc1, Location loc2) {
        List<Block> blocks = new ArrayList<>();
        // 获取坐标xyz
        int x1 = loc1.getBlockX();
        int y1 = loc1.getBlockY();
        int z1 = loc1.getBlockZ();
        int x2 = loc2.getBlockX();
        int y2 = loc2.getBlockY();
        int z2 = loc2.getBlockZ();
        // 遍历区域坐标
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    if (loc1.getWorld() == null) return blocks;
                    blocks.add(loc1.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    /**
     * 监守者死亡时同时会向21x5x21范围内播撒1-3个幽匿尖啸体
     */
    private static void generateSculkShrieker(Location location) {
        Location loc1 = location.clone().add(10, 2, 10);
        Location loc2 = location.clone().subtract(10, 2, 10);
        List<Block> blocks = getRegionBlocks(loc1, loc2);
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        List<Block> eligibleBlocks = new ArrayList<>();
        for (Block block : blocks) {
            if (isValidPlacement(block)) {
                eligibleBlocks.add(block);
            }
        }
        Random random = new Random();
        int blockCount = random.nextInt(2) + 1; // 1-2
        for (int i = 0; i < blockCount; i++) {
            if (eligibleBlocks.isEmpty()) break;
            int randomIndex = random.nextInt(eligibleBlocks.size());
            Block randomBlock = eligibleBlocks.remove(randomIndex);
            Block generator = world.getBlockAt(randomBlock.getLocation().add(0, 1, 0));
            generator.setType(Material.SCULK_SHRIEKER);
            if (generator.getBlockData() instanceof SculkShrieker sculkShrieker) {
                sculkShrieker.setCanSummon(true);
                generator.setBlockData(sculkShrieker);
            }
        }
    }

    /**
     * 检查方块上下是否有空间放置
     */
    @SuppressWarnings("all")
    private static boolean isValidPlacement(Block block) {
        // 获取方块上方和下方的方块
        Block blockAbove = block.getRelative(BlockFace.UP);
        Block blockBelow = block.getRelative(BlockFace.DOWN);
        // 检查上方是否是 AIR
        boolean isAirAbove = blockAbove.getType() == Material.AIR;
        // 检查下方方块是否不是半透明方块
        boolean isOpaqueBelow = !blockBelow.getType().isTransparent();
        // 返回结果
        return isAirAbove && isOpaqueBelow;
    }

    /**
     * 死亡后替换方块
     */
    private static void replaceBlocks(Location location) {
        List<Block> blocks = getRegionBlocks(location, 5, 6, 5);
        // 遍历方块并替换黑曜石、水、岩浆、含水方块
        for (Block block : blocks) {
            if (block.getType() == Material.OBSIDIAN || block.getType() == Material.CRYING_OBSIDIAN) {
                block.setType(Material.SCULK);
                continue;
            }
            if (block.getType() == Material.WATER) {
                block.setType(Material.AIR);
                continue;
            }
            if (block.getBlockData() instanceof Waterlogged waterlogged) {
                // 检查方块是否为含水方块
                if (waterlogged.isWaterlogged()) {
                    // 设置含水状态为false并更新方块
                    waterlogged.setWaterlogged(false);
                    block.setBlockData(waterlogged);
                }
            }
        }
    }

    /**
     * 生成奖励箱
     */
    @SuppressWarnings("all")
    public static void createRewardChest(Warden warden, Location deathLocation) {
        // 在死亡地点设置箱子
        Block block = deathLocation.getBlock();
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        int playerCounter = 0;
        List<Entity> nearbyEntities = warden.getNearbyEntities(24, 24, 24);
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof Player) playerCounter++;
        }
        NamespacedKey key;
        // 设置LootTable
        if (playerCounter >= 3) {
            key = new NamespacedKey("minecraft", "chests/warden_legend");
        } else {
            key = new NamespacedKey("minecraft", "chests/warden_treasure");
        }
        LootTable lootTable = Bukkit.getLootTable(key);
        if (lootTable != null) {
            chest.setLootTable(lootTable);
            chest.update();
        } else {
            // 处理LootTable找不到的情况
            System.out.println("LootTable not found");
        }
    }

    // @LoopThis(period = 60L)
    public static void cleanUpMaps() {
        // List of Maps to clean up
        doCleanUpMaps(wardenCheckStartTime);
        doCleanUpMaps(isWardenConsistentlyTrapped);
        doCleanUpMaps(wardenAgitatedTaskIDs);
    }

    private static void doCleanUpMaps(Map<UUID, ?> map) {
        Iterator<UUID> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = Bukkit.getEntity(uuid);
            // If the entity does not exist, remove the UUID from the map
            if (entity == null) {
                iterator.remove();
            }
        }
    }

}
