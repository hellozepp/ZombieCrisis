package Game;

/**
 * 方向枚举类
 * 定义游戏中角色可以移动和面向的8个方向以及停止状态
 */
public enum Direction {
    LD,    // 左下
    RU,    // 右上
    LU,    // 左上
    RD,    // 右下
    L,     // 左
    R,     // 右
    U,     // 上
    D,     // 下
    STOP;  // 停止

    /**
     * 将方向转换为角度值（以度为单位）
     * @param dir 方向枚举值
     * @return 对应的角度值，以右方向为0度，逆时针递增
     */
    public static double toDegree(Direction dir){
        switch (dir){
            case R: return 0;      // 右：0度
            case RU: return 45;    // 右上：45度
            case U: return 90;     // 上：90度
            case LU: return 135;   // 左上：135度
            case L: return 180;    // 左：180度
            case LD: return 225;   // 左下：225度
            case D: return 270;    // 下：270度
            case RD:  return 315;  // 右下：315度
            default: return 360;   // 停止：360度（特殊标记）
        }
    }

    /**
     * 获取方向的X轴分量（单位向量）
     * @param dir 方向枚举值
     * @return X轴方向的分量值
     */
    public static double getVectorX(Direction dir){
        switch (dir){
            case R: return 1;                                    // 右：X=1
            case RU: return Math.cos(Math.toRadians(45));       // 右上：X=cos(45°)
            case U: return 0;                                    // 上：X=0
            case LU: return -Math.cos(Math.toRadians(45));      // 左上：X=-cos(45°)
            case L: return -1;                                   // 左：X=-1
            case LD: return -Math.cos(Math.toRadians(45));      // 左下：X=-cos(45°)
            case D: return 0;                                    // 下：X=0
            case RD:  return Math.cos(Math.toRadians(45));      // 右下：X=cos(45°)
            default: return -2;                                  // 停止：无效值
        }
    }

    /**
     * 获取方向的Y轴分量（单位向量）
     * @param dir 方向枚举值
     * @return Y轴方向的分量值
     */
    public static double getVectorY(Direction dir){
        switch (dir){
            case R: return 0;                                    // 右：Y=0
            case RU: return Math.cos(Math.toRadians(45));       // 右上：Y=cos(45°)
            case U: return 1;                                    // 上：Y=1
            case LU: return Math.cos(Math.toRadians(45));       // 左上：Y=cos(45°)
            case L: return 0;                                    // 左：Y=0
            case LD: return -Math.cos(Math.toRadians(45));      // 左下：Y=-cos(45°)
            case D: return -1;                                   // 下：Y=-1
            case RD:  return -Math.cos(Math.toRadians(45));     // 右下：Y=-cos(45°)
            default: return -2;                                  // 停止：无效值
        }
    }
}
