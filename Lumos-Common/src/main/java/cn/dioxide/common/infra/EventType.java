package cn.dioxide.common.infra;

/**
 * @author Dioxide.CN
 * @date 2023/6/22
 * @since 1.0
 */
public enum EventType {

    RESPAWN_HERE("respawn_here"), // 原地复活
    NEUTRAL_PIGLIN("neutral_piglin"), // 让猪灵中立
    DOCILE_ENDERMAN("docile_enderman"), // 可以直视末影人
    EXPLOIT_VILLAGERS("exploit_villagers"), // 从村民身上剥削0-3个铁粒或0-3个金粒
    IMMUNE_DEBUFF("immune_debuff"); // 免疫负面药水效果

    private final String text;

    EventType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static EventType fromText(String text) {
        for (EventType eventType : EventType.values()) {
            if (eventType.getText().equals(text)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("No enum constant for text: " + text);
    }

    @Override
    public String toString() {
        return this.text;
    }
}

