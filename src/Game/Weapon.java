package Game;

import java.awt.*;
import java.util.Iterator;

/**
 * 武器抽象基类
 * 继承自GameObject，是所有武器的父类
 * 包含攻击判定、冷却系统等通用功能
 */
public abstract class Weapon extends GameObject {
    protected int state;           // 武器状态（-1未使用，>=0攻击动画帧）
    protected Role host;           // 持有者
    protected int coldDownTime;    // 冷却时间（帧数）
    protected int coldDown;        // 当前冷却计数器
    protected int damage;          // 伤害值
    protected int attackRange;     // 攻击范围
    protected int attackAngle;     // 攻击角度（度）

    /**
     * 构造方法
     * @param name 武器名称
     * @param radius 半径
     * @param speed 速度
     * @param damage 伤害
     * @param coldDownTime 冷却时间
     * @param host 持有者
     * @param attackRange 攻击范围
     * @param attackAngle 攻击角度
     * @param collidable 是否可碰撞
     * @param world 所属世界
     */
    public Weapon(String name, int radius, int speed, int damage, int coldDownTime, Role host, int attackRange,int attackAngle, boolean collidable, World world) {
        super(name, radius, speed, host.getDir(), 9999, host.x, host.y, collidable,  world);
        this.damage = damage;
        this.coldDownTime = coldDownTime;
        this.coldDown = 0;
        this.host = host;
        this.attackRange = attackRange;
        this.attackAngle = attackAngle;
        state = -1;  // 初始状态：未使用
    }

    /**
     * 设置伤害值
     * @param damage 伤害值
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * 绘制普通攻击动画
     * 显示角色的攻击动画帧
     * @param g 图形上下文
     * @return 上一帧的状态
     */
    public int drawNomalAttack(Graphics g){
        if(state < 0) return state;  // 未在攻击状态
        
        int lastState = state;
        int picX = getState() / 3 + 5;  // 攻击动画在精灵图的第5-7列
        int picY = (host.getDir() == Direction.STOP ? host.getOldDir() : host.getDir()).ordinal();
        drawOneImage(g, host.name, Role.PICOFFSET, host.x, host.y, picX, picY);
        maintainState(9);  // 攻击动画共9帧
        return lastState;
    }

    /**
     * 执行攻击（武器攻击判定的核心方法）
     * 
     * 工作流程：
     * 1. 遍历世界中的所有对象
     * 2. 对每个对象进行距离判定（是否在攻击范围内）
     * 3. 对每个对象进行角度判定（是否在攻击扇形内）
     * 4. 满足条件的对象受到伤害
     * 
     * 攻击扇形说明：
     * - 以持有者为中心，朝向为方向
     * - attackRange：扇形半径（攻击距离）
     * - attackAngle：扇形角度（攻击范围）
     * 
     * 示例（剑）：
     * - attackRange = 80：攻击距离80像素
     * - attackAngle = 42：左右各21度，总共42度扇形
     * 
     * 角度计算说明：
     * - myangle：武器朝向角度（0-360度）
     * - angle：目标相对角度（0-360度）
     * - 如果角度差<attackAngle，说明目标在攻击扇形内
     * - 需要考虑360度循环（例如350度和10度的差值是20度）
     */
    public void Attack(){
        Iterator<GameObject> iter = world.getObjectsIterator();
        while(iter.hasNext()){
            GameObject object = iter.next();
            
            // 跳过自己
            if(this.host.equals(object)) continue;
            
            // 敌人不攻击敌人
            if(this.host instanceof Enemy && object instanceof Enemy) continue;
            
            int objX = object.getX();
            int objY = object.getY();
            int deltaX = objX - this.getX();
            int deltaY = - objY + this.getY();  // Y轴向下为正，需要取反
            double D = getDistance(objX, objY, this.getX(),this.getY());
            
            // 距离判定：目标在攻击范围内
            if(D < getAttackRange()){
                // 计算目标相对于武器的角度
                double sinA = deltaY / D;
                double cosA = deltaX / D;
                double angle1 = Math.toDegrees(Math.asin(sinA));
                double angle2 = Math.toDegrees(Math.acos(cosA));
                double angle = angle2;
                if(angle1 < 0) angle = 360 - angle;  // 调整到0-360度范围
                
                // 获取武器朝向角度
                double myangle = Direction.toDegree(this.host.getDir());
                if(myangle == 360) myangle = Direction.toDegree(this.host.getOldDir());
                
                // 角度判定：目标在攻击扇形范围内
                // 需要考虑360度循环（例如350度和10度的差值是20度，不是340度）
                if(Math.abs(myangle - angle) < attackAngle || 360 - Math.abs(myangle - angle) < attackAngle)
                    object.onAttack(this);  // 对目标造成伤害
            }
        }
    }

    /**
     * 获取攻击范围
     * @return 攻击范围
     */
    public int getAttackRange() {
        return attackRange;
    }

    /**
     * 维护武器状态
     * 更新攻击动画帧
     * @param n 动画总帧数
     * @return 当前状态
     */
    public int maintainState(int n){
        this.state++;
        if(state >= n) {
            state = -1;  // 动画结束，重置状态
        }
        return state;
    }

    /**
     * 维护冷却时间
     * 每帧递减冷却计数器
     * @return 剩余冷却时间
     */
    public int maintainColdDown(){
        if(coldDown > 0)
            coldDown--;
        return coldDown;
    }

    /**
     * 设置冷却时间
     * @param coldDownTime 冷却时间（帧数）
     */
    public void setColdDownTime(int coldDownTime) {
        this.coldDownTime = coldDownTime;
    }

    /**
     * 设置冷却
     * 使用武器后调用，开始冷却
     */
    public void setColdDown(){
        coldDown = coldDownTime;
    }

    /**
     * 获取当前冷却时间
     * @return 剩余冷却帧数
     */
    public int getColdDown() {
        return coldDown;
    }

    /**
     * 受攻击处理（空实现）
     * 武器不受攻击影响
     */
    public void onAttack(Weapon weapon){ }

    /**
     * 绘制方法（空实现）
     * 武器本身不绘制，由持有者绘制攻击动画
     */
    public void draw(Graphics g){
    }

    /**
     * 碰撞响应（空实现）
     * 武器不响应碰撞
     */
    public void collisionResponse(GameObject object){
    }

    /**
     * 获取伤害值
     * @return 伤害值
     */
    public int getDamage(){
        return this.damage;
    }

    /**
     * 获取武器状态
     * @return 当前状态
     */
    public int getState(){
        return this.state;
    }

    /**
     * 设置武器状态
     * 开始攻击动画
     */
    public void setState(){
        this.state = 0;
    }

    /**
     * 重置武器状态
     */
    public void resetState(){
        this.state = -1;
    }

    /**
     * 判断是否为指定角色的武器
     * @param role 角色对象
     * @return 是否为该角色的武器
     */
    public boolean isHost(Role role){
        if(this.host.equals(role)){
            return true;
        }
        return false;
    }

    /**
     * 转换为字符串
     * @return 武器名称
     */
    @Override
    public String toString() {
        return this.name;
    }
}
