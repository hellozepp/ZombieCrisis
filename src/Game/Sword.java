package Game;

import java.util.Iterator;

/**
 * 剑类
 * 继承自Weapon，玩家的近战武器
 * 扇形范围攻击，无弹药限制
 */
public class Sword extends Weapon{

    /**
     * 构造方法
     * @param role 持有者（玩家）
     * @param world 所属世界
     */
    public Sword(Role role, World world){
        super("Sword", 0, 15, 80, 12, role, 80, 42, false, world);
        // radius=0: 不参与碰撞检测（通过攻击判定造成伤害）
        // speed=15: 未使用
        // damage=80: 伤害值
        // coldDownTime=12: 冷却12帧
        // attackRange=80: 攻击范围80像素
        // attackAngle=42: 攻击角度42度（扇形）
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
     * 剑的位置就是持有者的位置
     * @return 持有者X坐标
     */
    public int getX(){
        return this.host.getX();
    }

    /**
     * 获取武器Y坐标
     * 剑的位置就是持有者的位置
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
