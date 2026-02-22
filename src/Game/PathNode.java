package Game;

/**
 * 寻路节点类
 * 用于A*寻路算法的节点表示
 * 实现Comparable接口支持按F值排序
 */
public class PathNode implements Comparable<PathNode>{
    private Grid stateData;      // 节点对应的网格
    private PathNode parentNode; // 父节点（用于回溯路径）
    private int g, h, f, depth;  // g:起点到当前点代价, h:当前点到终点估计, f:总代价, depth:深度

    /**
     * 获取父节点
     * @return 父节点
     */
    public PathNode getParentNode() {
        return parentNode;
    }

    /**
     * 设置父节点
     * @param parentNode 父节点
     */
    public void setParentNode(PathNode parentNode) {
        this.parentNode = parentNode;
    }

    /**
     * 获取G值（起点到当前点的实际代价）
     * @return G值
     */
    public int getG() {
        return g;
    }

    /**
     * 设置G值
     * @param g G值
     */
    public void setG(int g) {
        this.g = g;
    }

    /**
     * 获取H值（当前点到终点的启发式估计）
     * @return H值
     */
    public int getH() {
        return h;
    }

    /**
     * 设置H值
     * @param h H值
     */
    public void setH(int h) {
        this.h = h;
    }

    /**
     * 获取F值（总代价 = G + H）
     * @return F值
     */
    public int getF() {
        return f;
    }

    /**
     * 设置F值
     * @param f F值
     */
    public void setF(int f) {
        this.f = f;
    }

    /**
     * 获取深度
     * @return 深度
     */
    public int getDepth() {
        return depth;
    }

    /**
     * 设置深度
     * @param depth 深度
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * 获取节点数据（网格）
     * @return 网格对象
     */
    public Grid getStateData() {
        return stateData;
    }

    /**
     * 设置节点数据
     * @param stateData 网格对象
     */
    public void setStateData(Grid stateData) {
        this.stateData = stateData;
    }

    /**
     * 构造方法
     * @param stateData 网格数据
     * @param parentNode 父节点
     * @param g G值（起点到当前点代价）
     * @param h H值（当前点到终点估计）
     * @param depth 深度
     */
    public PathNode(Grid stateData, PathNode parentNode, int g, int h, int depth) {
        this.stateData = stateData;
        this.parentNode = parentNode;
        this.g = g;
        this.h = h;
        this.f = this.g + this.h;  // F = G + H
        this.depth = depth;
    }

    /**
     * 比较方法
     * 用于节点排序，F值小的优先（F值相同时深度小的优先）
     * @param other 另一个节点
     * @return 比较结果
     */
    @Override
    public int compareTo(PathNode other)
    {
        int NodeComp = (this.f - other.getF()) * -1;  // F值小的排在后面（降序）
        if (NodeComp == 0)  // F值相同
        {
            NodeComp = (this.depth - other.getDepth());  // 深度小的排在前面（升序）
        }
        return NodeComp;
    }
}
