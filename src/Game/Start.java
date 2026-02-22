package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 游戏开始界面类
 * 继承自JFrame，提供单人和双人游戏模式选择
 */
public class Start extends JFrame{
	public static final int WIDTH = 400;    // 窗口宽度
	public static final int HEIGHT = 350;   // 窗口高度
	private boolean flag;                    // 标志位（未使用）
	
	/**
	 * 单人游戏按钮监听器
	 * 点击后启动单人模式游戏
	 */
	class SinglePlayerListener implements ActionListener {  
	    @Override  
	    public void actionPerformed(ActionEvent e) {  
	    	new GameClient(false).lauchFrame();  // 创建单人游戏客户端（false表示单人模式）
	    	setVisible(false);                    // 隐藏开始界面
	    }  
	} 
	
	/**
	 * 双人游戏按钮监听器
	 * 点击后启动双人模式游戏
	 */
	class DoublePlayerListener implements ActionListener {  
	    @Override  
	    public void actionPerformed(ActionEvent e) {  
	    	new GameClient(true).lauchFrame();   // 创建双人游戏客户端（true表示双人模式）
	    	setVisible(false);                    // 隐藏开始界面
	    }  
	} 
	
	/**
	 * 窗口关闭监听器
	 * 处理窗口关闭事件
	 */
	class WindowDestroyer extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			// 窗口关闭时的处理（当前为空）
		}
	}
	
	/**
	 * 构造方法
	 * 初始化开始界面窗口，创建单人和双人模式选择按钮
	 */
	public Start(){
		setSize(WIDTH, HEIGHT);              // 设置窗口大小
		setResizable(false);                 // 禁止调整窗口大小
		setLocationRelativeTo(null);         // 窗口居中显示
		addWindowListener(new WindowDestroyer());  // 添加窗口监听器
		setTitle("Zombie Crsis");            // 设置窗口标题
		Container contentPane = getContentPane();  // 获取内容面板
		contentPane.setBackground(Color.LIGHT_GRAY);  // 设置背景色
		
		// 创建按钮面板
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(Color.WHITE);
		
		// 设置流式布局
		contentPane.setLayout(new FlowLayout());
		
		// 创建单人模式按钮
		ImageIcon singleIcon = new ImageIcon(Start.class.getResource("/images/single.jpg"));
		singleIcon.setImage(singleIcon.getImage().getScaledInstance(380,140,Image.SCALE_DEFAULT));  // 缩放图片
		JButton singleButton = new JButton(singleIcon);
		singleButton.setIcon(singleIcon);
		singleButton.addActionListener(new SinglePlayerListener());  // 添加点击监听器
		contentPane.add(singleButton);  // 添加到面板
		
		// 创建双人模式按钮
		ImageIcon doubleIcon = new ImageIcon(Start.class.getResource("/images/double.jpg"));
		doubleIcon.setImage(doubleIcon.getImage().getScaledInstance(380,140,Image.SCALE_DEFAULT));  // 缩放图片
		JButton doubleButton = new JButton(doubleIcon);
		doubleButton.setIcon(doubleIcon);
		doubleButton.addActionListener(new DoublePlayerListener());  // 添加点击监听器
		contentPane.add(doubleButton);  // 添加到面板
		
		setVisible(true);  // 显示窗口
	}
}
