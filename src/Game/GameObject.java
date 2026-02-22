package Game;

import javax.naming.event.ObjectChangeListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 游戏对象抽象基类
 * 所有游戏中的实体对象（角色、武器、障碍物等）的父类
 * 定义了对象的基本属性和行为
 */
public abstract class GameObject implements Cloneable{
    protected String name;           // 对象名称
    protected int radius;            // 半径（用于碰撞检测）
    protected int speed;             // 移动速度
    protected int xIncrement;        // X轴增量（每帧移动距离）
    protected int yIncrement;        // Y轴增量（每帧移动距离）
    protected Direction dir;         // 当前方向
    protected Direction oldDir;      // 上一次的方向（用于停止时保持朝向）
    protected int x, y;              // 对象坐标
    protected int HP;                // 生命值
    protected int onAttackState;     // 受击状态计数器（>0表示正在受击）
    protected boolean collidable;    // 是否可碰撞
    protected World world;           // 所属的游戏世界
    protected static Toolkit tk = Toolkit.getDefaultToolkit();  // 工具包
    protected static Image[] imgs = null;                        // 图片数组
    protected static Map<String, Image> imgMap = new HashMap<String, Image>();  // 图片映射表

    /**
     * 绘制方法（抽象）
     * 子类必须实现具体的绘制逻辑
     */
    public abstract void draw(Graphics g);
    
    /**
     * 碰撞响应方法（抽象）
     * 子类必须实现碰撞后的处理逻辑
     */
    public abstract void collisionResponse(GameObject object);
    
    /**
     * 受攻击方法（抽象）
     * 子类必须实现被武器攻击时的处理逻辑
     */
    public abstract void onAttack(Weapon weapon);

    /**
     * 静态初始化块
     * 加载所有游戏图片资源并建立映射关系
     */
    static {
        imgs = new Image[] {
                tk.getImage(GameObject.class.getClassLoader().getResource("images/hero.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/monster.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/fireball.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/ghost.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/ghostball.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/wall.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/blood.png")),
                tk.getImage(GameObject.class.getClassLoader().getResource("images/box.png"))
        };
        imgMap.put("Hero", imgs[0]);
        imgMap.put("Monster", imgs[1]);
        imgMap.put("Fireball", imgs[2]);
        imgMap.put("Ghost", imgs[3]);
        imgMap.put("Ghostball", imgs[4]);
        imgMap.put("Wall", imgs[5]);
        imgMap.put("Blood", imgs[6]);
        imgMap.put("Box", imgs[7]);
    }

    /**
     * 构造方法
     * @param name 对象名称
     * @param radius 半径
     * @param speed 速度
     * @param HP 生命值
     * @param x X坐标
     * @param y Y坐标
     * @param collidable 是否可碰撞
     * @param world 所属世界
     */
    public GameObject(String name, int radius, int speed, int HP, int x, int y, boolean collidable, World world) {
        this.name = name;
        this.radius = radius;
        this.speed = speed;
        this.xIncrement = 0;
        this.yIncrement = 0;
        this.onAttackState = 0;
        this.HP = HP;
        this.dir = Direction.STOP;
        this.oldDir = Direction.D;
        this.x = x;
        this.y = y;
        this.collidable = collidable;
        this.world = world;
    }

    /**
     * 构造方法（带初始方向）
     * @param name 对象名称
     * @param radius 半径
     * @param speed 速度
     * @param dir 初始方向
     * @param HP 生命值
     * @param x X坐标
     * @param y Y坐标
     * @param collidable 是否可碰撞
     * @param world 所属世界
     */
    public GameObject(String name, int radius, int speed, Direction dir, int HP, int x, int y, boolean collidable, World world) {
        this.name = name;
        this.radius = radius;
        this.speed = speed;
        this.xIncrement = 0;
        this.yIncrement = 0;
        this.onAttackState = 0;
        this.dir = dir;
        this.HP = HP;
        this.oldDir = Direction.D;
        this.x = x;
        this.y = y;
        this.collidable = collidable;
        this.world = world;
    }

    /**
     * 绘制单个图片帧
     * 从精灵图中截取指定位置的图片并绘制到指定位置
     * @param g 图形上下文
     * @param name 图片名称
     * @param picOffset 图片偏移量（半宽/半高）
     * @param x 绘制X坐标
     * @param y 绘制Y坐标
     * @param picX 精灵图中的X索引
     * @param picY 精灵图中的Y索引
     */
    public void drawOneImage(Graphics g, String name, int picOffset,int x, int y, int picX, int picY){
        g.drawImage(
                imgMap.get(name),
                x - picOffset - 4,                          // 目标区域左上角X
                y - picOffset - 4,                          // 目标区域左上角Y
                x + picOffset + 4,                          // 目标区域右下角X
                y + picOffset + 4,                          // 目标区域右下角Y
                picX * picOffset * 2,                       // 源图片左上角X
                picY * picOffset * 2,                       // 源图片左上角Y
                picX * picOffset * 2 + picOffset * 2 - 1,  // 源图片右下角X
                picY * picOffset * 2 + picOffset * 2 - 1,  // 源图片右下角Y
                null);
    }

    /**
     * 碰撞检测（圆形碰撞检测算法）
     * 
     * 工作原理：
     * 1. 计算两个对象中心点的距离
     * 2. 如果距离 ≤ 两个半径之和，则发生碰撞
     * 3. 碰撞时将当前对象推开，避免重叠
     * 
     * 推开算法：
     * - 计算碰撞方向的单位向量（cosValue, sinValue）
     * - 计算需要分离的距离：R - d + 2
     * - 沿着碰撞方向推开当前对象
     * 
     * 示例：
     * - 玩家半径14，墙壁半径50，半径和R=64
     * - 如果距离d=60，重叠了4像素
     * - 需要推开距离：64-60+2=6像素
     * - 沿着远离墙壁的方向推开6像素
     * 
     * @param object 要检测的对象
     * @return 是否发生碰撞
     */
    public boolean collisionDetection(GameObject object){
        if(!object.isCollidable()) return false;  // 对方不可碰撞
        if(this.getDir() == Direction.STOP  && this.checkOnAttack() <= 0) return false;  // 自己静止且未受击
        
        // 计算两个对象中心点的距离
        double deltaX = this.x - object.getX();
        double deltaY = (this.y - object.getY());
        double d = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        int R = this.getRadius() + object.getRadius();  // 两个半径之和
        
        if(d <= R){  // 距离小于等于半径和，发生碰撞
            // 计算碰撞方向的单位向量
            double cosValue = deltaX / d;  // X方向分量
            double sinValue = deltaY / d;  // Y方向分量
            
            // 计算分离距离（让两个对象不再重叠）
            // R - d：重叠的距离
            // +2：额外的安全距离，防止浮点误差导致仍然重叠
            int offsetY = (int)((R - d + 2) * sinValue);
            int offsetX = (int)((R - d + 2) * cosValue);
            
            // 将当前对象沿碰撞方向推开
            this.x += offsetX;
            this.y += offsetY;
            return true;
        }
        return false;
    }

    /**
     * 检查受击状态
     * @return 受击状态计数器值
     */
    public int checkOnAttack(){
        return (this.onAttackState);
    }

    /**
     * 重置受击状态
     */
    public void resetOnAttackState(){
        this.onAttackState = 0;
    }

    // ========== Getter和Setter方法 ==========
    
    public int getSpeed() {
        return this.speed;
    }

    public int getRadius() {
        return this.radius;
    }

    public Direction getDir() {
        return this.dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public Direction getOldDir() {
        return this.oldDir;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getHP() {
        return this.HP;
    }

    /**
     * 设置生命值
     * 当HP降为0且不是英雄时，设置为不可碰撞
     */
    public void setHP(int HP) {
    	if(HP == 0 && !(this instanceof Hero))
    		this.collidable = false;
        this.HP = HP;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public int getxIncrement() {
        return xIncrement;
    }

    /**
     * 根据角度和速度设置X轴增量
     * @param degree 角度（度）
     * @param speed 速度
     */
    public void setxIncrement(double degree, int speed) {
        this.xIncrement = (int)(getSpeed() * Math.cos(Math.toRadians(degree)));
    }

    public int getyIncrement() {
        return yIncrement;
    }

    /**
     * 根据角度和速度设置Y轴增量
     * 注意：Y轴向下为正，所以需要取负
     * @param degree 角度（度）
     * @param speed 速度
     */
    public void setyIncrement(double degree, int speed) {
        this.yIncrement = -(int)(getSpeed() * Math.sin(Math.toRadians(degree)));
    }

    /**
     * 计算两点之间的距离
     * 两点坐标分别为(x1, y1)和(x2, y2)，使用欧几里得距离公式
     * @param x1 点1的X坐标
     * @param y1 点1的Y坐标
     * @param x2 点2的X坐标
     * @param y2 点2的Y坐标
     * @return 距离值
     */
    public double getDistance(int x1, int y1, int x2, int y2){
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    /**
     * 克隆方法
     * 实现对象的深拷贝
     */
    @Override
    public Object clone() {
        GameObject obj = null;
        try{
            obj = (GameObject) super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
