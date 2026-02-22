package Game;

/**
 * 幽灵弹类
 * 继承自Ball，幽灵敌人的远程武器
 * 无弹药限制
 */
public class Ghostball extends Ball {
    /**
     * 构造方法
     * @param role 持有者（幽灵）
     * @param world 所属世界
     */
    public Ghostball(Role role, World world) {
        super("Ghostball", 7, 8, 80, 30, role, world);
        // name="Ghostball": 弹丸名称
        // radius=7: 碰撞半径（比火球小）
        // speed=8: 飞行速度（比火球慢）
        // damage=80: 伤害值（比火球高）
        // coldDownTime=30: 冷却30帧（比火球长）
        this.setNum(10000);  // 无限弹药
        this.picOffset = 8;  // 精灵图偏移量
    }
}
