package cn.dioxide.common.extension;

import cn.dioxide.common.infra.EffectTarget;
import cn.dioxide.common.infra.EventType;
import cn.dioxide.common.infra.TrimUpgradeStore;
import cn.dioxide.common.infra.WhiteList;
import cn.dioxide.common.util.ConvertUtils;
import cn.dioxide.common.util.DecimalUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dioxide.CN
 * @date 2023/6/19
 * @since 1.0
 */
public class Config {

    // The static instance of the config.
    private static Config config;
    // The list of config filenames.
    private static final String[] filenames = { "config.yml", "trim_upgrade.yml", "whitelist.yml" };
    // The list of files.
    private static File[] files;
    // The list of configs.
    private static YamlConfiguration[] configs;

    public final int version;
    public final WhiteList whiteList;
    public final Feature feature;
    public final Display display;

    public static void init(JavaPlugin plugin, boolean showLog) {
        try {
            if (showLog) {
                Format.use().plugin().info("&aLoading config.yml...");
                Format.use().plugin().info("&7==========&f[&3config.yml&f]&7==========");
            }
            initializeConfig(plugin, showLog);
        } catch (Exception e) {
            e.printStackTrace();
            if (showLog) {
                Format.use().plugin().info("&7==============================");
            }
        }
    }

    private static void initializeConfig(JavaPlugin plugin, boolean showLog) {
        files = new File[filenames.length];
        configs = new YamlConfiguration[filenames.length];
        config = new Config(plugin, showLog);
        featureKeyMap();
    }

    private Config(JavaPlugin plugin, boolean showLog) {
        for (String filename : filenames) {
            if (!new File(plugin.getDataFolder(), filename).exists()) {
                plugin.saveResource(filename, false);
            }
        }

        for (int i = 0; i < filenames.length; i++) {
            File file = new File(plugin.getDataFolder(), filenames[i]);
            if (!file.exists()) {
                plugin.getLogger().info("Failed to load config file: " + filenames[i]);
                continue;
            }
            files[i] = file;
            configs[i] = YamlConfiguration.loadConfiguration(files[i]);
        }

        // configuration
        this.version = configs[0].getInt("version", 1);

        this.feature = new Feature();
        this.feature.protectTerrain = configs[0].getBoolean("feature.protect-terrain", true);
        this.feature.preventedEnderman = configs[0].getBoolean("feature.prevented-enderman", true);
        this.feature.craftingTable = configs[0].getBoolean("feature.crafting-table", true);
        this.feature.ironGolem = configs[0].getBoolean("feature.iron-golem", true);
        this.feature.summonIronGolem = configs[0].getBoolean("feature.summon-iron-golem", true);
        this.feature.stupidVillager = configs[0].getBoolean("feature.stupid-villager", true);
        this.feature.minecartFullSpeed = configs[0].getBoolean("feature.minecart-full-speed", true);
        this.feature.enableWhitelist = configs[0].getBoolean("feature.enable-whitelist", true);

        this.whiteList = new WhiteList();
        this.whiteList.kickMessage = configs[2].getString("kick_message", "&c你没有获得白名单,请联系服主获得。");
        this.whiteList.users = configs[2].getStringList("users");

        this.display = new Display();
        this.display.item.placeRadius = configs[0].getInt("display.item.place-radius", 3);
        this.display.item.recycleRadius = configs[0].getInt("display.item.recycle-radius", 2);
        this.display.item.consume = configs[0].getBoolean("display.item.consume", true);
        this.display.item.scale = new double[3][2];
        this.display.item.scale[0][0] = configs[0].getDouble("display.item.scale.x.min");
        this.display.item.scale[0][1] = configs[0].getDouble("display.item.scale.x.max");
        this.display.item.scale[1][0] = configs[0].getDouble("display.item.scale.y.min");
        this.display.item.scale[1][1] = configs[0].getDouble("display.item.scale.y.max");
        this.display.item.scale[2][0] = configs[0].getDouble("display.item.scale.z.min");
        this.display.item.scale[2][1] = configs[0].getDouble("display.item.scale.z.max");

        this.display.block.placeRadius = configs[0].getInt("display.block.place-radius", 3);
        this.display.block.recycleRadius = configs[0].getInt("display.block.recycle-radius", 2);
        this.display.block.consume = configs[0].getBoolean("display.block.consume", true);
        this.display.block.scale = new double[3][2];
        this.display.block.scale[0][0] = configs[0].getDouble("display.block.scale.x.min");
        this.display.block.scale[0][1] = configs[0].getDouble("display.block.scale.x.max");
        this.display.block.scale[1][0] = configs[0].getDouble("display.block.scale.y.min");
        this.display.block.scale[1][1] = configs[0].getDouble("display.block.scale.y.max");
        this.display.block.scale[2][0] = configs[0].getDouble("display.block.scale.z.min");
        this.display.block.scale[2][1] = configs[0].getDouble("display.block.scale.z.max");

        this.display.book.consume = configs[0].getBoolean("display.book.consume", true);

        if (showLog) {
            String[] dimensions = {"X", "Y", "Z"};
            Format.use().plugin().finder("Version", this.version);
            Format.use().plugin().finder("Protect Terrain", this.feature.protectTerrain);
            Format.use().plugin().finder("Crafting Table", this.feature.craftingTable);
            Format.use().plugin().finder("Iron Golem", this.feature.ironGolem);
            Format.use().plugin().finder("Stupid Villager", this.feature.stupidVillager);
            Format.use().plugin().finder("Minecart Full Speed", this.feature.minecartFullSpeed);
            Format.use().plugin().finder("Enable Whitelist", this.feature.enableWhitelist);
            Format.use().plugin().finder("Item Display Place Radius", this.display.item.placeRadius);
            Format.use().plugin().finder("Item Display Recycle Radius", this.display.item.recycleRadius);
            Format.use().plugin().finder("Item Display Consume", this.display.item.consume);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Format.use().plugin().finder("Item Display Scale " + dimensions[i],
                            "[" + DecimalUtils.decimal(2, this.display.item.scale[i][j]) +
                                    "," + DecimalUtils.decimal(2, this.display.item.scale[i][j]) + "]");
                }
            }
            Format.use().plugin().finder("Block Display Place Radius", this.display.block.placeRadius);
            Format.use().plugin().finder("Block Display Recycle Radius", this.display.block.recycleRadius);
            Format.use().plugin().finder("Block Display Consume", this.display.block.consume);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Format.use().plugin().finder("Block Display Scale " + dimensions[i],
                            "[" + DecimalUtils.decimal(2, this.display.block.scale[i][j]) +
                                    "," + DecimalUtils.decimal(2, this.display.block.scale[i][j]) + "]");
                }
            }
            Format.use().plugin().finder("Book Display Consume", this.display.book.consume);
        }

        // 到这里读完所有config.yml的配置
        if (showLog) {
            Format.use().plugin().info("&7==============================");
        }
        // 注入TrimUpgrade的配置
        loadTrimUpgrade();
    }

    public static final Map<TrimPattern, TrimUpgradeStore> TRIM_UPGRADE_MAP = new HashMap<>();
    // load configs[1] configuration
    private void loadTrimUpgrade() {
        Format.use().plugin().info("&7Loading trim upgrade...");
        ConfigurationSection upgradeSection = configs[1].getConfigurationSection("upgrade");
        if (upgradeSection == null) {
            return;
        }
        for (String key : upgradeSection.getKeys(false)) {
            ConfigurationSection section = upgradeSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            TrimUpgradeStore store = new TrimUpgradeStore();
            // set trimPattern
            store.trimPattern = ConvertUtils.getTrimPatternByName(key);
            // set trimMaterials
            List<String> trimMaterialsList = section.getStringList("trim-material");
            store.trimMaterials = new ArrayList<>(10);
            for (String material : trimMaterialsList) {
                if ("*".equals(material)) { // to all
                    trimMaterialsList.clear();
                    break;
                }
                store.trimMaterials.add(ConvertUtils.getTrimMaterialByName(material));
            }
            // set armorMaterial
            List<String> sectionList = section.getStringList("armor-material");
            for (String sec : sectionList) {
                if ("*".equals(sec)) { // to all
                    trimMaterialsList.clear();
                    break;
                }
            }
            store.armorMaterial = sectionList;
            // set buffs
            List<?> buffsSections = section.getList("buff");
            if (buffsSections != null) {
                List<TrimUpgradeStore.Buff> buffs = new LinkedList<>();
                for (Object buffSectionObject : buffsSections) {
                    if (buffSectionObject instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> buffSection = (Map<String, Object>) buffSectionObject;
                        TrimUpgradeStore.Buff buff = new TrimUpgradeStore.Buff();
                        // set count
                        if (buffSection.containsKey("count")) {
                            buff.count = (int) buffSection.getOrDefault("count", 0);
                        }
                        // set potion effect (this may not exist)
                        String effect = (String) buffSection.get("effect");
                        if (effect != null) {
                            buff.effect = PotionEffectType.getByName(effect);
                        }
                        // set potion effect level
                        if (buffSection.containsKey("effect-level")) {
                            buff.effectLevel = (int) buffSection.getOrDefault("effect-level", 1);
                        } else {
                            buff.effectLevel = 0;
                        }
                        // set potion effect duration
                        if (buffSection.containsKey("effect-duration")) {
                            buff.effectDuration = (int) buffSection.getOrDefault("effect-duration", 0);
                        } else {
                            buff.effectDuration = 0;
                        }
                        // effect to whom
                        String effectTarget = (String) buffSection.getOrDefault("effect-target", "self");
                        buff.effectTarget = EffectTarget.valueOf(effectTarget.toUpperCase());
                        // set event
                        String event = (String) buffSection.get("event");
                        if (event != null) {
                            buff.event = EventType.fromText(event);
                        }
                        // set chance to event
                        if (buffSection.containsKey("event-chance")) {
                            buff.chance = (double) buffSection.getOrDefault("event-chance", 0D);
                        } else {
                            buff.chance = 0D;
                        }
                        // add to buffs list
                        buffs.add(buff);
                    }
                }
                // add sorted buffs list to store
                store.buffList = buffs
                        .stream()
                        .sorted(Comparator.comparingInt(b -> b.count))
                        .collect(Collectors.toList());
            }
            TRIM_UPGRADE_MAP.put(store.trimPattern, store);
        }
        Format.use().plugin().info("&aSuccess &f" +
                TRIM_UPGRADE_MAP.size() +
                " &cFail &f" +
                (upgradeSection.getKeys(false).size() - TRIM_UPGRADE_MAP.size()));
    }

    public static final Map<String, Boolean> FEATURE_KEY_MAP = new HashMap<>();
    private static void featureKeyMap() {
        FEATURE_KEY_MAP.put("feature.protect-terrain", config.feature.protectTerrain);
        FEATURE_KEY_MAP.put("feature.prevented-enderman", config.feature.preventedEnderman);
        FEATURE_KEY_MAP.put("feature.crafting-table", config.feature.craftingTable);
        FEATURE_KEY_MAP.put("feature.iron-golem", config.feature.ironGolem);
        FEATURE_KEY_MAP.put("feature.summon-iron-golem", config.feature.summonIronGolem);
        FEATURE_KEY_MAP.put("feature.stupid-villager", config.feature.stupidVillager);
        FEATURE_KEY_MAP.put("feature.minecart-full-speed", config.feature.minecartFullSpeed);
        FEATURE_KEY_MAP.put("feature.enable-whitelist", config.feature.enableWhitelist);
    }

    public static class Feature {
        public boolean protectTerrain;
        public boolean preventedEnderman;
        public boolean craftingTable;
        public boolean ironGolem;
        public boolean summonIronGolem;
        public boolean stupidVillager;
        public boolean minecartFullSpeed;
        public boolean enableWhitelist;
    }

    public static class Display {
        public final Item item = new Item();
        public final Block block = new Block();
        public final Book book = new Book();

        public static class Item {
            public int placeRadius;
            public int recycleRadius;
            public boolean consume;
            public double[][] scale;
        }

        public static class Block {
            public int placeRadius;
            public int recycleRadius;
            public boolean consume;
            public double[][] scale;
        }

        public static class Book {
            public boolean consume;
        }
    }

    public static Config get() {
        return config;
    }


    public boolean set(int fileIndex, String configKey, Object value) {
        if (fileIndex > filenames.length - 1) {
            return false;
        }
        try {
            configs[fileIndex].set(configKey, value);
            configs[fileIndex].save(files[fileIndex]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
