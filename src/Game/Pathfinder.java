package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 寻路器类
 * 实现A*寻路算法，为敌人提供智能路径规划
 * 考虑障碍物和其他对象，计算最优路径
 */
public class Pathfinder{
    private List<PathNode> openNodes;      // 开放列表（待探索节点）
    private List<PathNode> closedNodes;    // 关闭列表（已探索节点）
    private List<PathNode> nodesToGoal;    // 到达目标的节点序列
    private List<Grid> pathToGoal;         // 到达目标的网格路径
    private WorldGrids worldGrids;         // 世界网格系统
    private int depth;                     // 当前搜索深度
    private GameObject object;             // 寻路对象
    private List<Grid> gridsOfObject;      // 对象占用的网格列表
    private int centre;                    // 中心网格索引

    /**
     * 构造方法
     * @param worldGrids 世界网格系统
     * @param object 寻路对象
     */
    public Pathfinder(WorldGrids worldGrids, GameObject object) {
        this.worldGrids = worldGrids;
        this.object = object;
        this.pathToGoal = new ArrayList<>();
        this.nodesToGoal = new ArrayList<>();
        this.gridsOfObject = worldGrids.getGrid(object);
        
        // 找到对象的中心网格
        Grid tmp = this.worldGrids.getGrid(object.getX(), object.getY());
        for(int i = 0; i < gridsOfObject.size(); i++){
            if(gridsOfObject.get(i).equals(tmp)){
                centre = i;
                break;
            }
        }
    }

    public List<PathNode> getOpenNodes() {
        return openNodes;
    }

    public void setOpenNodes(List<PathNode> openNodes) {
        this.openNodes = openNodes;
    }

    public List<PathNode> getClosedNodes() {
        return closedNodes;
    }

    public void setClosedNodes(List<PathNode> closedNodes) {
        this.closedNodes = closedNodes;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<PathNode> getNodesToGoal() {
        return nodesToGoal;
    }

    public void setNodesToGoal(List<PathNode> nodesToGoal) {
        this.nodesToGoal = nodesToGoal;
    }

    public List<Grid> getPathToGoal() {
        return pathToGoal;
    }

    public void setPathToGoal(List<Grid> pathToGoal) {
        this.pathToGoal = pathToGoal;
    }

    public WorldGrids getWorldGrids() {
        return worldGrids;
    }

    /**
     * 判断网格是否被对象占用
     * @param currentGrid 当前网格
     * @param otherGird 要检查的网格
     * @return 是否占用
     */
    public boolean ownGrid(Grid currentGrid, Grid otherGird){
        int deltaX = currentGrid.getGridX() - getCentreGrid().getGridX();
        int deltaY = currentGrid.getGridY() - getCentreGrid().getGridY();
        for(Grid grid : gridsOfObject){
            Grid tmp = worldGrids.get(grid.getGridX() + deltaX, grid.getGridY() + deltaY);
            if(tmp.equals(otherGird)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取可移动的相邻网格
     * 检查8个方向的网格是否可通行
     * @param grid 当前网格
     * @return 可移动的网格列表
     */
    public List<Grid> nextMoves(Grid grid){
        List<Grid> moves = new ArrayList<>();
        int[] x = {0, 0, -1, 1, -1, -1, 1, 1};  // 8个方向的X偏移
        int[] y = {-1, 1, 0, 0, -1, 1, 1, -1};  // 8个方向的Y偏移
        int deltaX = grid.getGridX() - getCentreGrid().getGridX();
        int deltaY = grid.getGridY() - getCentreGrid().getGridY();
        
        // 检查8个方向
        for(int i = 0; i < 8; i++){
            boolean flag = true;
            // 检查对象占用的所有网格是否都能移动到该方向
            for(int j = 0; j < gridsOfObject.size(); j++){
                int nextX = gridsOfObject.get(j).getGridX() + deltaX + x[i];
                int nextY = gridsOfObject.get(j).getGridY() + deltaY + y[i];
                Grid next = worldGrids.get(nextX, nextY);
                // 如果有任何一个网格不可通行，则该方向不可移动
                if(!ownGrid(grid, next) && !next.isAccessible()){
                    flag = false;
                    break;
                }
            }
            if(flag){
                moves.add(worldGrids.get(grid.getGridX() + x[i], grid.getGridY() + y[i]));
            }
        }
        return moves;
    }

    /**
     * 计算启发式值（曼哈顿距离）
     * @param currentPos 当前位置
     * @param goalPos 目标位置
     * @return 启发式值
     */
    public int getHeuristic(Grid currentPos, Grid goalPos){
        return (Math.abs(goalPos.getGridX() - currentPos.getGridX()) + Math.abs(goalPos.getGridY() - currentPos.getGridY())) * 10;
    }

    /**
     * 计算移动代价
     * 直线移动代价10，斜线移动代价14（约√2*10）
     * @param currentPos 当前位置
     * @param goalPos 目标位置
     * @return 移动代价
     */
    public int getCost(Grid currentPos, Grid goalPos){
        if(Math.abs(goalPos.getGridX() - currentPos.getGridX()) != 0 && Math.abs(goalPos.getGridY() - currentPos.getGridY()) != 0){
            return 14;  // 斜线移动
        } else {
            return 10;  // 直线移动
        }
    }

    /**
     * 计算两点之间的距离
     * @param x1 点1的X坐标
     * @param y1 点1的Y坐标
     * @param x2 点2的X坐标
     * @param y2 点2的Y坐标
     * @return 距离
     */
    public int getDistance(int x1, int y1, int x2, int y2){
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        return (int)Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    /**
     * 获取中心网格
     * @return 中心网格
     */
    public Grid getCentreGrid(){
        return this.gridsOfObject.get(centre);
    }

    /**
     * A*寻路算法主方法
     * 计算从当前位置到目标位置的最短路径
     * @param goalPos 目标位置
     * @return 路径网格列表
     */
    public List<Grid> shortestPath(Grid goalPos){
    	if(((Enemy)object).getTarget() == null) return null;  // 没有目标
        worldGrids.updateGrids();  // 更新网格状态

        Grid startPos = (Grid) getCentreGrid().clone();  // 起点

        openNodes = new ArrayList<>();
        closedNodes = new ArrayList<>();
        depth = 0;
        boolean hasGoal = false;

        // 将起点加入开放列表
        openNodes.add(new PathNode(startPos, null, 0, getHeuristic(startPos, goalPos), depth));

        // 主循环：当开放列表不为空时继续搜索
        while(openNodes.size() != 0){
            // 取出F值最小的节点（列表末尾）
            closedNodes.add(openNodes.get(openNodes.size() - 1));
            PathNode currentNode = closedNodes.get(closedNodes.size() - 1);
            Grid current = currentNode.getStateData();
            openNodes.remove(openNodes.size() - 1);

            // 检查是否到达目标
            int distance = getDistance(current.getX(), current.getY(), goalPos.getX(), goalPos.getY());
            int r = ((Enemy)object).getTarget().getRadius();
            if (distance < object.getRadius() + r + 10 || currentNode.getDepth() > 25) {
                hasGoal = true;  // 到达目标或超过最大深度
                break;
            }

            // 扩展相邻节点
            List<Grid> expanded = nextMoves(current);

            // 处理扩展的节点
            NodeLoop:
            for(int i = 0; (i < openNodes.size() || i < closedNodes.size()); i++){
                int s = expanded.size() - 1;
                while(s >= 0){
                    // 检查开放列表
                    if(i < openNodes.size()){
                        Grid OpenstateData = openNodes.get(i).getStateData();
                        if(OpenstateData.equals(expanded.get(s))){
                            // 如果找到更短的路径，更新节点
                            if((currentNode.getG() + getCost(current, OpenstateData)) < openNodes.get(i).getG()){
                                openNodes.get(i).setG(currentNode.getG() + getCost(current, OpenstateData));
                                openNodes.get(i).setH(getHeuristic(expanded.get(s), goalPos));
                                openNodes.get(i).setF(openNodes.get(i).getG() + openNodes.get(i).getH());
                                openNodes.get(i).setParentNode(currentNode);
                            }
                            expanded.remove(s);
                            if (expanded.isEmpty()) {
                                break NodeLoop;
                            }
                            s--;
                            continue ;
                        }
                    }
                    // 检查关闭列表
                    if(i < closedNodes.size()){
                        if (closedNodes.get(i).getStateData().equals(expanded.get(s))){
                            expanded.remove(s);  // 已探索过，跳过
                            if (expanded.isEmpty())
                            {
                                break NodeLoop;
                            }
                        }
                    }
                    s--;
                }
            }
            
            // 将新节点加入开放列表
            if (!expanded.isEmpty()) {
                for (int i = 0; i < expanded.size(); i++) {
                    openNodes.add(new PathNode(
                            expanded.get(i),
                            currentNode,
                            currentNode.getG() + getCost(current, expanded.get(i)),
                            getHeuristic(expanded.get(i), goalPos),
                            currentNode.getDepth() + 1));
                }
            }
            Collections.sort(openNodes);  // 按F值排序
        }
        
        // 回溯路径
        try {
            if (hasGoal) {
                int depth = closedNodes.get(closedNodes.size() - 1).getDepth();
                PathNode parent = closedNodes.get(closedNodes.size() - 1);

                // 从目标节点回溯到起点
                for (int s = 0; s <= depth; s++) {
                    nodesToGoal.add(parent);
                    pathToGoal.add(parent.getStateData());
                    parent = nodesToGoal.get(s).getParentNode();
                }
                Collections.reverse(pathToGoal);  // 反转路径（从起点到终点）
                return pathToGoal;
            }
            return null;
        } catch (NullPointerException e){
            if(pathToGoal != null) return pathToGoal;
            return null;
        }
    }
}
