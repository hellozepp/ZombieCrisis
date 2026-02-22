package Game;

import java.awt.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * 网格类（内部类）
 * 用于A*寻路算法的地图网格表示
 * 实现Cloneable接口支持网格克隆
 */
class Grid implements Cloneable{
    public static final int LENGTH = 10;  // 网格边长（像素）
    private int gridX, gridY;             // 网格坐标（网格索引）
    private int x, y;                     // 实际坐标（像素坐标）
    private boolean accessible;           // 是否可通行
    private GameObject object;            // 当前网格上的对象
    private boolean isBorder;             // 是否为边界

    /**
     * 构造方法
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @param isBorder 是否为边界
     */
    public Grid(int gridX, int gridY, boolean isBorder) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.x = gridX * LENGTH;      // 转换为像素坐标
        this.y = gridY * LENGTH;
        this.isBorder = isBorder;
        this.accessible = !isBorder;  // 边界不可通行
        this.object = null;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public GameObject getObject() {
        return object;
    }

    public void setObject(GameObject object) {
        this.object = object;
    }

    /**
     * 克隆网格对象
     * @return 克隆的网格
     */
    @Override
    public Object clone() {
        Grid tmp = null;
        try{
            tmp = (Grid) super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return tmp;
    }
}

/**
 * 世界网格系统类
 * 将游戏世界划分为网格，用于A*寻路算法
 * 管理网格的可通行状态和对象占用情况
 */
public class WorldGrids {
    private List<Grid> grids;                    // 所有网格列表
    private World world;                         // 所属世界
    private List<Grid> unaccessibleGrids;        // 不可通行网格列表
    private int w, h;                            // 网格数量（宽、高）

    /**
     * 构造方法
     * 初始化网格系统，将世界划分为10x10像素的网格
     * @param world 游戏世界
     */
    public WorldGrids(World world) {
        this.world = world;
        w = world.getWidth() / Grid.LENGTH;   // 计算网格列数
        h = world.getHeight() / Grid.LENGTH;  // 计算网格行数
        this.grids = new ArrayList<>();
        this.unaccessibleGrids = new ArrayList<>();
        
        // 创建所有网格
        for(int i = 0; i < h; i++){
            for(int j = 0; j < w; j++){
                // 左上角区域设为边界（不可通行）
                if(j == 0 || j == 1 || i <= 5)
                    grids.add(new Grid(j, i, true));
                else
                    grids.add(new Grid(j, i, false));
            }
        }
    }

    /**
     * 更新网格状态
     * 根据当前游戏对象的位置更新网格的可通行状态
     * 每次寻路前调用，确保网格状态与游戏状态同步
     */
    public void updateGrids(){
        resetGrid();  // 先重置所有网格

        // 遍历所有游戏对象
        Iterator<GameObject> objIter = world.getObjectsIterator();
        while(objIter.hasNext()){
            GameObject object = objIter.next();
            // 跳过不可碰撞对象和边界
            if(!object.isCollidable() || object instanceof Border) continue;
            
            // 获取对象占用的所有网格
            Iterator<Grid> tmpIt = getGrid(object).iterator();
            while(tmpIt.hasNext()){
                Grid tmp = tmpIt.next();
                tmp.setObject(object);        // 标记网格上的对象
                tmp.setAccessible(false);     // 设为不可通行
                unaccessibleGrids.add(tmp);   // 加入不可通行列表
            }
        }
    }

    /**
     * 重置网格状态
     * 将所有不可通行网格恢复为可通行状态
     */
    public void resetGrid(){
        Iterator<Grid> unAcGridIter = getUnaccessibleGridsIterator();
        while(unAcGridIter.hasNext()){
            Grid tmp = unAcGridIter.next();
            tmp.setAccessible(true);   // 恢复可通行
            tmp.setObject(null);       // 清除对象引用
            unAcGridIter.remove();     // 从不可通行列表移除
        }
    }

    /**
     * 获取对象占用的所有网格
     * 根据对象的位置和半径计算其覆盖的网格
     * @param obj 游戏对象
     * @return 对象占用的网格列表
     */
    public List<Grid> getGrid(GameObject obj){
        List<Grid> grids = new ArrayList<>();
        int objX = obj.getX() - obj.getRadius();  // 对象左上角X
        int objY = obj.getY() - obj.getRadius();  // 对象左上角Y
        int length = obj.getRadius() * 2;         // 对象直径
        int t = length / Grid.LENGTH + 1;         // 需要的网格数
        
        // 遍历对象覆盖的网格区域
        for(int x = objX, i = 0; i < t && i < w; i++, x += Grid.LENGTH)
            for(int y = objY, j = 0; j < t && j < h; j++, y += Grid.LENGTH) {
                Grid tmp = getGrid(x+Grid.LENGTH, y+Grid.LENGTH);
                if(grids.indexOf(tmp) < 0)  // 避免重复添加
                    grids.add(tmp);
            }
        return grids;
    }

    /**
     * 根据像素坐标获取网格
     * @param x 像素X坐标
     * @param y 像素Y坐标
     * @return 对应的网格
     */
    public Grid getGrid(int x, int y){
        int gridX = x / Grid.LENGTH;  // 转换为网格坐标
        int gridY = y / Grid.LENGTH;
        return this.grids.get(gridY * w + gridX);  // 一维数组索引
    }

    /**
     * 根据网格坐标获取网格（带边界检查）
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 对应的网格
     */
    public Grid get(int gridX, int gridY){
        // 边界检查，防止越界
        if(gridX < 0) gridX = 0;
        if(gridY < 0) gridY = 0;
        if(gridX >= w) gridX = w-1;
        if(gridY >= h) gridY = h-1;
        return this.grids.get(gridY * w + gridX);
    }

    /**
     * 获取不可通行网格迭代器
     * @return 不可通行网格的迭代器
     */
    public Iterator<Grid> getUnaccessibleGridsIterator(){
        return unaccessibleGrids.iterator();
    }

    /**
     * 获取所有网格迭代器
     * @return 所有网格的迭代器
     */
    public Iterator<Grid> getGridsIterator(){
        return grids.iterator();
    }
}
