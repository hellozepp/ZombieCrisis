package Game;

import java.awt.*;

/**
 * 弹丸基类
 * 继承自Weapon，是所有远程弹丸武器的父类
 * 实现Cloneable接口支持弹丸克隆（用于大招多方向发射）
 */
public class Ball extends Weapon implements Cloneable{
    private final int attackRange = 40;           // 攻击范围（碰撞半径）
    private boolean ultimateState;                // 大招状态
    private int num;                              // 弹药数量
    protected int picOffset;                      // 精灵图偏移量
    private int[] imgOrder = {4,7,5,6,1,2,3,0};  // 图片顺序映射（方向到图片索引）

    /**
     * 构造方法
     * @param name 弹丸名称
     * @param radius 半径
     * @param speed 速度
     * @param damage 伤害
     * @param coldDownTime 冷却时间
     * @param role 持有者
     * @param world 所属世界
     */
    public Ball(String name, int radius, int speed, int damage, int coldDownTime, Role role, World world){
        super(name, radius, speed, damage, coldDownTime, role, 300, 360,true, world);
        // attackRange=300: 弹丸飞行距离
        // attackAngle=360: 全方向攻击（碰撞即伤害）
    }

    /**
     * 初始化并发射弹丸
     * 根据指定方向创建弹丸副本并加入世界
     * @param dir 发射方向
     */
    public void initFireball(Direction dir){
        if(this.num <= 0) return;  // 弹药不足
        
        this.setDir(dir);  // 设置弹丸方向
        
        // 计算发射方向的单位向量
        double cosA = (Math.cos(Math.toRadians(Direction.toDegree(dir))));
        double sinA = -(Math.sin(Math.toRadians(Direction.toDegree(dir))));
        
        // 设置弹丸初始位置（从角色边缘发射）
        this.x = (int)(this.host.getX() + this.host.getRadius() * cosA);
        this.y = (int)(this.host.getY() + this.host.getRadius() * sinA);
        
        // 设置弹丸速度向量
        this.xIncrement = (int)(this.speed * cosA);
        this.yIncrement = (int)(this.speed * sinA);
        
        world.addObject((Ball) this.clone());  // 克隆弹丸并加入世界
        this.num--;  // 消耗弹药
    }

    /**
     * 获取弹药数量
     * @return 剩余弹药
     */
    public int getNum() {
        return num;
    }

    /**
     * 设置弹药数量
     * @param num 弹药数量
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * 攻击方法（重写）
     * 弹丸通过碰撞造成伤害，不需要主动攻击判定
     */
    public void Attack(){
        // 弹丸的伤害在collisionResponse中处理
    }

    /**
     * 碰撞检测（重写）
     * 弹丸不与持有者和其他武器碰撞
     * @param object 要检测的对象
     * @return 是否发生碰撞
     */
    @Override
    public boolean collisionDetection(GameObject object) {
        return ((!object.equals(this.host)) && !(object instanceof Weapon) && super.collisionDetection(object));
    }

    /**
     * 绘制弹丸
     * 每帧更新位置并绘制动画
     * @param g 图形上下文
     */
    public void draw(Graphics g){
        // 根据方向选择图片帧
        int picY = imgOrder[dir == Direction.STOP ? oldDir.ordinal() : dir.ordinal()];
        int picX = maintainState(3);  // 3帧循环动画
        
        // 更新弹丸位置
        this.x += xIncrement;
        this.y += yIncrement;
        
        // 绘制弹丸
        drawOneImage(g, this.name, getPicOffset(), this.x, this.y, picX, picY);
        
        // 碰撞检测
        world.collisionDetection(this);
    }

    /**
     * 设置攻击状态（重写）
     * 发射单个弹丸
     */
    public void setState(){
        initFireball(this.host.getDir() == Direction.STOP ? host.getOldDir() : host.getDir());
        super.setState();
    }

    /**
     * 设置大招状态
     * 8方向同时发射弹丸
     */
    public void setUltimateState(){
        if(getNum() < 8) return;  // 弹药不足8发
        
        // 遍历8个方向
        for(Direction dir : Direction.values()){
            if(dir == Direction.STOP) continue;  // 跳过停止方向
            initFireball(dir);  // 每个方向发射一个弹丸
        }
        super.setState();
    }

    /**
     * 维护动画状态（重写）
     * 弹丸动画循环播放
     * @param n 动画帧数
     * @return 当前帧索引
     */
    public int maintainState(int n){
        this.state++;
        if(state >= n) {
            state = 0;  // 循环动画
        }
        return state;
    }

    /**
     * 碰撞响应
     * 弹丸碰撞后消失并对目标造成伤害
     * @param object 碰撞的对象
     */
    public void collisionResponse(GameObject object){
        world.removeObject(this);  // 从世界移除弹丸
        resetState();              // 重置状态
        object.onAttack(this);     // 对目标造成伤害
    }

    /**
     * 获取精灵图偏移量
     * @return 偏移量
     */
    public int getPicOffset(){
        return picOffset;
    }

    /**
     * 转换为字符串
     * 显示武器名称和弹药数量
     * @return 格式化字符串
     */
    @Override
    public String toString(){
        String strBuf = name;
        strBuf += ":";
        strBuf += String.valueOf(getNum());
        return strBuf;
    }
}
