package Game;

import java.awt.*;

/**
 * 边界类
 * 继承自GameObject，用于限制游戏活动范围
 * 使用巨大的圆形碰撞体作为地图边界
 */
public class Border extends GameObject {
    private int position;  // 边界位置（0上1下2左3右）

    /**
     * 构造方法
     * 根据位置创建对应的边界
     * @param position 边界位置（0上1下2左3右）
     * @param world 所属世界
     */
    public Border(int position, World world){
        super("Border", 100000000, 0, 100000000, 0, 0, true, world);
        // radius=100000000: 巨大的半径，确保能阻挡所有对象
        // speed=0: 不移动
        // HP=100000000: 不可破坏
        // collidable=true: 可碰撞
        
        // 根据位置设置边界中心坐标
        // 边界圆心在地图外很远的地方，只有边缘与地图接触
        switch(position){
            case 0:  // 上边界
                this.x = 0;
                this.y = -100000000 + Role.PICOFFSET + 10;
                break;
            case 1:  // 下边界
                this.x = 0;
                this.y = 100000000 + world.getHeight() - Role.PICOFFSET;
                break;
            case 2:  // 左边界
                this.x = -100000000 + 10;
                this.y = 0;
                break;
            case 3:  // 右边界
                this.x = 100000000 + world.getWidth() - 10;
                this.y = 0;
                break;
        }
    }

    /**
     * 绘制边界（空实现）
     * 边界不可见，不需要绘制
     * @param g 图形上下文
     */
    public void draw(Graphics g){
        // 边界不可见，可选择性绘制调试用的圆弧
        // g.drawArc(this.x, this.y, 100000000, 100000000, 0, 360);
    }

    /**
     * 碰撞响应（空实现）
     * 边界不需要响应碰撞，碰撞处理由对方完成
     */
    public void collisionResponse(GameObject object){
        // 边界是静止的，不需要响应
    }

    /**
     * 受攻击处理（空实现）
     * 边界不可破坏
     */
    public void onAttack(Weapon weapon){ 
        // 边界不受攻击影响
    }
}
