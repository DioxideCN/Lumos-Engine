package cn.dioxide.common.extension;

import java.util.Objects;

/**
 * @author Dioxide.CN
 * @date 2023/6/3
 * @since 1.0
 */
public class Pair<L, R> {

    private L leftValue;
    private R rightValue;

    private Pair() {}
    private Pair(L leftValue, R rightValue) {
        this.rightValue = rightValue;
        this.leftValue = leftValue;
    }

    public L left() {
        return leftValue;
    }

    public R right() {
        return rightValue;
    }

    /**
     * 创建空的Pair
     *
     * @return 返回两边都为空的Pair对象
     */
    public static <L, R> Pair<L, R> empty() {
        return new Pair<>(null, null);
    }

    /**
     * 创建Pair
     *
     * @param left 左侧值
     * @param right 右侧值
     * @return 返回Pair对象
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    /**
     * 镜像创建Pair
     *
     * @param right 左侧右边值
     * @param left 右侧左边值
     * @return 返回左右镜像的Pair对象
     */
    public static <L, R> Pair<L, R> flip(R right, L left) {
        return new Pair<>(left, right);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(leftValue);
        result = 31 * result + Objects.hashCode(rightValue);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(leftValue, pair.leftValue) && Objects.equals(rightValue, pair.rightValue);
    }

    @Override
    public String toString() {
        return "<" + (leftValue != null ? leftValue.toString() : "null") + ","
                + (rightValue != null ? rightValue.toString() : "null") + ">";
    }
}
