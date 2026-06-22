package main;

import model.Level;
import model.Player;
import service.FileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static main.CustomUI.*;

public class MainGUI {

    private static CardLayout cardLayout;
    private static JPanel mainPanel;

    private static JLabel infoLabel;
    private static JLabel hintLabel;
    private static JTextField inputField;

    private static JLabel lblTopScore;
    private static JLabel lblTopGold;
    private static JLabel lblCardLevel;

    private static int currentLevel = 1;
    private static int totalScore = 0;
    private static int gold = 0;
    private static int currentMin;
    private static int currentMax;
    private static int secretNumber;
    private static int guessesLeft;
    private static int itemsUsed;

    private static final String BACKGROUND_FILE = "background.jpg";
    private static final String TITLE_FILE = "title.png";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Game Đoán Số");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        ImageBackgroundPanel menuPanel = new ImageBackgroundPanel(BACKGROUND_FILE);
        menuPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(60, 0, 40, 0));
        try {
            BufferedImage img = ImageIO.read(new File(TITLE_FILE));
            BufferedImage transImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    int rgb = img.getRGB(x, y); Color c = new Color(rgb, true);
                    if (c.getRed() > 245 && c.getGreen() > 245 && c.getBlue() > 245) transImg.setRGB(x, y, 0x00FFFFFF);
                    else transImg.setRGB(x, y, rgb);
                }
            }
            int originalWidth = transImg.getWidth(); int originalHeight = transImg.getHeight();
            int targetWidth = 600; int targetHeight = (int) Math.round(((double) originalHeight / originalWidth) * targetWidth);

            Image scaledImage = transImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            titleLabel.setText("<html><div style='color: #FFD700; text-shadow: 5px 5px 0px #FF4500;'>ĐOÁN SỐ</div></html>");
            titleLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 100));
        }
        menuPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20); gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        RoundedButton btnPlay = new RoundedButton("CHƠI NGAY", 40, BTN_BLUE, Color.WHITE);
        RoundedButton btnRank = new RoundedButton("BẢNG XẾP HẠNG", 40, BTN_YELLOW, Color.WHITE);
        // Đã thêm nút Cài Đặt mới tinh
        RoundedButton btnSettings = new RoundedButton("CÀI ĐẶT", 40, BTN_GREEN, Color.WHITE);
        RoundedButton btnExit = new RoundedButton("THOÁT", 40, BTN_RED, Color.WHITE);

        buttonPanel.add(btnPlay, gbc);
        buttonPanel.add(btnRank, gbc);
        buttonPanel.add(btnSettings, gbc);
        buttonPanel.add(btnExit, gbc);
        menuPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel gamePanel = createGamePanel(frame);
        JPanel rankPanel = createRankPanel();

        mainPanel.add(menuPanel, "MainMenu");
        mainPanel.add(gamePanel, "Gameplay");
        mainPanel.add(rankPanel, "Leaderboard");

        btnPlay.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            currentLevel = 1; totalScore = 0; gold = 0;
            startNewLevel();
            cardLayout.show(mainPanel, "Gameplay");
        });

        btnRank.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            mainPanel.add(createRankPanel(), "Leaderboard");
            cardLayout.show(mainPanel, "Leaderboard");
        });

        btnSettings.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            showSettingsBox(frame);
        });

        btnExit.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            System.exit(0);
        });

        SoundManager.playBackgroundMusic("background.wav");

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // =========================================
    // BOX CÀI ĐẶT ÂM THANH
    // =========================================
    private static void showSettingsBox(JFrame parent) {
        JDialog dialog = new JDialog(parent, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel mainPanel = new RoundedPanel(40, CARD_WHITE, BTN_GREEN, 4);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("CÀI ĐẶT ÂM THANH", SwingConstants.CENTER);
        title.setFont(new Font(FONT_CUTE, Font.BOLD, 30));
        title.setForeground(BTN_GREEN);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        JLabel lblMusic = new JLabel("Nhạc nền: " + SoundManager.currentBGMVolume + "%", SwingConstants.CENTER);
        lblMusic.setFont(new Font(FONT_CUTE, Font.BOLD, 22));
        lblMusic.setForeground(TEXT_DARK);

        JSlider musicSlider = new JSlider(0, 100, SoundManager.currentBGMVolume);
        musicSlider.setOpaque(false);
        musicSlider.addChangeListener(e -> {
            int val = musicSlider.getValue();
            lblMusic.setText("Nhạc nền: " + val + "%");
            SoundManager.updateBGMVolume(val);
        });

        JLabel lblSFX = new JLabel("Hiệu ứng: " + SoundManager.currentSFXVolume + "%", SwingConstants.CENTER);
        lblSFX.setFont(new Font(FONT_CUTE, Font.BOLD, 22));
        lblSFX.setForeground(TEXT_DARK);

        JSlider sfxSlider = new JSlider(0, 100, SoundManager.currentSFXVolume);
        sfxSlider.setOpaque(false);
        sfxSlider.addChangeListener(e -> {
            int val = sfxSlider.getValue();
            lblSFX.setText("Hiệu ứng: " + val + "%");
            SoundManager.currentSFXVolume = val;
        });

        centerPanel.add(lblMusic);
        centerPanel.add(musicSlider);
        centerPanel.add(lblSFX);
        centerPanel.add(sfxSlider);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        RoundedButton btnOk = new RoundedButton("ĐÓNG", 30, BTN_GREEN, Color.WHITE);
        btnOk.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            dialog.dispose();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnOk);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void startNewLevel() {
        Level level = new Level(currentLevel);
        currentMin = level.getMinRange();
        currentMax = level.getMaxRange();
        guessesLeft = level.getMaxGuesses();
        itemsUsed = 0;

        Random random = new Random();
        secretNumber = random.nextInt((currentMax - currentMin) + 1) + currentMin;


        if (lblCardLevel != null) lblCardLevel.setText("Cấp độ: " + currentLevel);
        if (lblTopScore != null) lblTopScore.setText("ĐIỂM: " + totalScore);
        if (lblTopGold != null) lblTopGold.setText(" " + gold);

        if (infoLabel != null) {
            infoLabel.setText("Khoảng đoán: [ " + currentMin + "  -  " + currentMax + " ]");
            hintLabel.setText("Còn " + guessesLeft + " lượt đoán");
            inputField.setText("");
            inputField.requestFocus();
        }
    }

    private static JPanel createGamePanel(JFrame parentFrame) {
        ImageBackgroundPanel panel = new ImageBackgroundPanel(BACKGROUND_FILE);
        panel.setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        topBar.setOpaque(false); topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));

        RoundedPanel statsBadge = new RoundedPanel(45, SKIN_COLOR, WOOD_BROWN, 4);
        statsBadge.setLayout(new GridBagLayout()); statsBadge.setBorder(BorderFactory.createEmptyBorder(20, 35, 25, 35));
        GridBagConstraints badgeGbc = new GridBagConstraints(); badgeGbc.insets = new Insets(0, 10, 0, 10); badgeGbc.anchor = GridBagConstraints.CENTER;

        lblTopScore = new JLabel("ĐIỂM: 0"); lblTopScore.setFont(new Font(FONT_CUTE, Font.BOLD, 26)); lblTopScore.setForeground(WOOD_BROWN);
        JLabel separator = new JLabel(" | "); separator.setFont(new Font(FONT_CUTE, Font.BOLD, 26)); separator.setForeground(WOOD_BROWN);
        lblTopGold = new JLabel(" 0"); lblTopGold.setIcon(new CoinIcon(28)); lblTopGold.setFont(new Font(FONT_CUTE, Font.BOLD, 26)); lblTopGold.setForeground(WOOD_BROWN);

        statsBadge.add(lblTopScore, badgeGbc); statsBadge.add(separator, badgeGbc); statsBadge.add(lblTopGold, badgeGbc);
        topBar.add(statsBadge); panel.add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout()); centerWrapper.setOpaque(false);
        RoundedPanel cardPanel = new RoundedPanel(50, SKIN_COLOR, WOOD_BROWN, 6);
        cardPanel.setPreferredSize(new Dimension(800, 450)); cardPanel.setLayout(new BorderLayout()); cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel cardTop = new JPanel(new BorderLayout()); cardTop.setOpaque(false);
        JLabel instructLabel = new JLabel("TÌM SỐ BÍ MẬT", SwingConstants.CENTER); instructLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 45)); instructLabel.setForeground(WOOD_BROWN);
        lblCardLevel = new JLabel("Cấp độ: 1"); lblCardLevel.setFont(new Font(FONT_CUTE, Font.BOLD, 22)); lblCardLevel.setForeground(BTN_RED); lblCardLevel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        cardTop.add(instructLabel, BorderLayout.NORTH); cardTop.add(lblCardLevel, BorderLayout.WEST); cardPanel.add(cardTop, BorderLayout.NORTH);

        JPanel cardCenter = new JPanel(new GridBagLayout()); cardCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(15, 15, 15, 15); gbc.gridx = 0;

        infoLabel = new JLabel("Khoảng đoán: [ 10 - 100 ]"); infoLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 28)); infoLabel.setForeground(TEXT_DARK); gbc.gridy = 0; cardCenter.add(infoLabel, gbc);
        inputField = new JTextField(8); inputField.setFont(new Font(FONT_CUTE, Font.BOLD, 50)); inputField.setHorizontalAlignment(JTextField.CENTER); inputField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WOOD_BROWN, 4), BorderFactory.createEmptyBorder(10, 10, 10, 10))); gbc.gridy = 1; cardCenter.add(inputField, gbc);
        hintLabel = new JLabel("Còn 8 lượt đoán"); hintLabel.setFont(new Font(FONT_CUTE, Font.ITALIC, 24)); hintLabel.setForeground(BTN_RED); gbc.gridy = 2; cardCenter.add(hintLabel, gbc);

        cardPanel.add(cardCenter, BorderLayout.CENTER); centerWrapper.add(cardPanel); panel.add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 30)); bottomPanel.setOpaque(false);
        RoundedButton btnBack = new RoundedButton("THOÁT", 40, Color.GRAY, Color.WHITE); RoundedButton btnGuess = new RoundedButton("ĐOÁN NGAY", 40, BTN_GREEN, Color.WHITE); RoundedButton btnShop = new RoundedButton("CỬA HÀNG", 40, BTN_YELLOW, Color.WHITE);
        bottomPanel.add(btnBack); bottomPanel.add(btnGuess); bottomPanel.add(btnShop); panel.add(bottomPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            cardLayout.show(mainPanel, "MainMenu");
        });

        btnShop.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            if (itemsUsed >= 2) { CustomDialogs.showMessage(parentFrame, "CẢNH BÁO", "Bạn đã dùng hết lượt mua vật phẩm trong màn này!", CustomDialogs.TYPE_WARNING); inputField.requestFocus(); return; }
            int choice = CustomDialogs.showShop(parentFrame, gold, itemsUsed);

            if (choice == 0) {
                if (gold < 50) { CustomDialogs.showMessage(parentFrame, "LỖI", "Bạn không đủ Vàng!", CustomDialogs.TYPE_ERROR); }
                else { gold -= 50; itemsUsed++; lblTopGold.setText(" " + gold); SoundManager.playSound("levelup.wav"); String secretStr = String.valueOf(secretNumber); int randomCharIndex = new Random().nextInt(secretStr.length()); CustomDialogs.showMessage(parentFrame, "GỢI Ý QUÀ TẶNG", "Chữ số ở vị trí thứ " + (randomCharIndex + 1) + " là: " + secretStr.charAt(randomCharIndex), CustomDialogs.TYPE_INFO); }
            } else if (choice == 1) {
                if (gold < 30) { CustomDialogs.showMessage(parentFrame, "LỖI", "Bạn không đủ Vàng!", CustomDialogs.TYPE_ERROR); }
                else { gold -= 30; itemsUsed++; lblTopGold.setText(" " + gold); SoundManager.playSound("levelup.wav"); int midPoint = (currentMin + currentMax) / 2; if (secretNumber <= midPoint) currentMax = midPoint; else currentMin = midPoint + 1; infoLabel.setText("Khoảng đoán: [ " + currentMin + "  -  " + currentMax + " ]"); }
            } else if (choice == 2) {
                if (gold < 15) { CustomDialogs.showMessage(parentFrame, "LỖI", "Bạn không đủ Vàng!", CustomDialogs.TYPE_ERROR); }
                else { gold -= 15; itemsUsed++; guessesLeft++; lblTopGold.setText(" " + gold); SoundManager.playSound("levelup.wav"); hintLabel.setText("Còn " + guessesLeft + " lượt đoán"); }
            }
            inputField.requestFocus();
        });

        btnGuess.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            String input = inputField.getText().trim();
            if (input.isEmpty()) { hintLabel.setText("Nhập số đi kìa!"); return; }
            int guess;
            try { guess = Integer.parseInt(input); } catch (NumberFormatException ex) { hintLabel.setText("Chỉ nhập số thôi!"); inputField.setText(""); return; }

            if (guess < currentMin || guess > currentMax) { hintLabel.setText("Ngoài khoảng cho phép rồi!"); inputField.setText(""); return; }
            guessesLeft--;

            if (guess == secretNumber) {
                Level level = new Level(currentLevel);
                int levelScore = level.getBaseScore() + (guessesLeft * level.getMultiplier()); totalScore += levelScore;
                int baseGold = 10;
                int interest = (gold >= 50) ? 5 : (gold >= 40) ? 4 : (gold >= 30) ? 3 : (gold >= 20) ? 2 : (gold >= 10) ? 1 : 0;
                int totalGoldEarned = baseGold + interest; gold += totalGoldEarned;

                String successMsg = "Tuyệt vời!\nĐiểm: +" + levelScore + "\nVàng: +" + totalGoldEarned + "\nLợi tức: +" + interest;

                currentLevel++;
                if (currentLevel > 30) {
                    SoundManager.playSound("win_game.wav");
                    CustomDialogs.showMessage(parentFrame, "PHÁ ĐẢO THÀNH CÔNG!", "Tổng điểm của bạn: " + totalScore, CustomDialogs.TYPE_SUCCESS);
                    saveGameResult(parentFrame); cardLayout.show(mainPanel, "MainMenu");
                } else {
                    SoundManager.playSound("levelup.wav");
                    CustomDialogs.showMessage(parentFrame, "XUẤT SẮC!", successMsg, CustomDialogs.TYPE_SUCCESS);
                    startNewLevel();
                }
            } else {
                if (guessesLeft <= 0) {
                    SoundManager.playSound("lose.wav");
                    CustomDialogs.showMessage(parentFrame, "HẾT LƯỢT!", "Thất bại rồi.\nSố bí mật là: " + secretNumber, CustomDialogs.TYPE_ERROR);
                    saveGameResult(parentFrame); cardLayout.show(mainPanel, "MainMenu");
                }
                else {
                    if (guess < secretNumber) hintLabel.setText("Số bí mật LỚN HƠN " + guess + " (Còn " + guessesLeft + " lượt)");
                    else hintLabel.setText("Số bí mật NHỎ HƠN " + guess + " (Còn " + guessesLeft + " lượt)");
                    inputField.setText(""); inputField.requestFocus();
                }
            }
        });
        inputField.addActionListener(e -> btnGuess.doClick());
        return panel;
    }

    private static void saveGameResult(JFrame parentFrame) {
        String playerName = CustomDialogs.showInputDialog(parentFrame, "LƯU DANH CAO THỦ", "Nhập tên của bạn:");
        if (playerName == null || playerName.trim().isEmpty()) playerName = "NguoiChoiAnDanh";
        String playerId = "P" + System.currentTimeMillis();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        int levelsCompletedCount = (currentLevel > 30) ? 30 : (currentLevel - 1);
        FileManager fileManager = new FileManager(); fileManager.saveSingleMatch(new Player(playerId, playerName, totalScore, levelsCompletedCount, currentDate));
    }

    private static JPanel createRankPanel() {
        ImageBackgroundPanel panel = new ImageBackgroundPanel(BACKGROUND_FILE); panel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("BẢNG XẾP HẠNG", SwingConstants.CENTER); titleLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 55)); titleLabel.setForeground(Color.WHITE); titleLabel.setText("<html><div style='text-shadow: 3px 3px 0px #4682B4;'>BẢNG XẾP HẠNG</div></html>"); titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0)); panel.add(titleLabel, BorderLayout.NORTH);

        FileManager fileManager = new FileManager(); List<Player> players = fileManager.loadAllPlayers(); players.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));
        String[] columnNames = {"ID", "Tên Người Chơi", "Tổng Điểm", "Số Màn", "Ngày"}; Object[][] data = new Object[players.size()][5];
        for (int i = 0; i < players.size(); i++) { Player p = players.get(i); data[i][0] = p.getId(); data[i][1] = p.getName(); data[i][2] = p.getTotalScore(); data[i][3] = p.getLevelsCompleted(); data[i][4] = p.getPlayDate(); }

        JTable table = new JTable(new DefaultTableModel(data, columnNames)) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        table.setFont(new Font(FONT_CUTE, Font.BOLD, 18)); table.setForeground(WOOD_BROWN); table.setRowHeight(45); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0)); table.setOpaque(false); ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setOpaque(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); centerRenderer.setHorizontalAlignment(JLabel.CENTER); centerRenderer.setOpaque(false);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JTableHeader header = table.getTableHeader(); header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); label.setBackground(BTN_BLUE); label.setForeground(Color.WHITE); label.setFont(new Font(FONT_CUTE, Font.BOLD, 22)); label.setHorizontalAlignment(JLabel.CENTER); label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); return label;
            }
        });

        RoundedPanel tableWrapper = new RoundedPanel(40, SKIN_COLOR, WOOD_BROWN, 4); tableWrapper.setLayout(new BorderLayout()); tableWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 25, 20));
        JScrollPane scrollPane = new JScrollPane(table); scrollPane.getViewport().setOpaque(false); scrollPane.setOpaque(false); scrollPane.setBorder(BorderFactory.createEmptyBorder()); tableWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout()); centerPanel.setOpaque(false); centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 80, 20, 80)); centerPanel.add(tableWrapper, BorderLayout.CENTER); panel.add(centerPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(); bottomPanel.setOpaque(false); bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0)); RoundedButton btnBack = new RoundedButton("QUAY LẠI", 40, Color.GRAY, Color.WHITE); bottomPanel.add(btnBack); panel.add(bottomPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            cardLayout.show(mainPanel, "MainMenu");
        });
        return panel;
    }
}