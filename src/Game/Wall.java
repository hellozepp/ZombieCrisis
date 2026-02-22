package Game;

import java.awt.*;

/**
 * 墙壁类
 * 继承自GameObject，作为地图中的障碍物
 * 不可破坏，阻挡移动和寻路
 */
public class Wall extends GameObject{

    /**
     * 构造方法
     * @param x X坐标
     * @param y Y坐标
     * @param world 所属世界
     */
    public Wall(int x, int y, World world) {
        super("Wall", 50, 0, 99999, x, y, true, world);
        // radius=50: 碰撞半径
        // speed=0: 不移动
        // HP=99999: 不可破坏
        // collidable=true: 可碰撞，阻挡移动
    }

    /**
     * 绘制墙壁
     * @param g 图形上下文
     */
    public void draw(Graphics g){
        g.drawImage(imgMap.get(name), x - 50, y - 75, 100, 150, null);
    }
    
    /**
     * 碰撞响应（空实现）
     * 墙壁是静止的，不需要响应碰撞
     */
    public void collisionResponse(GameObject object){
        // 墙壁不响应碰撞
    }
    
    /**
     * 受攻击处理（空实现）
     * 墙壁不可破坏
     */
    public void onAttack(Weapon weapon){
        // 墙壁不受攻击影响
    }
}
