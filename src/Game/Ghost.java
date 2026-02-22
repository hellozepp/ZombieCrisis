package Game;

import java.awt.*;

/**
 * 幽灵类（远程敌人）
 * 继承自Enemy，使用幽灵弹进行远程攻击
 * 保持距离并发射弹丸
 */
public class Ghost extends Enemy {
    private int attackRange = 300;  // 攻击范围

    /**
     * 构造方法
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param world 所属世界
     */
    public Ghost(int x, int y, World world){
        super("Ghost", 130, 14, 2, x, y, world);
        // name="Ghost": 幽灵名称
        // HP=130: 生命值（比怪物低）
        // radius=14: 碰撞半径
        // speed=2: 移动速度
        
        // 装备幽灵弹武器
        addWeapon(new Ghostball(this, world));
        setCurrentWeapon(getWeapons().get(0));
    }

    /**
     * 绘制幽灵
     * 
     * 攻击逻辑：
     * 1. 检查与目标的距离是否在攻击范围内（300像素）
     * 2. 检查当前朝向是否对准目标（角度差<60度）
     * 3. 满足条件且武器冷却结束时发射弹丸
     * 
     * 为什么要检查角度差：
     * - 远程攻击需要瞄准，不能随便发射
     * - 60度的容错范围让幽灵不需要完全对准也能攻击
     * - 如果角度差太大，幽灵会继续移动调整位置
     * 
     * @param g 图形上下文
     */
    public void draw(Graphics g) {
        if(getTarget() != null && getTarget().getHP() > 0 && this.getHP() > 0) {
            // 计算与目标的距离
            int distance = (int) getDistance(this.getX(), this.getY(), getCurrentTarget().getX(), getCurrentTarget().getY());
            
            // 在攻击范围内且武器冷却结束
            if (distance <= attackRange && this.getCurrentWeapon().getColdDown() == 0) {
                // 计算当前朝向角度
                double currentDegree = Direction.toDegree(this.dir == Direction.STOP ? oldDir : dir);
                // 计算目标方向角度
                double targetDegree = getDeltaDegree(getTarget().getX(), getTarget().getY());
                
                // 检查角度差是否小于60度（瞄准判定）
                // 需要考虑360度循环（例如350度和10度的差值是20度，不是340度）
                if(Math.abs(targetDegree - currentDegree) < 60 || Math.abs(360 - targetDegree + currentDegree) < 60) {
                    // 发射弹丸
                    this.getCurrentWeapon().setState();
                    this.getCurrentWeapon().setColdDown();
                    
                    // 重新计算路径（发射后可能需要调整位置）
                    getPath();
                }
            }
        }
        
        locateDirection();  // 更新移动方向
        super.draw(g);      // 绘制角色
    }
}
