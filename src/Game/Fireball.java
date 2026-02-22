package Game;

/**
 * 火球类
 * 继承自Ball，玩家的远程武器
 * 有弹药限制，支持大招（8方向发射）
 */
public class Fireball extends Ball {
    int maxNum = 80;  // 最大弹药数量
    
    /**
     * 构造方法
     * @param role 持有者（玩家）
     * @param world 所属世界
     */
    public Fireball(Role role, World world) {
        super("Fireball", 15, 15, 55, 10, role, world);
        // name="Fireball": 弹丸名称
        // radius=15: 碰撞半径
        // speed=15: 飞行速度
        // damage=55: 伤害值
        // coldDownTime=10: 冷却10帧
        setNum(maxNum);      // 初始弹药满
        this.picOffset = 16; // 精灵图偏移量
    }

    /**
     * 获取最大弹药数量
     * @return 最大弹药数
     */
    public int getMaxNum() {
        return maxNum;
    }
}
