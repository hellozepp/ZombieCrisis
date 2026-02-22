package Game;

import java.awt.*;
import java.util.Random;

/**
 * 血迹类
 * 继承自GameObject，用于显示战斗中的血迹效果
 * 纯视觉效果，不参与碰撞检测
 */
public class Blood extends GameObject{
    private int picX;  // 精灵图X坐标
    private int picY;  // 精灵图Y坐标

    /**
     * 构造方法
     * 随机选择血迹样式（2x2种样式）
     * @param x X坐标
     * @param y Y坐标
     * @param world 所属世界
     */
    public Blood(int x, int y, World world) {
        super("Blood", 0, 0, 99999, x, y, false, world);
        // radius=0: 不参与碰撞
        // speed=0: 不移动
        // HP=99999: 永不消失
        // collidable=false: 不可碰撞
        
        Random rand = new Random();
        int picX = Math.abs(rand.nextInt()) % 2;  // 随机选择列（0或1）
        int picY = Math.abs(rand.nextInt()) % 2;  // 随机选择行（0或1）
        this.picX = picX * 475;  // 转换为像素坐标
        this.picY = picY * 475;
    }

    /**
     * 绘制血迹
     * 从精灵图中截取随机样式的血迹并绘制
     * @param g 图形上下文
     */
    public void draw(Graphics g){
        g.drawImage(imgMap.get(name), 
            x - 30, y,           // 目标位置左上角
            x + 30, y + 45,      // 目标位置右下角
            picX, picY,          // 源图片左上角
            picX + 475, picY + 475,  // 源图片右下角
            null);
    }
    
    /**
     * 碰撞响应（空实现）
     * 血迹不参与碰撞
     */
    public void collisionResponse(GameObject object){
        // 血迹不响应碰撞
    }
    
    /**
     * 受攻击处理（空实现）
     * 血迹不受攻击影响
     */
    public void onAttack(Weapon weapon){
        // 血迹不受攻击
    }
}
