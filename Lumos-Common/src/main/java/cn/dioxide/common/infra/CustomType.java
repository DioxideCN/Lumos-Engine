package cn.dioxide.common.infra;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public enum CustomType {

    ITEM_TYPE("item_type"),
    SKILL_TYPE("skill_type");

    private final String text;

    CustomType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return this.text;
    }

}
