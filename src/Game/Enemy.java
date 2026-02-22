package Game;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

/**
 * 敌人抽象基类
 * 继承自Role，是所有敌人的父类
 * 使用A*寻路算法追踪玩家
 */
public class Enemy extends Role {
    private Pathfinder pathfinder;  // 寻路器
    private Role target;            // 目标玩家
    private List<Grid> path;        // 路径列表
    private int refreshPath;        // 路径刷新计数器
    private int collisionDelay;     // 碰撞延迟计数器

    /**
     * 构造方法
     * @param name 敌人名称
     * @param HP 生命值
     * @param radius 半径
     * @param speed 速度
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param world 所属世界
     */
    public Enemy(String name, int HP, int radius, int speed,int x, int y, World world){
        super(name, HP, radius, speed, x, y, world);
        this.pathfinder = new Pathfinder(new WorldGrids(world), this);
        this.target = getTarget();  // 寻找初始目标
        this.refreshPath = 0;
        this.collisionDelay = 0;
    }

    /**
     * 获取目标玩家
     * 选择距离最近的存活玩家作为目标
     * @return 目标玩家
     */
    public Role getTarget() {
        Iterator<GameObject> tmp = world.getObjectsIterator();
        Role target = null;
        double minDistance = 10000000;  // 初始化为极大值
        
        // 遍历所有对象，寻找最近的存活玩家
        while(tmp.hasNext()){
            GameObject obj = tmp.next();
            if(obj instanceof Hero && obj.getHP() > 0){
                double distance = Math.pow(this.x - obj.getX(), 2) + Math.pow(this.y - obj.getY(), 2);
                target = minDistance <  distance ? target : (Role) obj;
                minDistance = minDistance < distance ? minDistance : distance;
            }
        }
        return target;
    }

    /**
     * 获取当前目标
     * @return 当前追踪的目标
     */
    public Role getCurrentTarget(){
        return this.target;
    }

    /**
     * 维护路径刷新计数器
     * @param n 刷新周期
     * @return 当前计数值
     */
    public int mainTainRefreshPath(int n){
        return refreshPath = (refreshPath + 1) % n;
    }

    /**
     * 维护碰撞延迟计数器
     * @param n 延迟周期
     * @return 当前计数值
     */
    public int maintainCollisionDelay(int n){
        return collisionDelay = (collisionDelay + 1) % n;
    }

    /**
     * 获取当前所在网格
     * @return 当前网格
     */
    public Grid getCurrentGrid(){
        return pathfinder.getWorldGrids().getGrid(this.x, this.y);
    }

    /**
     * 获取目标所在网格
     * @return 目标网格
     */
    public Grid getTargetGrid(){
        return pathfinder.getWorldGrids().getGrid(target.getX(), target.getY());
    }

    /**
     * 计算到目标的路径
     * 使用A*算法
     */
    public void getPath(){
        path = pathfinder.shortestPath(getTargetGrid());
    }

    /**
     * 根据坐标差判断移动方向
     * 用于寻路时，根据下一个网格的位置确定移动方向
     * 
     * 工作原理：
     * - 比较下一个网格与当前位置的X、Y坐标差
     * - 根据坐标差的正负判断8个方向
     * 
     * 示例：
     * - deltaX < 0, deltaY = 0 → 向左移动（L）
     * - deltaX > 0, deltaY < 0 → 向右上移动（RU）
     * 
     * @param deltaX X坐标差（目标X - 当前X）
     * @param deltaY Y坐标差（目标Y - 当前Y）
     * @return 对应的移动方向
     */
    public Direction judgeDirection(int deltaX, int deltaY){
        if (deltaX == 0 && deltaY < 0) return Direction.U;        // 正上方
        else if (deltaX < 0 && deltaY < 0) return Direction.LU;   // 左上方
        else if (deltaX < 0 && deltaY == 0) return Direction.L;   // 正左方
        else if (deltaX < 0 && deltaY > 0) return Direction.LD;   // 左下方
        else if (deltaX == 0 && deltaY > 0) return Direction.D;   // 正下方
        else if (deltaX > 0 && deltaY > 0) return Direction.RD;   // 右下方
        else if (deltaX > 0 && deltaY == 0) return Direction.R;   // 正右方
        else if (deltaX > 0 && deltaY < 0) return Direction.RU;   // 右上方
        else return Direction.STOP;                                // 已到达目标
    }

    /**
     * 计算从当前位置指向目标位置的角度
     * 用于判断敌人应该朝哪个方向（用于远程攻击的瞄准）
     * 
     * 角度定义：以右方向为0度，逆时针递增
     * - 右：0度
     * - 右上：45度
     * - 上：90度
     * - 左上：135度
     * - 左：180度
     * - 左下：225度
     * - 下：270度
     * - 右下：315度
     * 
     * @param anotherX 目标X坐标
     * @param anotherY 目标Y坐标
     * @return 角度值（0-360度）
     */
    public double getDeltaDegree(int anotherX, int anotherY){
        // 计算目标相对于当前位置的坐标差
        int deltaX = anotherX - this.getX();
        int deltaY = - anotherY + this.getY();  // Y轴向下为正，需要取反
        
        // 计算距离
        double D = getDistance(anotherX, anotherY, this.getX(),this.getY());
        
        // 计算三角函数值
        double sinA = deltaY / D;  // sin值 = 对边/斜边
        double cosA = deltaX / D;  // cos值 = 邻边/斜边
        
        // 通过反三角函数计算角度
        double angle1 = Math.toDegrees(Math.asin(sinA));  // 通过sin计算角度（-90到90度）
        double angle2 = Math.toDegrees(Math.acos(cosA));  // 通过cos计算角度（0到180度）
        
        // 使用cos的结果作为基准角度
        double angle = angle2;
        
        // 如果sin值为负（目标在下方），需要调整角度到180-360度范围
        if(angle1 < 0) angle = 360 - angle;
        
        return angle;
    }

    /**
     * 精确判断朝向方向（8方向）
     * 根据角度值判断应该面向哪个方向
     * 
     * 用途：
     * 1. 敌人停止移动时，仍然保持朝向目标（用于攻击动画）
     * 2. 远程敌人判断是否瞄准目标（Ghost类使用）
     * 
     * 角度范围划分：
     * - 337.5-22.5度：右（R）
     * - 22.5-67.5度：右上（RU）
     * - 67.5-112.5度：上（U）
     * - 112.5-157.5度：左上（LU）
     * - 157.5-202.5度：左（L）
     * - 202.5-247.5度：左下（LD）
     * - 247.5-292.5度：下（D）
     * - 292.5-337.5度：右下（RD）
     * 
     * @param anotherX 目标X坐标
     * @param anotherY 目标Y坐标
     * @return 对应的方向枚举
     */
    public Direction judgeAccurateDir(int anotherX, int anotherY){
        double angle = getDeltaDegree(anotherX, anotherY);
        
        // 根据角度范围判断方向（每个方向占45度）
        if((angle >= 0 && angle <= 22.5) || (angle >= 337.5 && angle <= 360)) return Direction.R;
        else if(angle >= 22.5 && angle < 67.5) return Direction.RU;
        else if(angle >= 67.5 && angle < 112.5) return Direction.U;
        else if(angle >= 112.5 && angle < 157.5) return Direction.LU;
        else if(angle >= 157.5 && angle < 202.5) return Direction.L;
        else if(angle >= 202.5 && angle < 247.5) return Direction.LD;
        else if(angle >= 247.5 && angle < 292.5) return Direction.D;
        else if(angle >= 292.5 && angle < 337.5) return Direction.RD;
        else  return Direction.STOP;
    }

    /**
     * 获取下一步移动方向
     * @param nextGrid 下一个目标网格
     * @return 移动方向
     */
    public Direction getNextDir(Grid nextGrid){
        if(nextGrid != null) {
            int deltaX = nextGrid.getX() - this.getX();
            int deltaY = nextGrid.getY() - this.getY();
            return judgeDirection(deltaX, deltaY);
        }
        return Direction.STOP;
    }

    /**
     * 确定移动方向（敌人AI的核心方法）
     * 
     * 工作流程：
     * 1. 每30帧刷新一次路径（避免每帧都计算A*，提高性能）
     * 2. 沿着计算好的路径移动
     * 3. 到达路径点后移除该点，前往下一个点
     * 4. 如果停止移动，至少保持朝向目标（用于攻击动画）
     * 
     * 为什么每30帧刷新：
     * - A*算法计算量大，每帧计算会严重影响性能
     * - 30帧约1秒，足够应对玩家移动和障碍物变化
     * - 碰撞时会立即重新计算路径
     */
    public void locateDirection() {
        if(getTarget() == null) return;  // 没有目标玩家
        
        // 每30帧刷新一次路径
        if(mainTainRefreshPath(30) == 0) {
            this.target = getTarget();  // 重新选择最近的玩家
            this.pathfinder = new Pathfinder(new WorldGrids(world), this);
            getPath();  // 使用A*算法计算新路径
        }
        
        // 路径为空，停止移动
        if(this.path == null || this.path.size() == 0){
            this.dir = Direction.STOP;
            return;
        }
        
        // 到达当前路径点，移除并前往下一个
        if(getCurrentGrid().getGridX() == path.get(0).getGridX() && getCurrentGrid().getGridY() == path.get(0).getGridY())
            path.remove(0);
        
        // 设置移动方向（朝向下一个路径点）
        if(this.path.size() > 0) {
            Grid nextGrid = path.get(0);
            this.oldDir =(this.dir == Direction.STOP) ? oldDir : dir;
            this.dir = getNextDir(nextGrid);
        }
        
        // 如果停止移动，至少保持朝向目标（用于攻击动画的朝向）
        if(this.getDir() == Direction.STOP) {
            this.oldDir = (judgeAccurateDir(target.getX(), target.getY()) == Direction.STOP) ? oldDir : judgeAccurateDir(target.getX(), target.getY());
        }
    }

    /**
     * 绘制敌人
     * 先确定方向，再调用父类绘制
     * @param g 图形上下文
     */
    public void draw(Graphics g) {
        locateDirection();  // 更新移动方向
        super.draw(g);      // 绘制角色
    }

    /**
     * 碰撞响应（重写）
     * 碰撞时停止移动并重新计算路径
     * @param object 碰撞的对象
     */
    public void collisionResponse(GameObject object){
        this.dir = Direction.STOP;  // 停止移动

        // 碰撞延迟，避免频繁重新计算路径
        if(maintainCollisionDelay(3) > 0) return;
        else{
            collisionDelay = 3;
            this.pathfinder = new Pathfinder(new WorldGrids(world), this);
            getPath();  // 重新计算路径
        }
        super.collisionResponse(object);
    }
}
