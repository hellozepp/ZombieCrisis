package Game;

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 游戏世界类
 * 管理游戏中的所有对象、敌人生成、碰撞检测、游戏状态等核心逻辑
 * 使用CopyOnWriteArrayList保证线程安全
 */
public class World {
    private CopyOnWriteArrayList<GameObject> objects;  // 游戏对象列表（线程安全）
    private CopyOnWriteArrayList<Blood> bloods;        // 血迹列表
    private List<Box> pickedBoxes;                     // 已被拾取的宝箱列表（等待重生）
    private int maxBloodNum = 5000;                    // 最大血迹数量
    private int bloodNum;                              // 当前血迹编号
    private int width;                                 // 世界宽度
    private int height;                                // 世界高度
    private int maxEnemyNum;                           // 当前波次最大敌人数量
    private int currentEnemyNum;                       // 当前存活敌人数量
    private int producedEnemyNum;                      // 当前波次已生成敌人数量
    private int produceDelay;                          // 敌人生成延迟计数器
    private int boxDelay;                              // 宝箱生成延迟计数器
    private Image endImg;                              // 游戏结束图片
    private int end;                                   // 游戏结束倒计时（-1表示未结束）

    /**
     * 构造方法
     * @param width 世界宽度
     * @param height 世界高度
     * @param Doubleplayer 是否为双人模式
     */
    public World(int width, int height, boolean Doubleplayer) {
        this.width = width;
        this.height = height;
        this.objects = new CopyOnWriteArrayList<>();
        this.bloods = new CopyOnWriteArrayList<>();
        this.pickedBoxes = new ArrayList<>();
        this.bloodNum = 0;
        this.maxEnemyNum = 3;                          // 初始波次3个敌人
        this.currentEnemyNum = 0;
        this.producedEnemyNum = 0;
        this.boxDelay = 0;
        this.end = -1;                                 // -1表示游戏未结束
        this.endImg = Toolkit.getDefaultToolkit().getImage(World.class.getClassLoader().getResource("images/gameover.png"));
        
        // 添加玩家1（位置340, 180，键盘组1）
        objects.add(new Hero(340, 180, 1, this));
        
        // 如果是双人模式，添加玩家2（位置620, 180，键盘组0）
        if(Doubleplayer)
        	objects.add(new Hero(620, 180, 0, this));
        
        // 添加四个边界（上下左右）
        objects.add(new Border(0, this));
        objects.add(new Border(1, this));
        objects.add(new Border(2, this));
        objects.add(new Border(3, this));
        
        // 添加两个宝箱
        objects.add(new Box(320, 360, this));
        objects.add(new Box(640, 360, this));
        
        // 添加墙壁（3x2网格布局）
        for(int i = 1; i <= 2; i++)
            for(int j = 1; j <= 3; j++){
                objects.add(new Wall(width / 4 * j, height / 3 * i, this));
        }
    }

    /**
     * 获取游戏对象迭代器
     * @return 对象列表的迭代器
     */
    public Iterator<GameObject> getObjectsIterator(){
        return objects.iterator();
    }

    /**
     * 从世界中移除对象
     * @param obj 要移除的对象
     */
    public void removeObject(GameObject obj){
        objects.remove(obj);
    }

    /**
     * 向世界中添加对象
     * @param obj 要添加的对象
     */
    public void addObject(GameObject obj){
        objects.add(obj);
    }

    /**
     * 获取指定索引的对象
     * @param index 对象索引
     * @return 游戏对象
     */
    public GameObject getObject(int index){
        return objects.get(index);
    }

    /**
     * 获取世界宽度
     * @return 宽度值
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取世界高度
     * @return 高度值
     */
    public int getHeight() {
        return height;
    }

    /**
     * 获取当前存活敌人数量
     * @return 敌人数量
     */
    public int getCurrentEnemyNum() {
        return currentEnemyNum;
    }

    /**
     * 设置当前存活敌人数量
     * @param currentEnemyNum 敌人数量
     */
    public void setCurrentEnemyNum(int currentEnemyNum) {
        this.currentEnemyNum = currentEnemyNum;
    }

    /**
     * 设置敌人生成延迟
     * 每次生成敌人后调用，防止敌人瞬间全部生成
     */
    public void setProduceDelay(){
        this.produceDelay = 50;
    }

    /**
     * 拾取宝箱
     * 将宝箱从世界中移除并加入重生队列
     * @param box 被拾取的宝箱
     */
    public void pickUpBox(Box box){
        this.removeObject(box);
        pickedBoxes.add(box);
        box.setDelay(Box.DELAYTIME);  // 设置重生延迟
    }

    /**
     * 生成宝箱
     * 检查已拾取的宝箱，延迟结束后重新加入世界
     */
    public void produceBox(){
        for(Box box : pickedBoxes){
            if(box.getDelay() == 0){
                this.addObject(box);
                pickedBoxes.remove(box);
                break;
            } else {
                box.setDelay(box.getDelay() - 1);
            }
        }
    }

    /**
     * 生成敌人
     * 在地图四个边界随机位置生成敌人（Monster或Ghost）
     * 每波敌人全部消灭后，下一波敌人数量+3
     */
    public void produceEnemy(){
    	produceDelay = (produceDelay - 1 > 0? produceDelay - 1 : 0);  // 延迟倒计时
    	if(currentEnemyNum >= 100) return;  // 限制最大敌人数量
    	
        if(producedEnemyNum < maxEnemyNum && produceDelay <= 0){
            Random rand = new Random();
            int pos = Math.abs(rand.nextInt()) % 4;      // 随机选择边界（0上1左2下3右）
            int type = Math.abs(rand.nextInt()) % 100;   // 随机敌人类型（10%概率Ghost，90%概率Monster）
            int t = (rand.nextInt() % 2) * Role.PICOFFSET * 2;  // 随机偏移
            int off = Role.PICOFFSET + 10;
            
            // 根据位置和类型生成敌人
            switch (pos){
                case 0:  // 上边界
                    objects.add(type < 10 ? new Ghost(width / 2 + t, off, this) : new Monster(width / 2 + t, off, this));
                    break;
                case 1:  // 左边界
                    objects.add(type < 10 ? new Ghost(off, height / 2 + t, this) : new Monster(off, height / 2 + t, this));
                    break;
                case 2:  // 下边界
                    objects.add(type < 10 ? new Ghost(width / 2 + t, height - off, this) : new Monster(width / 2 + t, height - off, this));
                    break;
                case 3:  // 右边界
                    objects.add(type < 10 ? new Ghost(width - off, height / 2 + t, this) : new Monster(width -off, height / 2 + t, this));
                    break;
                default:
                    break;
            }
            currentEnemyNum++;
            producedEnemyNum++;
            setProduceDelay();  // 设置生成延迟
        } else if(currentEnemyNum <= 0 && producedEnemyNum == maxEnemyNum) {
            // 当前波次敌人全部消灭，开始下一波
            maxEnemyNum += 3;  // 下一波敌人数量+3
            producedEnemyNum = 0;
        }
    }

    /**
     * 碰撞检测
     * 检测指定对象与世界中所有其他对象的碰撞
     * @param obj 要检测的对象
     * @return 是否发生碰撞
     */
    public boolean collisionDetection(GameObject obj){
        Iterator<GameObject> iter = this.getObjectsIterator();
        int flag = 0;
        while(iter.hasNext()){
            GameObject tmpObj = iter.next();
            // 跳过自己和已死亡的对象
            if(!obj.equals(tmpObj) && tmpObj.getHP() > 0){
                if(obj.collisionDetection(tmpObj)){
                    obj.collisionResponse(tmpObj);  // 触发碰撞响应
                    flag = 1;
                }
            }
        }
        if(flag == 1) return true;
        else return false;
    }

    /**
     * 对象排序
     * 按Y坐标排序（Y相同则按X排序），实现遮挡效果
     * Y坐标小的对象先绘制，Y坐标大的对象后绘制（在上层）
     */
    public void objectSort(){
        Collections.sort(objects, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject obj1, GameObject obj2) {
                int i = obj1.getY() - obj2.getY();
                if(i == 0){
                    return obj1.getX() - obj2.getX();
                }
                return i;
            }
        });
    }

    /**
     * 绘制游戏世界
     * 每帧调用，绘制所有游戏对象
     * @param g 图形上下文
     */
    public void drawWorld(Graphics g){
    	if(isEnd()) {  // 如果游戏结束倒计时开始
    		end--;
    	}
        produceEnemy();  // 生成敌人
        produceBox();    // 生成宝箱
        
        // 绘制所有血迹
        for(Blood blood : bloods){
            blood.draw(g);
        }
        
        // 对象排序后绘制
        this.objectSort();
        Iterator<GameObject> iter =this.getObjectsIterator();
        while(iter.hasNext()){
            iter.next().draw(g);
        }
    }

    /**
     * 对象死亡处理
     * @param obj 死亡的对象
     */
    public void objDead(Object obj){
        if(obj instanceof Enemy) currentEnemyNum--;  // 如果是敌人，减少敌人计数
        this.objects.remove(obj);
    }

    /**
     * 添加血迹编号
     * 循环使用血迹数组，避免无限增长
     * @return 当前血迹编号
     */
    public int addBloodNum(){
        bloodNum = (bloodNum + 1) % maxBloodNum;
        return bloodNum;
    }

    /**
     * 搜索存活的英雄
     * 检查是否还有存活的玩家，如果没有则游戏结束
     * @return 是否有存活的英雄
     */
    public boolean searchHero() {
    	Iterator<GameObject> iter = getObjectsIterator();
    	while(iter.hasNext()) {
    		GameObject obj = iter.next();
    		if(obj instanceof Hero && obj.getHP() > 0) return true;
    	}
    	gameOver();  // 没有存活英雄，游戏结束
    	return false;
    }
    
    /**
     * 添加血迹
     * 在指定位置添加血迹效果
     * @param x X坐标
     * @param y Y坐标
     */
    public void addBlood(int x, int y){
        int n = addBloodNum();
        if(bloods.size() < maxBloodNum){
            bloods.add(new Blood(x, y, this));
        } else {
            bloods.set(n, new Blood(x, y, this));  // 循环覆盖旧血迹
        }
    }
    
    /**
     * 绘制游戏结束画面
     * @param g 图形上下文
     */
    public void drawEnd(Graphics g) {
    	g.drawImage(endImg, 0, 0, width, height, null);
    }
    
    /**
     * 游戏结束
     * 设置游戏结束倒计时
     */
    public void gameOver() {
    	if(!isEnd())
    		this.end = 30;  // 30帧后显示结束画面
    }
    
    /**
     * 判断是否处于结束倒计时状态
     * @return 是否在倒计时
     */
    public boolean isEnd() {
    	return this.end >= 0;
    }
    
    /**
     * 判断游戏是否完全结束
     * @return 是否结束
     */
    public boolean End() {
    	return this.end == 0;
    }
}
