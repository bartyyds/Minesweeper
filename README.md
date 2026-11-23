# Minesweeper 扫雷（Java / Swing）

一个用 Java 写的扫雷小游戏，9×9 棋盘、10 颗雷，图形界面，单文件 259 行，无第三方依赖。

## 项目由来

初三时用手机自学过 Java，照着教程写过一个扫雷的雏形——没做完，后来换手机，代码也丢了。

2026 年 11 月，大一确立职业目标（市政给排水工程师，方向涉及智慧水务与 BIM 数据处理）后，我把这个欠了三年的作业重做了一遍。这次是完整的：能正常玩、能判胜负、能随时重开。

## 功能

- 9×9 棋盘，10 颗雷，格宽 42px
- **左键翻开，右键插旗**，状态栏实时显示剩余雷数
- **首击保护**：等玩家点下第一下才布雷，且第一下点的格子连同周围一圈都不放雷
- 翻开空白格时**递归展开**周围 8 格，和经典扫雷一致
- 周围雷数 1~8 用不同颜色区分
- 踩雷后显示全部雷的位置；翻完所有非雷格判定胜利，并自动给剩余雷插旗
- 顶部「重新开始」按钮，随时重开一局

## 实现要点

| 问题 | 做法 |
|---|---|
| 棋盘状态怎么存 | 三个 `boolean` 二维数组记状态：`hasMine`（是否布雷）、`opened`（是否已翻开）、`flagged`（是否插旗）；再加一个 `JButton[][]` 存格子状态 |
| 首击为什么不会踩雷 | `minesPlaced` 为 false 时先不布雷，等第一次 `openCell` 才调 `placeMines(row, col)`，并以该点为中心跳过 3×3 范围 |
| 空白格怎么连锁展开 | `openCell` 里若 `countMinesAround == 0`，对 8 邻域递归调用自身，靠 `opened` 数组防止重复 |
| 周围雷数怎么算 | `countMinesAround` 双循环遍历 8 邻域，用 `isInside` 做边界判断 |
| 胜利条件 | `openedCount == ROWS * COLS - MINE_COUNT` |
| 界面布局 | `JFrame` + `BorderLayout`，棋盘用 `GridLayout(9, 9)`，每个格子是一个 `JButton` |

想改难度，改文件开头这三个常量即可：

```java
private static final int ROWS = 9;
private static final int COLS = 9;
private static final int MINE_COUNT = 10;
```

## 运行

需要 JDK 8 或以上。项目只有一个源文件，无包名、无外部依赖。

**命令行：**

```bash
javac Minesweeper.java
java Minesweeper
```

**IntelliJ IDEA：** 打开项目，运行 `Minesweeper.main()`。

## 目录结构

```
Minesweeper/
├── src/
│   └── Minesweeper.java   # 全部逻辑：界面、布雷、翻格、胜负判定
├── .idea/                 # IDEA 项目配置，clone 下来可直接打开
├── Minesweeper.iml
└── .gitignore
```

## 后续可以做的

- 难度选择（初级 9×9/10 雷、中级 16×16/40 雷、高级 30×16/99 雷）
- 计时器与最快纪录
- 左键双击数字格快速展开（经典扫雷的 chord 操作）

## 作者

胡** · 天津**大学 ****专业 1 班（2026 级）

2026 年 11 月
