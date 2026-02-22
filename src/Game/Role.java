package Game;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 角色基类
 * 继承自GameObject，是Hero和Enemy的共同父类
 * 包含武器系统、血条、行走动画、死亡状态等角色共有功能
 */
public class Role extends GameObject{
    public static final int PICOFFSET = 32;  // 精灵图偏移量（半宽/半高）
    private int walkState;                    // 行走动画状态（0-15循环）
    private List<Weapon> weapons;             // 武器列表
    private Weapon currentWeapon;             // 当前装备的武器
    protected int deadState;                  // 死亡状态计数器（-1表示存活）
    private int maxHP;                        // 最大生命值
    private int begin;                        // 开始保护时间（闪烁效果）
    
    /**
     * 血条内部类
     * 在角色头顶显示生命值条
     */
    private class BloodBar {
        /**
         * 绘制血条
         * @param g 图形上下文
         */
        public void draw(Graphics g) {
            int maxLength = 40;  // 血条最大长度
            // 根据当前HP计算血条长度
            int length = (int)((double)getHP() / (double)getMaxHP() * 40);
            Color c = g.getColor();
            g.setColor(Color.RED);
            g.drawRect(x - 20, y - 40, maxLength, 7);  // 绘制边框
            g.fillRect(x - 20, y - 40, length, 7);     // 填充血量
            g.setColor(c);
        }
    }
    private BloodBar bloodBar;  // 血条对象

    /**
     * 构造方法
     * @param name 角色名称
     * @param HP 生命值
     * @param radius 半径
     * @param speed 速度
     * @param x X坐标
     * @param y Y坐标
     * @param world 所属世界
     */
    public Role(String name, int HP, int radius, int speed,int x, int y, World world) {
        super(name , radius, speed, HP, x, y, true, world);
        this.maxHP = this.HP;
        this.weapons = new ArrayList<>();
        this.walkState = 0;
        this.deadState = -1;  // -1表示存活
        // 英雄有250帧的初始保护时间（闪烁效果）
        if(this instanceof Hero)
        	this.begin = 250;
        else this.begin = 0;
        bloodBar = new BloodBar();
    }

    /**
     * 维护行走动画状态
     * 根据移动方向切换动画帧
     * @param n 动画帧总数
     * @return 当前动画帧索引
     */
    public int mainTainWalkState(int n){
        if(dir != Direction.STOP) {  // 如果在移动
            if (dir == oldDir) {      // 方向未改变
                walkState++;          // 动画帧递增
            } else {                  // 方向改变
                walkState = 0;        // 重置动画
                oldDir = dir;         // 更新方向
            }
        } else {
            walkState = -1;           // 停止时重置
        }
        if(walkState >= n) walkState = 0;  // 循环动画
        return walkState;
    }

    /**
     * 绘制行走动画
     * 根据移动状态选择合适的精灵图帧
     * @param g 图形上下文
     */
    public void drawWalkImage(Graphics g) {
        if(mainTainWalkState(16) < 0){  // 停止状态
            // 绘制静止帧（第0列）
            drawOneImage(g, name, PICOFFSET,this.x, this.y, 0, this.oldDir.ordinal());
        } else {  // 移动状态
            // 绘制行走动画（第1-4列，每4帧切换一次）
            drawOneImage(g, name, PICOFFSET, this.x, this.y, walkState / 4 + 1, this.dir.ordinal());
        }
    }

    /**
     * 绘制角色
     * 处理死亡动画、受击动画、武器攻击动画和正常行走
     * @param g 图形上下文
     */
    public void draw(Graphics g) {
	    // 1. 死亡状态：显示死亡动画
	    if(deadState >= 0){
	        this.drawOneImage(g, name, PICOFFSET, this.x, this.y, 13, 0);
	        this.maintainDeadState();
	        return;
	    }
	    
	    // 2. 受击状态：显示受击动画和血迹
	    if(checkOnAttack() > 0){
	        this.drawOneImage(g, name, PICOFFSET, this.x, this.y, 0, this.oldDir.ordinal());
	        Random rand = new Random();
	        if(Math.abs(rand.nextInt(100)) > 20) world.addBlood(this.x, this.y);  // 80%概率产生血迹
	        onAttackState--;
	    } 
	    // 3. 武器攻击状态：显示攻击动画
	    else if (currentWeapon.getState() >= 0 && (currentWeapon instanceof Sword || currentWeapon instanceof Hand)) {
	        this.currentWeapon.drawNomalAttack(g);
	        if (this.currentWeapon.getState() == 1)  // 攻击动画第1帧时执行伤害判定
	            this.currentWeapon.Attack();
	        this.currentWeapon.maintainColdDown();
	        return;
	    }
	    
	    // 4. 正常状态：显示行走动画
	    this.drawWalkImage(g);
        
        this.currentWeapon.maintainColdDown();  // 维护武器冷却
        move();  // 移动
    }

    /**
     * 移动方法
     * 根据当前方向更新坐标，并进行碰撞检测
     */
    public void move(){
        if((getDir() == Direction.STOP  && checkOnAttack() <= 0) || getHP() <= 0) return;  // 停止或死亡时不移动
        
        double degree = Direction.toDegree(getDir());  // 获取方向角度
        if(checkOnAttack() <= 0) {  // 非受击状态才更新移动增量
            setxIncrement(degree, speed);
            setyIncrement(degree, speed);
        }
        world.collisionDetection(this);  // 碰撞检测
        this.x += getxIncrement();       // 更新X坐标
        this.y += getyIncrement();       // 更新Y坐标
    }

    /**
     * 碰撞响应（滑动效果）
     * 
     * 当角色与障碍物碰撞时，不是完全停止，而是沿着障碍物边缘滑动
     * 
     * 工作原理：
     * 1. 计算碰撞法线方向（垂直于碰撞面的方向）
     * 2. 将移动速度投影到法线方向
     * 3. 沿着法线方向移动（滑动效果）
     * 
     * 示例：
     * - 玩家斜向移动撞到墙壁
     * - 不是完全停止，而是沿着墙壁边缘滑动
     * - 让移动更流畅，不会因为轻微碰撞就卡住
     * 
     * 数学原理：
     * - 碰撞法线：垂直于碰撞面的方向
     * - 速度投影：将速度分解为法线方向和切线方向
     * - 保留法线方向的速度，实现滑动
     * 
     * @param object 碰撞的对象
     */
    public void collisionResponse(GameObject object){
        if(this.dir == Direction.STOP) return;
        
        // 计算碰撞法线方向（从对方指向自己）
        int deltaX = object.getX() - this.getX();
        int deltaY = object.getY() - this.getY();
        
        // 计算法线的倒数向量（用于投影计算）
        double tmpVectorX = 1.0 / deltaX;
        double tmpVectorY = -1.0 / deltaY;
        double normOfTmp = Math.sqrt(Math.pow(tmpVectorX, 2) + Math.pow(tmpVectorY, 2));
        
        // 计算当前移动方向的单位向量
        double dirX = Math.cos(Math.toRadians(Direction.toDegree(this.dir)));
        double dirY = -Math.sin(Math.toRadians(Direction.toDegree(this.dir)));
        
        // 将移动速度投影到法线方向（点积运算）
        // 这样可以保留沿着障碍物边缘的速度分量
        double newSpeed = (tmpVectorX * dirX + tmpVectorY * dirY) / normOfTmp * getSpeed();
        int newDirX = (int) (newSpeed * tmpVectorX);
        int newDirY = (int) (newSpeed * tmpVectorY);
        
        // 沿法线方向移动（滑动效果）
        this.x += newDirX;
        this.y += newDirY;
    }

    /**
     * 受攻击处理
     * 扣除生命值，产生击退效果和血迹
     * @param weapon 攻击的武器
     */
    public void onAttack(Weapon weapon){
    	if(getHP() <= 0 || begin > 0) return;  // 已死亡或保护期内不受伤
    	
        this.onAttackState = 5;  // 设置受击状态持续5帧
        this.setHP(this.getHP() - weapon.getDamage());  // 扣除生命值
        
        if(getHP() <= 0) {  // 生命值归零
            this.setHP(0);
            this.setDeadState();  // 进入死亡状态
            return;
        }
        
        // 计算击退方向
        int weaponX = weapon.getX();
        int weaponY = weapon.getY();
        int deltaX = weaponX - this.x;
        int deltaY = weaponY - this.y;
        double D = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        double cosA = deltaX / D;
        double sinA = deltaY / D;
        
        // 设置击退速度（反方向）
        this.xIncrement = (int)(-cosA * 8);
        this.yIncrement = (int)(-sinA * 8);
        
        world.addBlood(this.x, this.y);  // 产生血迹
    }

    /**
     * 设置死亡状态
     * 开始死亡倒计时
     */
    public void setDeadState(){
        this.deadState = 150;  // 150帧后移除
    }

    /**
     * 获取武器数量
     * @return 武器列表大小
     */
    public int getWeaponsAmount(){
        return weapons.size();
    }

    /**
     * 添加武器
     * @param weapon 要添加的武器
     */
    public void addWeapon(Weapon weapon){
        weapons.add(weapon);
    }

    /**
     * 设置当前武器
     * @param currentWeapon 要装备的武器
     */
    public void setCurrentWeapon(Weapon currentWeapon) {
        this.currentWeapon = currentWeapon;
    }

    /**
     * 获取当前武器
     * @return 当前装备的武器
     */
    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    /**
     * 获取武器列表
     * @return 所有武器
     */
    public List<Weapon> getWeapons() {
        return weapons;
    }

    /**
     * 设置武器列表
     * @param weapons 武器列表
     */
    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }
    
    /**
     * 维护死亡状态
     * 倒计时结束后从世界中移除
     */
    public void maintainDeadState() {
    	if(deadState > 0)
        	deadState--;
    	else
    		world.objDead(this);  // 移除对象
    }

    /**
     * 切换到下一个武器
     * 循环切换武器列表
     */
    public void NextWeapon() {
        int index = weapons.indexOf(currentWeapon);
        if(index + 1 < this.getWeaponsAmount()){
            currentWeapon = weapons.get(index + 1);  // 切换到下一个
        } else {
            currentWeapon = weapons.get(0);  // 循环到第一个
        }
    }

    /**
     * 碰撞检测（重写）
     * 跳过自己的武器
     * @param object 要检测的对象
     * @return 是否发生碰撞
     */
    public boolean collisionDetection(GameObject object){
        if(object instanceof Weapon && ((Weapon) object).isHost(this)){
            return false;  // 不与自己的武器碰撞
        }
        return super.collisionDetection(object);
    }

    /**
     * 获取最大生命值
     * @return 最大HP
     */
    public int getMaxHP() {
        return maxHP;
    }

    /**
     * 绘制血条
     * @param g 图形上下文
     */
    public void drawBloodBar(Graphics g){
        bloodBar.draw(g);
    }
    
    /**
     * 获取并递减保护时间
     * @return 剩余保护时间
     */
    public int getBegin() {
    	return begin--;
    }
    
    /**
     * 重置保护时间
     * 用于复活后的无敌时间
     */
    public void resetBegin() {
    	this.begin = 350;
    }
}
