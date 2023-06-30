package cn.dioxide.common.util;

import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 用于计算仿射变换后的4阶矩阵工具
 *
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class MatrixUtils {

    private MatrixUtils() {}

    public static Matrix4f getMatrix(double rx, double ry, double rz) {
        Matrix4f affineMatrix = new Matrix4f();
        // 3D Rotate
        affineMatrix.rotateX(convertDoubleToFloat(Math.toRadians(rx)));
        affineMatrix.rotateY(convertDoubleToFloat(Math.toRadians(ry)));
        affineMatrix.rotateZ(convertDoubleToFloat(Math.toRadians(rz)));
        return affineMatrix;
    }

    public static Matrix4f getMatrix(double rx, double ry, double rz, double sx, double sy, double sz) {
        Matrix4f affineMatrix = getMatrix(rx, ry, rz); // Note the change here to rx, ry, rz instead of sx, sy, sz
        // 3D Scale
        affineMatrix.scale(convertDoubleToFloat(sx), convertDoubleToFloat(sy), convertDoubleToFloat(sz));
        return affineMatrix;
    }

    public static Matrix4f getMatrix(double rx, double ry, double rz, double sx, double sy, double sz, double offset_x, double offset_y, double offset_z) {
        Matrix4f affineMatrix = getMatrix(rx, ry, rz, sx, sy, sz);
        // 3D Translate
        affineMatrix.translate(convertDoubleToFloat(offset_x), convertDoubleToFloat(offset_y), convertDoubleToFloat(offset_z));
        return affineMatrix;
    }

    private static float convertDoubleToFloat(double value) {
        BigDecimal bigDecimalValue = new BigDecimal(value, MathContext.DECIMAL32); // Using a MathContext for 7 digits precision (similar to float)
        return bigDecimalValue.floatValue();
    }

}
