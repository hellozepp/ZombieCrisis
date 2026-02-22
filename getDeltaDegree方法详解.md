# getDeltaDegree 方法详解

## 📍 方法位置
`Game.Enemy#getDeltaDegree(int anotherX, int anotherY)`

## 🎯 方法作用

计算从敌人当前位置指向目标位置的角度，用于：
1. **远程敌人（Ghost）瞄准**：判断是否对准玩家
2. **敌人朝向**：停止移动时保持面向玩家

## 📐 角度定义

游戏中的角度系统：
```
        90° (上)
         |
         |
180° ----+---- 0° (右)
(左)     |
         |
       270° (下)
```

- **0度**：正右方向
- **90度**：正上方向
- **180度**：正左方向
- **270度**：正下方向
- **逆时针递增**

## 🔍 代码分析

```java
public double getDeltaDegree(int anotherX, int anotherY){
    // 1. 计算坐标差
    int deltaX = anotherX - this.getX();
    int deltaY = - anotherY + this.getY();
    
    // 2. 计算距离
    double D = getDistance(anotherX, anotherY, this.getX(), this.getY());
    
    // 3. 计算三角函数值
    double sinA = deltaY / D;
    double cosA = deltaX / D;
    
    // 4. 反三角函数计算角度
    double angle1 = Math.toDegrees(Math.asin(sinA));
    double angle2 = Math.toDegrees(Math.acos(cosA));
    
    // 5. 角度调整
    double angle = angle2;
    if(angle1 < 0) angle = 360 - angle;
    
    return angle;
}
```

## 📊 步骤详解

### 步骤1：计算坐标差

```java
int deltaX = anotherX - this.getX();
int deltaY = - anotherY + this.getY();
```

**为什么Y要取反？**
- 屏幕坐标系：Y轴向下为正
- 数学坐标系：Y轴向上为正
- 取反后符合数学习惯

**示例**：
```
敌人位置：(100, 100)
目标位置：(200, 50)

deltaX = 200 - 100 = 100  (目标在右边)
deltaY = -(50) + 100 = 50  (目标在上方，取反后为正)
```

### 步骤2：计算距离

```java
double D = getDistance(anotherX, anotherY, this.getX(), this.getY());
```

使用勾股定理：
```
D = √(deltaX² + deltaY²)
D = √(100² + 50²) = √12500 ≈ 111.8
```

### 步骤3：计算三角函数值

```java
double sinA = deltaY / D;  // sin = 对边/斜边
double cosA = deltaX / D;  // cos = 邻边/斜边
```

**三角函数定义**：
```
        目标
         /|
      D / | deltaY (对边)
       /  |
      /   |
   敌人---+
    deltaX (邻边)

sin(角度) = 对边/斜边 = deltaY/D
cos(角度) = 邻边/斜边 = deltaX/D
```

**示例计算**：
```
sinA = 50 / 111.8 ≈ 0.447
cosA = 100 / 111.8 ≈ 0.894
```

### 步骤4：反三角函数计算角度

```java
double angle1 = Math.toDegrees(Math.asin(sinA));  // 通过sin计算
double angle2 = Math.toDegrees(Math.acos(cosA));  // 通过cos计算
```

**为什么要用两个反三角函数？**

因为它们的值域不同：
- `asin(x)` 返回：**-90° 到 90°**
- `acos(x)` 返回：**0° 到 180°**

**示例**：
```
angle1 = asin(0.447) ≈ 26.6°
angle2 = acos(0.894) ≈ 26.6°
```

### 步骤5：角度调整（关键！）

```java
double angle = angle2;
if(angle1 < 0) angle = 360 - angle;
```

**为什么需要调整？**

`acos` 只能返回 0-180度，无法区分上半圆和下半圆：

```
情况1：目标在上方（deltaY > 0）
  sin > 0, angle1 > 0
  → 使用 angle2（0-180度）

情况2：目标在下方（deltaY < 0）
  sin < 0, angle1 < 0
  → 使用 360 - angle2（180-360度）
```

## 🎨 图解示例

### 示例1：目标在右上方

```
敌人：(100, 100)
目标：(200, 50)

deltaX = 100 (正，在右边)
deltaY = 50  (正，在上方)

sinA = 0.447 > 0
cosA = 0.894 > 0

angle1 = 26.6° (正数)
angle2 = 26.6°

结果：26.6° (右上方向)
```

### 示例2：目标在右下方

```
敌人：(100, 100)
目标：(200, 150)

deltaX = 100  (正，在右边)
deltaY = -50  (负，在下方)

sinA = -0.447 < 0
cosA = 0.894 > 0

angle1 = -26.6° (负数！)
angle2 = 26.6°

调整：360 - 26.6 = 333.4°
结果：333.4° (右下方向)
```

### 示例3：目标在左上方

```
敌人：(100, 100)
目标：(50, 50)

deltaX = -50  (负，在左边)
deltaY = 50   (正，在上方)

sinA = 0.447 > 0
cosA = -0.447 < 0

angle1 = 26.6° (正数)
angle2 = 116.6° (cos为负，角度>90)

结果：116.6° (左上方向)
```

### 示例4：目标在左下方

```
敌人：(100, 100)
目标：(50, 150)

deltaX = -50  (负，在左边)
deltaY = -50  (负，在下方)

sinA = -0.447 < 0
cosA = -0.447 < 0

angle1 = -26.6° (负数！)
angle2 = 116.6°

调整：360 - 116.6 = 243.4°
结果：243.4° (左下方向)
```

## 📋 完整角度对照表

| 目标位置 | deltaX | deltaY | sinA | cosA | angle1 | angle2 | 最终角度 |
|---------|--------|--------|------|------|--------|--------|---------|
| 正右    | +      | 0      | 0    | +1   | 0°     | 0°     | 0°      |
| 右上    | +      | +      | +    | +    | +      | 小     | angle2  |
| 正上    | 0      | +      | +1   | 0    | 90°    | 90°    | 90°     |
| 左上    | -      | +      | +    | -    | +      | 大     | angle2  |
| 正左    | -      | 0      | 0    | -1   | 0°     | 180°   | 180°    |
| 左下    | -      | -      | -    | -    | -      | 大     | 360-angle2 |
| 正下    | 0      | -      | -1   | 0    | -90°   | 90°    | 270°    |
| 右下    | +      | -      | -    | +    | -      | 小     | 360-angle2 |

## 🎮 实际应用

### 应用1：Ghost（幽灵）瞄准判定

```java
// Ghost.java
double currentDegree = Direction.toDegree(this.dir);  // 当前朝向
double targetDegree = getDeltaDegree(target.getX(), target.getY());  // 目标角度

// 判断是否瞄准（角度差<60度）
if(Math.abs(targetDegree - currentDegree) < 60) {
    // 发射弹丸
}
```

**示例**：
```
幽灵朝向：45° (右上)
目标角度：60° (右上偏上)
角度差：|60-45| = 15° < 60°
→ 可以发射！
```

### 应用2：敌人停止时保持朝向

```java
// Enemy.java
if(this.getDir() == Direction.STOP) {
    // 虽然不移动，但要面向玩家
    this.oldDir = judgeAccurateDir(target.getX(), target.getY());
}
```

## 🔧 常见问题

### Q1：为什么不直接用 atan2？

**答**：`atan2(y, x)` 确实更简单，但这段代码可能是为了教学目的，展示完整的三角函数计算过程。

**简化版本**：
```java
public double getDeltaDegree(int anotherX, int anotherY){
    int deltaX = anotherX - this.getX();
    int deltaY = -(anotherY - this.getY());
    double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
    if(angle < 0) angle += 360;
    return angle;
}
```

### Q2：为什么要处理360度循环？

**答**：判断角度差时需要考虑循环：
```
情况1：350° 和 10°
  直接相减：|350-10| = 340° (错误！)
  实际差值：360-340 = 20° (正确)

情况2：10° 和 350°
  直接相减：|10-350| = 340° (错误！)
  实际差值：360-340 = 20° (正确)
```

**正确判断**：
```java
double diff = Math.abs(angle1 - angle2);
if(diff > 180) diff = 360 - diff;  // 取较小的角度差
```

### Q3：为什么Y坐标要取反？

**答**：
- **屏幕坐标**：原点在左上角，Y向下递增
- **数学坐标**：原点在左下角，Y向上递增
- 取反后符合数学习惯，三角函数计算更直观

## 📚 相关知识点

### 三角函数基础
- sin(θ) = 对边/斜边
- cos(θ) = 邻边/斜边
- tan(θ) = 对边/邻边

### 反三角函数
- asin(x)：返回 -90° 到 90°
- acos(x)：返回 0° 到 180°
- atan(x)：返回 -90° 到 90°
- atan2(y,x)：返回 -180° 到 180°

### 角度与弧度转换
- 弧度 = 角度 × π / 180
- 角度 = 弧度 × 180 / π
- Java中：`Math.toDegrees()` 和 `Math.toRadians()`

## 🎯 总结

`getDeltaDegree` 方法的核心思想：
1. **计算坐标差**：确定目标的相对位置
2. **三角函数**：通过 sin 和 cos 计算角度
3. **角度调整**：处理 0-360度的完整范围
4. **应用场景**：远程攻击瞄准、角色朝向

这个方法虽然看起来复杂，但本质上就是**计算两点之间的方向角度**，是游戏开发中非常常用的算法！

---

**相关方法**：
- `judgeAccurateDir()` - 将角度转换为8方向
- `Direction.toDegree()` - 将方向转换为角度
- `Weapon.Attack()` - 使用角度判断攻击范围

**学习建议**：
1. 先理解三角函数的基本概念
2. 画图理解坐标系转换
3. 用具体数值代入计算
4. 理解角度循环的处理方法
