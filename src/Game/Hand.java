package Game;

import java.util.Iterator;

/**
 * 拳头类
 * 继承自Weapon，敌人的近战武器
 * 扇形范围攻击
 */
public class Hand extends Weapon{
    private int attackRange;  // 攻击范围

    /**
     * 构造方法
     * @param role 持有者（敌人）
     * @param world 所属世界
     */
    public Hand(Role role, World world){
        super("Hand", 0, 15, 60, 12, role, 55, 70, false, world);
        // radius=0: 不参与碰撞检测
        // speed=15: 未使用
        // damage=60: 伤害值
        // coldDownTime=12: 冷却12帧
        // attackRange=55: 攻击范围55像素
        // attackAngle=70: 攻击角度70度（比剑更宽的扇形）
        // collidable=false: 不可碰撞
    }

    /**
     * 执行攻击
     * 调用父类的攻击判定
     */
    public void Attack(){
        super.Attack();
    }

    /**
     * 获取武器X坐标
     * 拳头的位置就是持有者的位置
     * @return 持有者X坐标
     */
    public int getX(){
        return this.host.getX();
    }

    /**
     * 获取武器Y坐标
     * 拳头的位置就是持有者的位置
     * @return 持有者Y坐标
     */
    public int getY(){
        return this.host.getY();
    }

    /**
     * 获取攻击范围
     * @return 攻击范围
     */
    public int getAttackRange() {
        return attackRange;
    }

    /**
     * 设置攻击范围
     * @param attackRange 攻击范围
     */
    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }
}
