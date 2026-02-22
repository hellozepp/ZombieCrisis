package Game;

import java.awt.*;
import java.lang.annotation.Target;

/**
 * 怪物类（近战敌人）
 * 继承自Enemy，使用拳头进行近战攻击
 * 接近玩家后发动攻击
 */
public class Monster extends Enemy {

    /**
     * 构造方法
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param world 所属世界
     */
    public Monster(int x, int y, World world){
        super("Monster", 170, 14, 2, x, y, world);
        // name="Monster": 怪物名称
        // HP=170: 生命值
        // radius=14: 碰撞半径
        // speed=2: 移动速度
        
        // 装备拳头武器
        addWeapon(new Hand(this, world));
        setCurrentWeapon(getWeapons().get(0));
        
        // 设置武器属性
        getCurrentWeapon().setDamage(100);        // 伤害100
        getCurrentWeapon().setColdDownTime(24);   // 冷却24帧
        ((Hand)getCurrentWeapon()).setAttackRange(50);  // 攻击范围50
    }

    /**
     * 绘制怪物
     * 在攻击范围内时发动攻击
     * @param g 图形上下文
     */
    public void draw(Graphics g) {
        if(getTarget() != null && getTarget().getHP() > 0) {
            // 计算与目标的距离
            int distance = (int) getDistance(this.getX(), this.getY(), getCurrentTarget().getX(), getCurrentTarget().getY());
            
            // 在攻击范围内且武器冷却结束
            if (distance <= ((Hand) getCurrentWeapon()).getAttackRange() && this.getCurrentWeapon().getColdDown() == 0) {
                // 朝向目标
                Direction dir = judgeAccurateDir(getTarget().getX(), getTarget().getY());
                this.dir = dir;
                this.oldDir = (dir == Direction.STOP ? oldDir : dir);
                
                // 发动攻击
                this.getCurrentWeapon().setState();
                this.getCurrentWeapon().setColdDown();
                
                // 重新计算路径（攻击后可能需要调整位置）
                getPath();
            }
        }
        
        locateDirection();  // 更新移动方向
        super.draw(g);      // 绘制角色
    }
}
