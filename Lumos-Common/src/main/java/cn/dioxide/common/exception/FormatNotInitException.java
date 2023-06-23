package cn.dioxide.common.exception;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class FormatNotInitException extends RuntimeException {
    public FormatNotInitException() {
        super("AbstractFormat haven't initialized");
    }
}
