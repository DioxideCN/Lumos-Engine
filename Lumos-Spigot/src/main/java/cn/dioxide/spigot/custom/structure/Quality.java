package cn.dioxide.spigot.custom.structure;

import java.util.Random;

/**
 * @author Dioxide.CN
 * @date 2023/7/19
 * @since 1.0
 */
public enum Quality {

    STRONG(25), // 强力
    HEINOUS(30); // 凶残

    private final int id;
    private static final Random RANDOM = new Random();

    Quality(int id) {
        this.id = id;
    }

    public int get() {
        return this.id;
    }

    public static Quality random() {
        float chance = RANDOM.nextFloat();
        if (chance <= 0.90) {
            return Quality.STRONG;
        } else {
            return Quality.HEINOUS;
        }
    }

}
