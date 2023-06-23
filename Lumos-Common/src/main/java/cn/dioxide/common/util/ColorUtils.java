package cn.dioxide.common.util;

/**
 * 统一将&颜色符号转换为§符号
 *
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class ColorUtils {

    private ColorUtils() {}

    public static String replace(String msg) {
        return msg.replaceAll("&", "§");
    }

}
