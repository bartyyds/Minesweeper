import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

// 扫雷。左键翻开格子，右键插旗，把没有雷的格子全翻完就赢。
public class Minesweeper extends JFrame {

    // 想改难度改这三行
    private static final int ROWS = 9;
    private static final int COLS = 9;
    private static final int MINE_COUNT = 10;
    private static final int CELL_SIZE = 42;

    private static final Color COVERED_COLOR = new Color(198, 198, 198);
    private static final Color OPENED_COLOR = new Color(232, 232, 232);
    private static final Color MINE_COLOR = new Color(255, 130, 130);
    private static final Color FLAG_COLOR = new Color(255, 240, 160);

    // 周围雷数 1~8 各自显示的颜色，下标 0 用不上
    private static final Color[] NUMBER_COLORS = {
            null,
            new Color(0, 0, 200),
            new Color(0, 130, 0),
            new Color(200, 0, 0),
            new Color(0, 0, 130),
            new Color(130, 0, 0),
            new Color(0, 130, 130),
            new Color(0, 0, 0),
            new Color(100, 100, 100)
    };

    private final boolean[][] hasMine = new boolean[ROWS][COLS];
    private final boolean[][] opened = new boolean[ROWS][COLS];
    private final boolean[][] flagged = new boolean[ROWS][COLS];
    private final JButton[][] cells = new JButton[ROWS][COLS];

    private int openedCount = 0;
    private int flagCount = 0;
    private boolean gameOver = false;
    private boolean minesPlaced = false;

    private final Random random = new Random();
    private JLabel statusLabel;

    public static void main(String[] args) {
        // Swing 的界面得交给事件线程去建，照写就行
        SwingUtilities.invokeLater(() -> new Minesweeper().setVisible(true));
    }

    public Minesweeper() {
        initUI();
        resetGame();
    }

    private void initUI() {
        setTitle("扫雷");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton restart = new JButton("重新开始");
        restart.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        restart.setFocusPainted(false);
        restart.addActionListener(e -> resetGame());

        JPanel top = new JPanel(new BorderLayout());
        top.add(statusLabel);
        top.add(restart, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel board = new JPanel(new GridLayout(ROWS, COLS));
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                b.setFont(new Font("微软雅黑", Font.BOLD, 18));
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);

                // 循环转完 row/col 就变成 9 了，先存一份给监听器用
                final int r = row;
                final int c = col;
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) return;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleFlag(r, c);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            openCell(r, c);
                        }
                    }
                });

                cells[row][col] = b;
                board.add(b);
            }
        }
        add(board, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void resetGame() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                hasMine[row][col] = false;
                opened[row][col] = false;
                flagged[row][col] = false;
                paintCovered(row, col);
            }
        }
        openedCount = 0;
        flagCount = 0;
        gameOver = false;
        minesPlaced = false;
        updateStatus();
    }

    // 等玩家点下第一下再布雷，而且第一下点的位置连同周围一圈都不放雷
    private void placeMines(int safeRow, int safeCol) {
        int placed = 0;
        while (placed < MINE_COUNT) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);
            if (hasMine[row][col]) continue;
            if (Math.abs(row - safeRow) <= 1 && Math.abs(col - safeCol) <= 1) continue;

            hasMine[row][col] = true;
            placed++;
        }
        minesPlaced = true;
    }

    // 格子上的数字，就是这里数出来的
    private int countMinesAround(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (isInside(row + dr, col + dc) && hasMine[row + dr][col + dc]) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isInside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    private void openCell(int row, int col) {
        if (gameOver || !isInside(row, col)) return;
        if (opened[row][col] || flagged[row][col]) return;

        if (!minesPlaced) placeMines(row, col);

        if (hasMine[row][col]) {
            gameOver = true;
            opened[row][col] = true;
            cells[row][col].setText("●");
            cells[row][col].setBackground(MINE_COLOR);
            cells[row][col].setForeground(Color.RED);
            showAllMines();
            statusLabel.setText("踩到雷了，点“重新开始”再来一局");
            return;
        }

        opened[row][col] = true;
        openedCount++;

        JButton button = cells[row][col];
        button.setBackground(OPENED_COLOR);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        int count = countMinesAround(row, col);
        if (count > 0) {
            button.setText(String.valueOf(count));
            button.setForeground(NUMBER_COLORS[count]);
        } else {
            // 周围一颗雷都没有，顺手把周围 8 格也翻开，就这么一直递归下去
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr != 0 || dc != 0) openCell(row + dr, col + dc);
                }
            }
        }

        checkWin();
    }

    private void toggleFlag(int row, int col) {
        if (gameOver || opened[row][col]) return;

        if (flagged[row][col]) {
            flagged[row][col] = false;
            flagCount--;
            paintCovered(row, col);
        } else {
            flagged[row][col] = true;
            flagCount++;
            cells[row][col].setText("旗");
            cells[row][col].setForeground(Color.RED);
            cells[row][col].setBackground(FLAG_COLOR);
        }
        updateStatus();
    }

    private void checkWin() {
        if (gameOver) return;
        if (openedCount < ROWS * COLS - MINE_COUNT) return;

        gameOver = true;
        // 赢了把剩下的雷插上旗，界面上看着整齐点
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (hasMine[row][col] && !flagged[row][col]) {
                    flagged[row][col] = true;
                    cells[row][col].setText("旗");
                    cells[row][col].setForeground(Color.RED);
                    cells[row][col].setBackground(FLAG_COLOR);
                }
            }
        }
        statusLabel.setText("赢了，点“重新开始”再来一局");
    }

    private void paintCovered(int row, int col) {
        JButton button = cells[row][col];
        button.setText("");
        button.setBackground(COVERED_COLOR);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    private void showAllMines() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (hasMine[row][col] && !flagged[row][col]) {
                    cells[row][col].setText("●");
                    cells[row][col].setForeground(Color.BLACK);
                    cells[row][col].setBackground(OPENED_COLOR);
                }
            }
        }
    }

    private void updateStatus() {
        statusLabel.setText("剩余雷数: " + (MINE_COUNT - flagCount) + "　左键翻开 / 右键插旗");
    }
}
