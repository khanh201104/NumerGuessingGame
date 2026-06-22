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

// Nhập toàn bộ tài nguyên đồ họa tĩnh và các lớp con từ CustomUI để sử dụng ngắn gọn trực tiếp
import static main.CustomUI.*;

public class MainGUI {

    // 👉 [VẤN ĐÁP]: Giải thích cơ chế điều hướng chuyển đổi màn hình trong dự án của nhóm?
    // Trả lời: Nhóm chúng em sử dụng cơ chế quản lý layout 'CardLayout'. Toàn bộ các giao diện thành phần
    // (Menu chính, Màn chơi game, Bảng xếp hạng) được tổ chức thành các Panel độc lập đóng vai trò như các quân bài xếp chồng lên nhau.
    // Khi người chơi ấn nút chuyển đổi, CardLayout sẽ lật quân bài tương ứng hiển thị lên cửa sổ duy nhất mà không cần tạo mới JFrame, tiết kiệm tài nguyên RAM.
    private static CardLayout cardLayout;
    private static JPanel mainPanel;

    // Khai báo các đối tượng nhãn hiển thị thông tin động trên màn hình game
    private static JLabel infoLabel;
    private static JLabel hintLabel;
    private static JTextField inputField;

    private static JLabel lblTopScore;
    private static JLabel lblTopGold;
    private static JLabel lblCardLevel;

    // Khai báo tập hợp các biến nguyên thủy lưu trữ trạng thái hiện hành của ván đấu (Game State)
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

    // Hàm MAIN - Điểm xuất phát khởi chạy kích hoạt chương trình ứng dụng Java
    public static void main(String[] args) {
        // Ép luồng đồ họa Java Swing khởi chạy an toàn trong hàng đợi sự kiện giúp tránh xung đột lag luồng hình ảnh
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Game Đoán Số");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Kết thúc tắt tiến trình ngầm hoàn toàn khi bấm dấu "X" góc cửa sổ
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Ép ứng dụng hiển thị phóng to toàn màn hình máy tính ngay khi bật

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        ImageBackgroundPanel menuPanel = new ImageBackgroundPanel(BACKGROUND_FILE);
        menuPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(60, 0, 40, 0));
        try {
            // Nạp và quét ảnh tiêu đề để loại bỏ phông viền trắng mặc định
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

        // Khởi tạo khu vực bảng nút bấm Menu chính bằng GridBagLayout giúp dễ dàn hàng căn giữa cân đối các linh kiện
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20); gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Khởi tạo các đối tượng nút bấm tùy chỉnh từ thư viện CustomUI
        RoundedButton btnPlay = new RoundedButton("CHƠI NGAY", 40, BTN_BLUE, Color.WHITE);
        RoundedButton btnRank = new RoundedButton("BẢNG XẾP HẠNG", 40, BTN_YELLOW, Color.WHITE);
        RoundedButton btnSettings = new RoundedButton("CÀI ĐẶT", 40, BTN_GREEN, Color.WHITE);
        RoundedButton btnExit = new RoundedButton("THOÁT", 40, BTN_RED, Color.WHITE);

        buttonPanel.add(btnPlay, gbc);
        buttonPanel.add(btnRank, gbc);
        buttonPanel.add(btnSettings, gbc);
        buttonPanel.add(btnExit, gbc);
        menuPanel.add(buttonPanel, BorderLayout.CENTER);

        // Khởi dựng sẵn bố cục 2 màn hình phụ nạp vào cây Layout chính
        JPanel gamePanel = createGamePanel(frame);
        JPanel rankPanel = createRankPanel();

        mainPanel.add(menuPanel, "MainMenu");
        mainPanel.add(gamePanel, "Gameplay");
        mainPanel.add(rankPanel, "Leaderboard");

        // Đăng ký lắng nghe hành động click chuột xử lý luồng sự kiện cho các nút bấm
        btnPlay.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            currentLevel = 1; totalScore = 0; gold = 0; // Khởi tạo mới lại từ đầu các thông số ván đấu
            startNewLevel(); // Thiết lập thông số độ khó màn 1
            cardLayout.show(mainPanel, "Gameplay"); // Lật thẻ màn chơi game lên màn hình chính
        });

        btnRank.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            mainPanel.add(createRankPanel(), "Leaderboard"); // Tải nạp mới lại dữ liệu từ tệp tin lưu trữ để cập nhật danh sách mới nhất
            cardLayout.show(mainPanel, "Leaderboard");
        });

        btnSettings.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            showSettingsBox(frame); // Gọi bật mở Popup tùy chỉnh cài đặt âm thanh
        });

        btnExit.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            System.exit(0); // Đóng chấm dứt hoàn toàn luồng phần mềm hệ thống
        });

        // Kích hoạt phát nhạc nền bất tận ngay khi ứng dụng được nạp mở thành công
        SoundManager.playBackgroundMusic("background.wav");

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Phương thức sinh cấu trúc Panel Giao diện cho cửa sổ cấu hình Cài Đặt Âm Thanh
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

        // Khởi tạo thanh kéo trượt nằm ngang JSlider thu nhận dải giá trị từ 0 đến 100
        JSlider musicSlider = new JSlider(0, 100, SoundManager.currentBGMVolume);
        musicSlider.setOpaque(false);
        // Đăng ký bắt sự kiện thời gian thực khi người chơi di chuyển thanh kéo trượt
        musicSlider.addChangeListener(e -> {
            int val = musicSlider.getValue(); // Trích xuất giá trị số hiện hành trên thanh trượt
            lblMusic.setText("Nhạc nền: " + val + "%");
            SoundManager.updateBGMVolume(val); // Truyền giá trị sang điều khiển âm thanh nhạc nền
        });

        JLabel lblSFX = new JLabel("Hiệu ứng: " + SoundManager.currentSFXVolume + "%", SwingConstants.CENTER);
        lblSFX.setFont(new Font(FONT_CUTE, Font.BOLD, 22));
        lblSFX.setForeground(TEXT_DARK);

        JSlider sfxSlider = new JSlider(0, 100, SoundManager.currentSFXVolume);
        sfxSlider.setOpaque(false);
        sfxSlider.addChangeListener(e -> {
            int val = sfxSlider.getValue();
            lblSFX.setText("Hiệu ứng: " + val + "%");
            SoundManager.currentSFXVolume = val; // Lưu trữ giá trị phần trăm hiệu ứng âm thanh vào bộ nhớ tĩnh
        });

        centerPanel.add(lblMusic); centerPanel.add(musicSlider);
        centerPanel.add(lblSFX); centerPanel.add(sfxSlider);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        RoundedButton btnOk = new RoundedButton("ĐÓNG", 30, BTN_GREEN, Color.WHITE);
        btnOk.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            dialog.dispose();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false); bottomPanel.add(btnOk);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel); dialog.pack(); dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
    }

    // Thực hiện thiết lập tham số nghiệp vụ kỹ thuật khi người chơi bước sang một cấp độ màn mới
    private static void startNewLevel() {
        // 👉 [VẤN ĐÁP]: Đối tượng Level được cấu trúc ra sao?
        // Trả lời: Đây là tư duy thiết kế cấu trúc hướng đối tượng (OOP). Lớp 'Level' nhận tham số màn hiện hành để tính toán
        // và đóng gói tự động các dải khoảng đoán (min, max) và số lượt đoán cho phép tối đa tương ứng với độ khó của màn đó.
        Level level = new Level(currentLevel);
        currentMin = level.getMinRange();
        currentMax = level.getMaxRange();
        guessesLeft = level.getMaxGuesses();
        itemsUsed = 0; // Đặt lại số lượt đã mua đạo cụ cứu trợ đầu màn về bằng 0

        // Thuật toán toán học sinh số ngẫu nhiên bí mật nằm trọn vẹn trong dải khoảng [currentMin, currentMax]
        Random random = new Random();
        secretNumber = random.nextInt((currentMax - currentMin) + 1) + currentMin;

        // Dòng lệnh Log in kết quả đáp án ra Console phục vụ Dev thực hiện kiểm thử nhanh (Hack test)
        System.out.println("====== [TEST] Số bí mật màn " + currentLevel + " là: " + secretNumber + " ======");

        // Đẩy đồng bộ chuỗi văn bản thông số mới lên các nhãn hiển thị giao diện
        if (lblCardLevel != null) lblCardLevel.setText("Cấp độ: " + currentLevel);
        if (lblTopScore != null) lblTopScore.setText("ĐIỂM: " + totalScore);
        if (lblTopGold != null) lblTopGold.setText(" " + gold);

        if (infoLabel != null) {
            infoLabel.setText("Khoảng đoán: [ " + currentMin + "  -  " + currentMax + " ]");
            hintLabel.setText("Còn " + guessesLeft + " lượt đoán");
            inputField.setText("");
            inputField.requestFocus(); // Tự động hướng tiêu điểm con nháy chuột sẵn vào ô nhập số
        }
    }

    // Phương thức thiết lập vẽ giao diện đồ họa khu vực màn chơi game chính
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

        // Xử lý logic nghiệp vụ mua sắm đạo cụ cứu trợ tại Cửa hàng
        btnShop.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            if (itemsUsed >= 2) { CustomDialogs.showMessage(parentFrame, "CẢNH BÁO", "Bạn đã dùng hết lượt mua vật phẩm trong màn này!", CustomDialogs.TYPE_WARNING); inputField.requestFocus(); return; }

            // Bật mở giao diện Cửa hàng và hứng mã lựa chọn vật phẩm từ người chơi trả về
            int choice = CustomDialogs.showShop(parentFrame, gold, itemsUsed);

            if (choice == 0) { // Đạo cụ 1: Xem gợi ý chữ số ngẫu nhiên trong số đích
                if (gold < 50) { CustomDialogs.showMessage(parentFrame, "LỖI", "Bạn không đủ Vàng!", CustomDialogs.TYPE_ERROR); }
                else {
                    gold -= 50; itemsUsed++; lblTopGold.setText(" " + gold);
                    SoundManager.playSound("levelup.wav"); // Phát âm báo mua đồ thành công
                    String secretStr = String.valueOf(secretNumber);
                    int randomCharIndex = new Random().nextInt(secretStr.length());
                    CustomDialogs.showMessage(parentFrame, "GỢI Ý QUÀ TẶNG", "Chữ số ở vị trí thứ " + (randomCharIndex + 1) + " là: " + secretStr.charAt(randomCharIndex), CustomDialogs.TYPE_INFO);
                }
            } else if (choice == 1) { // Đạo cụ 2: Cắt đôi khoảng giới hạn tìm kiếm nhị phân
                if (gold < 30) { CustomDialogs.showMessage(parentFrame, "LỖI", "Bạn không đủ Vàng!", CustomDialogs.TYPE_ERROR); }
                else {
                    gold -= 30; itemsUsed++; lblTopGold.setText(" " + gold);
                    SoundManager.playSound("levelup.wav");
                    int midPoint = (currentMin + currentMax) / 2;
                    if (secretNumber <= midPoint) currentMax = midPoint; else currentMin = midPoint + 1;
                    infoLabel.setText("Khoảng đoán: [ " + currentMin + "  -  " + currentMax + " ]");
                }
            } else if (choice == 2) { // Đạo cụ 3: Mua thêm lượt đoán số bổ sung
                if (gold < 15) { CustomDialogs.showMessage(parentFrame, "LỖI", "Bạn không đủ Vàng!", CustomDialogs.TYPE_ERROR); }
                else {
                    gold -= 15; itemsUsed++; guessesLeft++; lblTopGold.setText(" " + gold);
                    SoundManager.playSound("levelup.wav");
                    hintLabel.setText("Còn " + guessesLeft + " lượt đoán");
                }
            }
            inputField.requestFocus();
        });

        // Xử lý logic nghiệp vụ so sánh kiểm tra kết quả khi người chơi thực hiện dự đoán số
        btnGuess.addActionListener(e -> {
            SoundManager.playSound("click.wav");
            String input = inputField.getText().trim();
            if (input.equalsIgnoreCase("S")) { inputField.setText(""); btnShop.doClick(); return; } // Hỗ trợ nhấn phím tắt nhanh 'S' mở nhanh cửa hàng
            if (input.isEmpty()) { hintLabel.setText("Nhập số đi kìa!"); return; }

            int guess;
            // 👉 [VẤN ĐÁP]: Khối Try-Catch tại đây đóng vai trò gì cho hệ thống?
            // Trả lời: Đây là kỹ thuật xử lý ngoại lệ đầu vào (Input Validation Exception Handling).
            // Nếu người dùng cố tình nhập văn bản chữ ('abc') thay vì số, hàm 'Integer.parseInt' sẽ ném ra ngoại lệ 'NumberFormatException'.
            // Khối 'catch' sẽ bắt giữ lỗi này, in dòng chữ cảnh báo mà không làm crash (treo văng) ứng dụng.
            try { guess = Integer.parseInt(input); }
            catch (NumberFormatException ex) { hintLabel.setText("Chỉ nhập số thôi!"); inputField.setText(""); return; }

            if (guess < currentMin || guess > currentMax) { hintLabel.setText("Ngoài khoảng cho phép rồi!"); inputField.setText(""); return; }

            guessesLeft--; // Khấu trừ đi một lượt đoán số của màn chơi

            if (guess == secretNumber) {
                // =========================================
                // TRƯỜNG HỢP: ĐOÁN CHÍNH XÁC SỐ BÍ MẬT
                // =========================================
                Level level = new Level(currentLevel);
                // Thuật toán tính điểm thưởng tuyến tính: Điểm màn gốc + (Số lượt còn dư x Hệ số nhân màn đó)
                int levelScore = level.getBaseScore() + (guessesLeft * level.getMultiplier());
                totalScore += levelScore;

                int baseGold = 10; // Lượng vàng cơ bản nhận được mỗi khi thắng màn
                // Kỹ thuật tính lợi tức kinh tế (Interest Gold): Tích lũy tối đa cộng 5 vàng nếu số dư lớn hơn hoặc bằng 50 vàng
                int interest = (gold >= 50) ? 5 : (gold >= 40) ? 4 : (gold >= 30) ? 3 : (gold >= 20) ? 2 : (gold >= 10) ? 1 : 0;
                int totalGoldEarned = baseGold + interest;
                gold += totalGoldEarned;

                String successMsg = "Tuyệt vời!\nĐiểm: +" + levelScore + "\nVàng: +" + totalGoldEarned + "\nLợi tức: +" + interest;

                currentLevel++; // Nâng cấp độ màn chơi
                if (currentLevel > 30) {
                    // Nếu vượt qua ngưỡng 30 màn, kích hoạt phá đảo trò chơi thành công
                    SoundManager.playSound("win_game.wav");
                    CustomDialogs.showMessage(parentFrame, "PHÁ ĐẢO THÀNH CÔNG!", "Tổng điểm của bạn: " + totalScore, CustomDialogs.TYPE_SUCCESS);
                    saveGameResult(parentFrame); // Lưu bản ghi điểm số người chơi vào file csv
                    cardLayout.show(mainPanel, "MainMenu"); // Đẩy điều hướng lật quay về màn hình Menu chính
                } else {
                    // Chuyển tiếp sang màn chơi kế tiếp
                    SoundManager.playSound("levelup.wav");
                    CustomDialogs.showMessage(parentFrame, "XUẤT SẮC!", successMsg, CustomDialogs.TYPE_SUCCESS);
                    startNewLevel(); // Tái khởi tạo thiết lập thông số số ngẫu nhiên mới cho màn chơi tiếp theo
                }
            } else {
                // =========================================
                // TRƯỜNG HỢP: ĐOÁN SAI SỐ BÍ MẬT
                // =========================================
                if (guessesLeft <= 0) {
                    // Thua cuộc hoàn toàn ván đấu vì đã cạn kiệt số lượt đoán cho phép
                    SoundManager.playSound("lose.wav");
                    CustomDialogs.showMessage(parentFrame, "HẾT LƯỢT!", "Thất bại rồi.\nSố bí mật là: " + secretNumber, CustomDialogs.TYPE_ERROR);
                    saveGameResult(parentFrame);
                    cardLayout.show(mainPanel, "MainMenu");
                }
                else {
                    // Thuật toán thu hẹp khoanh vùng chỉ hướng tìm kiếm nhị phân (Binary Search Feedback Loop)
                    if (guess < secretNumber) hintLabel.setText("Số bí mật LỚN HƠN " + guess + " (Còn " + guessesLeft + " lượt)");
                    else hintLabel.setText("Số bí mật NHỎ HƠN " + guess + " (Còn " + guessesLeft + " lượt)");
                    inputField.setText(""); inputField.requestFocus();
                }
            }
        });
        inputField.addActionListener(e -> btnGuess.doClick()); // Bắt sự kiện gõ số xong nhấn phím Enter trên bàn phím kích hoạt nút đoán ngay
        return panel;
    }

    // Xử lý gọi liên kết lớp FileManager trong kiến trúc hệ thống để lưu kết quả trận đấu xuống ổ đĩa cứng
    private static void saveGameResult(JFrame parentFrame) {
        String playerName = CustomDialogs.showInputDialog(parentFrame, "LƯU DANH CAO THỦ", "Nhập tên của bạn:");
        if (playerName == null || playerName.trim().isEmpty()) playerName = "NguoiChoiAnDanh"; // Phòng thủ nếu người chơi để trống tên

        // Tạo chuỗi mã định danh ID duy nhất dựa theo mốc thời gian mili giây của hệ thống để chống trùng khóa chính dữ liệu
        String playerId = "P" + System.currentTimeMillis();
        // Định dạng ngày tháng năm hiện hành theo chuẩn Việt Nam bằng thư viện thời gian Java 8 tiên tiến
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        int levelsCompletedCount = (currentLevel > 30) ? 30 : (currentLevel - 1);

        FileManager fileManager = new FileManager();
        fileManager.saveSingleMatch(new Player(playerId, playerName, totalScore, levelsCompletedCount, currentDate));
    }

    // Khởi tạo Panel màn hình kết xuất dữ liệu hiển thị Bảng Xếp Hạng Cao Thủ trong game bằng cấu trúc bảng JTable
    private static JPanel createRankPanel() {
        ImageBackgroundPanel panel = new ImageBackgroundPanel(BACKGROUND_FILE); panel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("BẢNG XẾP HẠNG", SwingConstants.CENTER); titleLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 55)); titleLabel.setForeground(Color.WHITE); titleLabel.setText("<html><div style='text-shadow: 3px 3px 0px #4682B4;'>BẢNG XẾP HẠNG</div></html>"); titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0)); panel.add(titleLabel, BorderLayout.NORTH);

        FileManager fileManager = new FileManager();
        List<Player> players = fileManager.loadAllPlayers(); // Nạp đọc toàn bộ tệp tin danh sách thực thể người chơi từ ổ cứng lên bộ nhớ List

        // 👉 [VẤN ĐÁP]: Nhóm sử dụng thuật toán nào để sắp xếp thứ hạng điểm số trong bảng?
        // Trả lời: Nhóm chúng em áp dụng biểu thức Lambda (Lambda Expression) kết hợp API Stream được hỗ trợ từ Java 8 trở lên.
        // Em sử dụng hàm so sánh 'Integer.compare(p2, p1)' hoán đổi vị trí của đối tượng thứ 2 lên trước đối tượng thứ 1 nếu điểm cao hơn,
        // giúp sắp xếp danh sách mượt mà theo thứ tự Điểm số Giảm dần.
        players.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        // Khởi tạo ma trận mảng 2 chiều đổ dữ liệu thực thể vào trong bảng cấu trúc
        String[] columnNames = {"ID", "Tên Người Chơi", "Tổng Điểm", "Số Màn", "Ngày"}; Object[][] data = new Object[players.size()][5];
        for (int i = 0; i < players.size(); i++) { Player p = players.get(i); data[i][0] = p.getId(); data[i][1] = p.getName(); data[i][2] = p.getTotalScore(); data[i][3] = p.getLevelsCompleted(); data[i][4] = p.getPlayDate(); }

        // Ghi đè phương thức 'isCellEditable' ép giá trị về bằng 'false' chặn không cho người dùng click sửa trực tiếp điểm trên JTable
        JTable table = new JTable(new DefaultTableModel(data, columnNames)) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        table.setFont(new Font(FONT_CUTE, Font.BOLD, 18)); table.setForeground(WOOD_BROWN); table.setRowHeight(45); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0)); table.setOpaque(false); ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setOpaque(false);

        // Duyệt toàn bộ các cột trong JTable để căn lề văn bản nằm ở chính giữa ô hiển thị (Center alignment)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); centerRenderer.setHorizontalAlignment(JLabel.CENTER); centerRenderer.setOpaque(false);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        // Tùy biến thanh Header tiêu đề đầu bảng của JTable cho đồng bộ màu sắc Blue theme
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
            cardLayout.show(mainPanel, "MainMenu"); // Lật thẻ điều hướng quay trở lại màn hình Menu chính của game
        });
        return panel;
    }
}