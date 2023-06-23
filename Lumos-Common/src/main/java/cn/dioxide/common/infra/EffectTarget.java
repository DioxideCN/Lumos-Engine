package cn.dioxide.common.infra;

/**
 * @author Dioxide.CN
 * @date 2023/6/22
 * @since 1.0
 */
public enum EffectTarget {

    SELF("self"),
    ATTACKER("attacker");

    private final String text;

    EffectTarget(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static EffectTarget fromText(String text) {
        for (EffectTarget target : EffectTarget.values()) {
            if (target.getText().equals(text)) {
                return target;
            }
        }
        throw new IllegalArgumentException("No enum constant for text: " + text);
    }

    @Override
    public String toString() {
        return this.text;
    }

}
