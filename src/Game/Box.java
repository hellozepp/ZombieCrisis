package Game;

import java.awt.*;
import java.util.Random;

/**
 * 宝箱类
 * 继承自GameObject，提供弹药和生命值补给
 * 拾取后会重生
 */
public class Box extends GameObject{
    public static final int DELAYTIME = 800;  // 重生延迟时间（帧数）
    private int delay = 0;                     // 当前延迟计数器

    /**
     * 构造方法
     * @param x X坐标
     * @param y Y坐标
     * @param world 所属世界
     */
    public Box(int x, int y, World world) {
        super("Box", 14, 0, 99999, x, y, false, world);
        // radius=14: 碰撞半径
        // speed=0: 不移动
        // HP=99999: 不可破坏
        // collidable=false: 不阻挡移动，但可触发拾取
    }

    /**
     * 绘制宝箱
     * 每帧检测碰撞（拾取判定）
     * @param g 图形上下文
     */
    public void draw(Graphics g){
        world.collisionDetection(this);  // 检测是否有玩家接触
        g.drawImage(imgMap.get(name), x - 20, y - 10, 60, 60, null);
    }
    
    /**
     * 碰撞响应
     * 玩家接触宝箱时触发拾取
     * @param object 碰撞的对象
     */
    public void collisionResponse(GameObject object){
        if(object instanceof Hero) {  // 只有玩家可以拾取
            Random rand = new Random();
            int n = Math.abs(rand.nextInt()) % 100;  // 随机掉落
            
            // 掉落概率：45%弹药，45%生命，10%无
            if(n < 45)
            	fireballBox((Hero) object);  // 补充弹药
            else if(n < 90)
                bloodBox((Hero) object);     // 补充生命
            
            world.pickUpBox(this);  // 从世界移除并加入重生队列
            setDelay(DELAYTIME);    // 设置重生延迟
        }
    }

    /**
     * 设置重生延迟
     * @param delay 延迟帧数
     */
    public void setDelay(int delay){
        this.delay = delay;
    }

    /**
     * 获取重生延迟
     * @return 剩余延迟帧数
     */
    public int getDelay() {
        return delay;
    }

    /**
     * 受攻击处理（空实现）
     * 宝箱不受攻击影响
     */
    public void onAttack(Weapon weapon){
        // 宝箱不可破坏
    }

    /**
     * 弹药补给
     * 补满玩家的火球弹药
     * @param hero 玩家对象
     */
    public void fireballBox(Hero hero) {
        for (Weapon weapon : hero.getWeapons())
            if (weapon instanceof Fireball) {
                ((Fireball) weapon).setNum(((Fireball) weapon).getMaxNum());
                break;
            }
    }

    /**
     * 生命补给
     * 补满玩家的生命值
     * @param hero 玩家对象
     */
    public void bloodBox(Hero hero) {
        hero.setHP(Hero.MAX_HP);
    }

    /**
     * 碰撞检测（重写）
     * 只检测与玩家的碰撞
     * @param object 要检测的对象
     * @return 是否发生碰撞
     */
    public boolean collisionDetection(GameObject object){
        if(!(object instanceof Hero)) return false;  // 只检测玩家
        
        // 圆形碰撞检测
        double deltaX = this.x - object.getX();
        double deltaY = (this.y - object.getY());
        double d = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        int R = this.getRadius() + object.getRadius();
        
        if(d <= R) return true;
        return false;
    }
}
