package cn.dioxide.common.util;

import org.joml.Matrix4f;

/**
 * 用于计算仿射变换后的4阶矩阵工具
 *
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class MatrixUtils {

    private MatrixUtils() {}

    // 计算旋转的仿射矩阵
    public static Matrix4f getMatrix(double rx, double ry, double rz) {
        Matrix4f affineMatrix = new Matrix4f();
        // 3D Rotate
        affineMatrix.rotateX((float) Math.toRadians(rx));
        affineMatrix.rotateY((float) Math.toRadians(ry));
        affineMatrix.rotateZ((float) Math.toRadians(rz));

        return affineMatrix;
    }

    // 计算旋转和缩放的仿射矩阵
    public static Matrix4f getMatrix(double rx, double ry, double rz, double sx, double sy, double sz) {
        Matrix4f affineMatrix = getMatrix(sx, sy, sz);
        // 3D Scale
        affineMatrix.scale((float) sx, (float) sy, (float) sz);
        return affineMatrix;
    }

    // 计算旋转、缩放和枢轴点便宜的仿射矩阵
    public static Matrix4f getMatrix(double rx, double ry, double rz, double sx, double sy, double sz, double offset_x, double offset_y, double offset_z) {
        Matrix4f affineMatrix = getMatrix(sx, sy, sz, rx, ry, rz);
        // 3D Translate
        affineMatrix.translate((float) offset_x, (float) offset_y, (float) offset_z);
        return affineMatrix;
    }

}
