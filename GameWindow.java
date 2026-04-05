import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class GameWindow extends JFrame {

    private final Color BG_MAIN = new Color(36, 0, 70);
    private final Color CELL_COLOR = new Color(60, 9, 108);
    private final Color ACCENT_GREEN = new Color(56, 142, 60);
    private final Color BTN_NEW_GAME = new Color(240, 240, 240);

    private final Color PLAYER_X_BG = new Color(107, 203, 255);
    private final Color PLAYER_O_BG = new Color(227, 194, 50);
    private final Color DRAW_BG = new Color(209, 217, 240);

    private final Color X_MARK_COLOR = new Color(107, 203, 255);
    private final Color O_MARK_COLOR = new Color(227, 194, 50);

    private GameButton[] buttons = new GameButton[9];
    private JTextField ipField;
    private JTextField portField;
    private JLabel statusLabel;
    private JButton mainActionButton;
    private JPanel connectionPanel;

    private JLabel scoreXLabel, scoreOLabel, scoreDrawLabel;
    private int scoreX = 0, scoreO = 0, scoreDraw = 0;

    private NetworkConnection network;
    private boolean myTurn = false;
    private String myMark;
    private String opponentMark;
    private boolean isConnected = false;

    public GameWindow() {
        super("Tic-Tac-Toe Dark");
        setSize(420, 750); // Трохи витягнуте вікно як на мобільному
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        connectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectionPanel.setOpaque(false);

        ipField = createDarkInput("127.0.0.1");
        portField = createDarkInput("6666");

        JLabel ipLbl = new JLabel("IP:"); ipLbl.setForeground(Color.WHITE);
        JLabel portLbl = new JLabel("Port:"); portLbl.setForeground(Color.WHITE);

        connectionPanel.add(ipLbl);
        connectionPanel.add(ipField);
        connectionPanel.add(portLbl);
        connectionPanel.add(portField);
        topPanel.add(connectionPanel);
        topPanel.add(Box.createVerticalStrut(15));

        JPanel scorePanel = new JPanel(new GridLayout(1, 3, 15, 0)); // 15px відступ між квадратами
        scorePanel.setOpaque(false);
        scorePanel.setPreferredSize(new Dimension(380, 80));

        scoreXLabel = createScoreBox(scorePanel, "PLAYER X", PLAYER_X_BG);
        scoreDrawLabel = createScoreBox(scorePanel, "DRAW", DRAW_BG);
        scoreOLabel = createScoreBox(scorePanel, "PLAYER O", PLAYER_O_BG);

        topPanel.add(scorePanel);
        add(topPanel, BorderLayout.NORTH);

        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 15, 15)); // 15px проміжки
        boardPanel.setOpaque(false);
        boardPanel.setPreferredSize(new Dimension(320, 320));

        for (int i = 0; i < 9; i++) {
            buttons[i] = new GameButton("");
            final int index = i;
            buttons[i].addActionListener(e -> makeMove(index));
            buttons[i].setEnabled(false);
            boardPanel.add(buttons[i]);
        }
        boardWrapper.add(boardPanel);
        add(boardWrapper, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 20, 30, 20));

        // -- Зелена плашка статусу --
        statusLabel = new JLabel("Connect to start", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(255, 255, 255, 30)); // Напівпрозора спочатку
        statusLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        mainActionButton = new JButton("Start Server (Create Game)");
        styleMainButton(mainActionButton);

        mainActionButton.addActionListener(e -> handleMainButtonAction());

        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createVerticalStrut(20));
        bottomPanel.add(mainActionButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleMainButtonAction() {
        if (!isConnected) {

            Object[] options = {"Create Server (X)", "Join Game (O)"};
            int n = JOptionPane.showOptionDialog(this,
                    "Choose mode:",
                    "Network Setup",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (n == 0) startServer();
            else if (n == 1) connectToServer();

        } else {
            resetBoardVisuals();
            statusLabel.setText("Wait for opponent...");
            statusLabel.setBackground(new Color(255, 255, 255, 30));
        }
    }

    private JLabel createScoreBox(JPanel parent, String title, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setBorder(new EmptyBorder(5,0,0,0));

        JLabel scoreLbl = new JLabel("0", SwingConstants.CENTER);
        scoreLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        scoreLbl.setBorder(new EmptyBorder(0,0,5,0));

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(scoreLbl, BorderLayout.CENTER);

        JPanel roundedWrapper = new RoundedPanel(15, bg);
        roundedWrapper.setLayout(new BorderLayout());
        roundedWrapper.add(titleLbl, BorderLayout.NORTH);
        roundedWrapper.add(scoreLbl, BorderLayout.CENTER);

        parent.add(roundedWrapper);
        return scoreLbl;
    }

    private JTextField createDarkInput(String text) {
        JTextField field = new JTextField(text, 10);
        field.setBackground(new Color(60, 9, 108));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return field;
    }

    private void styleMainButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(BTN_NEW_GAME);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BTN_NEW_GAME, 0),
                BorderFactory.createEmptyBorder(15, 0, 15, 0)));
    }

    private void startServer() {
        new Thread(() -> {
            try {
                network = new NetworkConnection(this);
                int port = Integer.parseInt(portField.getText());

                SwingUtilities.invokeLater(() -> statusLabel.setText("Waiting for player..."));
                network.startServer(port);

                myMark = "X";
                opponentMark = "O";
                myTurn = true;
                isConnected = true;

                SwingUtilities.invokeLater(() -> {
                    onConnectUI();
                    statusLabel.setText("Your Turn (X)");
                });
                new Thread(network).start();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                network = new NetworkConnection(this);
                String ip = ipField.getText();
                int port = Integer.parseInt(portField.getText());
                network.connect(ip, port);

                myMark = "O";
                opponentMark = "X";
                myTurn = false;
                isConnected = true;

                SwingUtilities.invokeLater(() -> {
                    onConnectUI();
                    statusLabel.setText("Waiting for X...");
                });
                new Thread(network).start();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void onConnectUI() {
        connectionPanel.setVisible(false);
        mainActionButton.setText("Game in progress");
        mainActionButton.setEnabled(false);
        enableBoard(myTurn);
    }

    private void makeMove(int index) {
        if (myTurn) {
            buttons[index].setMark(myMark);
            buttons[index].setEnabled(false);
            myTurn = false;
            statusLabel.setText("Opponent's turn...");
            enableBoard(false);

            if (checkWin(myMark)) {
                handleWin(true);
                network.sendMove(index);
                return;
            }
            network.sendMove(index);
        }
    }

    public void receiveOpponentMove(int index) {
        buttons[index].setMark(opponentMark);
        buttons[index].setEnabled(false);

        if (checkWin(opponentMark)) {
            handleWin(false);
        } else {
            myTurn = true;
            statusLabel.setText("Your Turn (" + myMark + ")");
            enableBoard(true);
        }
    }

    private void handleWin(boolean iWon) {
        disableBoard();
        mainActionButton.setText("New Game");
        mainActionButton.setEnabled(true);

        statusLabel.setOpaque(true);
        statusLabel.setBackground(ACCENT_GREEN); // Зелений банер як на фото

        if (iWon) {
            statusLabel.setText("Game over. You Win!");
            if (myMark.equals("X")) scoreX++; else scoreO++;
        } else {
            statusLabel.setText("Game over. Opponent Wins!");
            if (opponentMark.equals("X")) scoreX++; else scoreO++;
        }
        updateScoreLabels();
    }

    private void updateScoreLabels() {
        scoreXLabel.setText(String.valueOf(scoreX));
        scoreOLabel.setText(String.valueOf(scoreO));
    }

    private void resetBoardVisuals() {
        for (GameButton btn : buttons) {
            btn.setText("");
            btn.setEnabled(false); // Чекаємо ходу
        }
    }

    private boolean checkWin(String mark) {
        int[][] wins = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] w : wins) {
            if (buttons[w[0]].getText().equals(mark) &&
                    buttons[w[1]].getText().equals(mark) &&
                    buttons[w[2]].getText().equals(mark)) {
                return true;
            }
        }
        return false;
    }

    private void enableBoard(boolean enable) {
        for (GameButton btn : buttons) {
            if (btn.getText().isEmpty()) btn.setEnabled(enable);
        }
    }

    private void disableBoard() {
        for (GameButton btn : buttons) btn.setEnabled(false);
    }

    class GameButton extends JButton {
        private String mark = "";

        public GameButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 60));
        }

        public void setMark(String m) {
            this.mark = m;
            setText(m);
            if (m.equals("X")) setForeground(X_MARK_COLOR);
            else setForeground(O_MARK_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(CELL_COLOR);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            super.paintComponent(g);
            g2.dispose();
        }
    }
}
