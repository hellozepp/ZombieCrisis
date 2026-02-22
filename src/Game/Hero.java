package Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * 英雄类（玩家角色）
 * 继承自Role，处理键盘输入和玩家控制
 * 支持两套按键方案（双人模式）
 */
public class Hero extends Role {
    public static final int MAX_HP = 1200;  // 最大生命值
    private int[] keys;                      // 按键映射数组
    private boolean bL=false, bU=false, bR=false, bD=false;  // 方向键状态

    /**
     * 构造方法
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param keyGroup 按键组（0为方向键+小键盘，1为WASD+JKL）
     * @param world 所属世界
     */
    public Hero(int x, int y, int keyGroup, World world) {
        super("Hero", MAX_HP, 14, 5, x, y, world);
        // name="Hero": 角色名称
        // HP=1200: 生命值
        // radius=14: 碰撞半径
        // speed=5: 移动速度
        
        // 添加武器
        addWeapon(new Sword(this, world));     // 近战剑
        addWeapon(new Fireball(this, world));  // 远程火球
        setCurrentWeapon(getWeapons().get(0)); // 默认装备剑
        
        keys = new int[7];  // 7个按键：左上右下、攻击、切换武器、大招
        
        // 根据按键组设置按键映射
        if(keyGroup == 0){  // 2P按键（方向键+小键盘）
            keys[0] = KeyEvent.VK_LEFT;     // 左
            keys[1] = KeyEvent.VK_UP;       // 上
            keys[2] = KeyEvent.VK_RIGHT;    // 右
            keys[3] = KeyEvent.VK_DOWN;     // 下
            keys[4] = KeyEvent.VK_NUMPAD1;  // 攻击
            keys[5] = KeyEvent.VK_NUMPAD2;  // 切换武器
            keys[6] = KeyEvent.VK_NUMPAD3;  // 大招
        } else {  // 1P按键（WASD+JKL）
            keys[0] = KeyEvent.VK_A;  // 左
            keys[1] = KeyEvent.VK_W;  // 上
            keys[2] = KeyEvent.VK_D;  // 右
            keys[3] = KeyEvent.VK_S;  // 下
            keys[4] = KeyEvent.VK_J;  // 攻击
            keys[5] = KeyEvent.VK_K;  // 切换武器
            keys[6] = KeyEvent.VK_L;  // 大招
        }
    }

    /**
     * 按键释放处理
     * @param e 键盘事件
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == keys[0]) bL = false;       // 释放左键
        else if(key == keys[1]) bU = false;  // 释放上键
        else if(key == keys[2]) bR = false;  // 释放右键
        else if(key == keys[3]) bD = false;  // 释放下键
        else if(key == keys[4]);             // 攻击键（无需处理释放）
        else if(key == keys[5]);             // 切换武器键（无需处理释放）
        else if(key == keys[6]);             // 大招键（无需处理释放）
        locateDirection();  // 更新移动方向
    }

    /**
     * 按键按下处理
     * @param e 键盘事件
     */
    public void KeyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == keys[0]) bL = true;        // 按下左键
        else if(key == keys[1]) bU = true;   // 按下上键
        else if(key == keys[2]) bR = true;   // 按下右键
        else if(key == keys[3]) bD = true;   // 按下下键
        else if(key == keys[4] && this.getCurrentWeapon().getColdDown() == 0) {
            // 攻击键：冷却结束才能攻击
            this.getCurrentWeapon().setState();
            this.getCurrentWeapon().setColdDown();
        } else if(key == keys[5]){
            // 切换武器键
            this.NextWeapon();
        } else if(key == keys[6] && this.getCurrentWeapon().getColdDown() == 0){
            // 大招键：只有火球武器支持大招
            Weapon weapon = this.getCurrentWeapon();
            if(weapon instanceof Fireball) {
                ((Fireball) this.getCurrentWeapon()).setUltimateState();
                this.getCurrentWeapon().setColdDown();
            }
        }
        locateDirection();  // 更新移动方向
    }

    /**
     * 根据按键状态确定移动方向
     * 支持8方向移动
     */
    public void locateDirection() {
        if(bL && !bU && !bR && !bD) dir = Direction.L;        // 左
        else if(bL && bU && !bR && !bD) dir = Direction.LU;   // 左上
        else if(!bL && bU && !bR && !bD) dir = Direction.U;   // 上
        else if(!bL && bU && bR && !bD) dir = Direction.RU;   // 右上
        else if(!bL && !bU && bR && !bD) dir = Direction.R;   // 右
        else if(!bL && !bU && bR && bD) dir = Direction.RD;   // 右下
        else if(!bL && !bU && !bR && bD) dir = Direction.D;   // 下
        else if(bL && !bU && !bR && bD) dir = Direction.LD;   // 左下
        else if(!bL && !bU && !bR && !bD) dir = Direction.STOP;  // 停止
    }

    /**
     * 绘制英雄
     * 显示武器信息、血条和角色动画
     * @param g 图形上下文
     */
    public void draw(Graphics g){
        // 显示当前武器信息
        g.drawString(getCurrentWeapon().toString(), this.x - 20, this.y - 45);
        
        // 绘制血条
        drawBloodBar(g);
        
        // 保护时间处理（闪烁效果）
        int b = this.getBegin();
        if(b > 0 && (b / 3) % 2 == 0) {
            // 保护期内每3帧闪烁一次（不绘制角色）
        	this.getCurrentWeapon().maintainColdDown();
        	mainTainWalkState(16);
            move();
        } else {
            // 正常绘制
        	super.draw(g);
        }
    }
    
    /**
     * 设置死亡状态（重写）
     * 英雄死亡时停止移动，延长死亡动画时间
     */
    public void setDeadState(){
    	this.setxIncrement(0, 0);   // 停止X方向移动
    	this.setyIncrement(0, 0);   // 停止Y方向移动
    	this.onAttackState = 0;     // 清除受击状态
        this.deadState = 600;       // 死亡动画持续600帧（比敌人长）
    }
    
    /**
     * 重置保护时间（重写）
     * 复活时重置位置、生命值和保护时间
     */
    public void resetBegin() {
        // 随机选择复活位置（左或右）
        this.x = (new Random().nextInt(100) % 2 == 0) ? 340 : 620;
        this.y = 280;
        this.setHP(MAX_HP);      // 满血复活
        this.deadState = -1;     // 重置死亡状态
        super.resetBegin();      // 设置保护时间
    }
    
    /**
     * 维护死亡状态（重写）
     * 检查是否还有其他存活玩家，决定是否复活
     */
    public void maintainDeadState() {
    	world.searchHero();  // 检查是否还有存活玩家
    	if(deadState <= 0) {
        	if(world.searchHero()) {
        		// 还有其他玩家存活，复活当前玩家
        		this.resetBegin();
        	} else {
        		// 所有玩家死亡，游戏结束
        		world.objDead(this);
        	}
    	} else {
    		this.deadState--;  // 死亡倒计时
    	}
    }
}
