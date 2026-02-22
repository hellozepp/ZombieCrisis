package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 游戏客户端主窗口类
 * 继承自AWT的Frame，负责游戏的主循环、渲染和键盘输入处理
 * 使用双缓冲技术避免画面闪烁
 */
public class GameClient extends Frame {
    public static final int WORLD_WIDTH = 960;   // 游戏世界宽度
    public static final int WORLD_HEIGHT = 720;  // 游戏世界高度
    private Image offScreenImage;                 // 离屏图像（用于双缓冲）
    private World world;                          // 游戏世界对象

    /**
     * 构造方法
     * @param Doubleplayer 是否为双人模式（true为双人，false为单人）
     */
    public GameClient(boolean Doubleplayer){
        this.world = new World(WORLD_WIDTH, WORLD_HEIGHT, Doubleplayer);
        offScreenImage = null;
    }

    /**
     * 绘制线程内部类
     * 负责游戏的主循环，每30毫秒刷新一次画面
     */
    private class PaintThread implements Runnable {
        public void run() {
            while(true) {
                repaint();     // 调用外部类的paint()方法，repaint()首先调用update()方法，再调用paint()方法
                try {
                    Thread.sleep(30);  // 控制帧率，约33FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 绘制方法
     * 根据游戏状态绘制游戏世界或游戏结束画面
     * @param g 图形上下文对象
     */
    public void paint(Graphics g) {
    	if(!world.End())        // 如果游戏未结束
    		world.drawWorld(g);  // 绘制游戏世界
    	else {
    		world.drawEnd(g);    // 绘制游戏结束画面
    	}
    }

    /**
     * 更新方法（双缓冲实现）
     * 先在离屏图像上绘制，然后一次性绘制到屏幕，避免闪烁
     * @param g 图形上下文对象
     */
    public void update(Graphics g) {
        if(offScreenImage == null) {
            offScreenImage = this.createImage(WORLD_WIDTH, WORLD_HEIGHT);   // 创建离屏图像
        }
        Graphics gOffScreen = offScreenImage.getGraphics();  // 获取离屏图像的图形上下文
        Color c = gOffScreen.getColor();                     // 保存当前颜色
        gOffScreen.setColor(Color.lightGray);                // 设置背景色
        gOffScreen.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT); // 填充背景
        gOffScreen.setColor(c);                              // 恢复颜色
        paint(gOffScreen);                                   // 在离屏图像上绘制
        g.drawImage(offScreenImage, 0, 0, null);            // 将离屏图像绘制到屏幕
    }

    /**
     * 启动游戏窗口
     * 初始化窗口属性，添加键盘监听器，启动绘制线程
     */
    public void lauchFrame() {
        this.setLocation(400, 100);                  // 设置窗口位置
        this.setSize(WORLD_WIDTH, WORLD_HEIGHT);     // 设置窗口大小
        this.setTitle("ZombieCrisis");               // 设置窗口标题
        this.addWindowListener(new WindowAdapter() {  // 添加窗口关闭监听器
            public void windowClosing(WindowEvent e) {
                System.exit(0);                       // 关闭窗口时退出程序
            }
        });
        this.setResizable(false);                    // 禁止调整窗口大小
        this.setBackground(Color.lightGray);         // 设置背景色
        
        // 为第一个玩家（索引0）添加键盘监听器
        this.addKeyListener(new KeyMonitor((Hero) world.getObject(0)));
        
        // 如果是双人模式，为第二个玩家（索引1）添加键盘监听器
        if(world.getObject(1) instanceof Hero)
            this.addKeyListener(new KeyMonitor((Hero) world.getObject(1)));
        
        setVisible(true);                            // 显示窗口
        new Thread(new PaintThread()).start();       // 启动绘制线程
    }

    /**
     * 键盘监听器内部类
     * 监听键盘事件并传递给对应的英雄对象处理
     */
    private class KeyMonitor extends KeyAdapter {
        Hero hero;  // 关联的英雄对象
        
        /**
         * 构造方法
         * @param hero 要监听的英雄对象
         */
        public KeyMonitor(Hero hero){
            this.hero = hero;
        }
        
        /**
         * 键盘释放事件处理
         * @param e 键盘事件对象
         */
        public void keyReleased(KeyEvent e) {
            this.hero.keyReleased(e);
        }
        
        /**
         * 键盘按下事件处理
         * @param e 键盘事件对象
         */
        public void keyPressed(KeyEvent e) {
            this.hero.KeyPressed(e);
        }
    }
}
