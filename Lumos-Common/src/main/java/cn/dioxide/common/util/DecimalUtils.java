package cn.dioxide.common.util;

/**
 * 小数处理工具
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class DecimalUtils {

    /**
     * 四舍五入保留place位double小数
     * @param place 保留小数位数
     * @param num 双精度数
     * @return 返回保留小数位后的字符串
     */
    public static String decimal(int place, double num) {
        if (place <= 0) {
            return String.format("%.2f", num);
        }
        return String.format("%." + place + "f", num);
    }

    /**
     * 四舍五入保留place位float小数
     * @param place 保留小数位数
     * @param num 双精度数
     * @return 返回保留小数位后的字符串
     */
    public static String decimal(int place, float num) {
        if (place <= 0) {
            return String.format("%.2f", num);
        }
        return String.format("%." + place + "f", num);
    }

}
