package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CustomUI {

    // 👉 [VẤN ĐÁP]: Việc tạo hằng số bảng màu (Color Palette) tập trung ở đây có lợi ích gì?
    // Trả lời: Đây là tính Đóng gói (Encapsulation) và thiết kế Clean Code. Giúp quản lý toàn bộ giao diện tập trung.
    // Nếu muốn thay đổi chủ đề đồ họa của game, chỉ cần sửa mã màu tại đây, toàn bộ hệ thống sẽ tự động đồng bộ thay đổi theo.
    public static final Color CARD_WHITE = new Color(255, 255, 255, 240);
    public static final Color BTN_BLUE = new Color(70, 150, 255);
    public static final Color BTN_YELLOW = new Color(255, 175, 0);
    public static final Color BTN_GREEN = new Color(40, 180, 80);
    public static final Color BTN_RED = new Color(230, 80, 80);
    public static final Color TEXT_DARK = new Color(60, 60, 60);
    public static final Color SKIN_COLOR = new Color(255, 228, 196);
    public static final Color WOOD_BROWN = new Color(139, 69, 19);
    public static final String FONT_CUTE = "Segoe UI";

    // Lớp chứa các thành phần Hộp thoại, Thông báo đặc biệt nổi lên trên màn hình chính
    public static class CustomDialogs {
        public static final int TYPE_INFO = 1;
        public static final int TYPE_WARNING = 2;
        public static final int TYPE_ERROR = 3;
        public static final int TYPE_SUCCESS = 4;

        // 👉 [VẤN ĐÁP]: Đối tượng JDialog hoạt động như thế nào? Thuộc tính Modal là gì?
        // Trả lời: JDialog là một dạng cửa sổ con độc lập bật lên đè lên JFrame chính. Khi đặt thuộc tính Modal là 'true',
        // nó sẽ chặn toàn bộ tương tác ở cửa sổ phía dưới. Người chơi bắt buộc phải thao tác và đóng hộp thoại này thì mới chơi tiếp được.
        public static String showInputDialog(JFrame parent, String titleText, String msgText) {
            final String[] result = {null}; // Mảng 1 phần tử dùng để lưu giá trị trả về từ luồng sự kiện của nút bấm
            JDialog dialog = new JDialog(parent, true);
            dialog.setUndecorated(true); // Gỡ bỏ thanh tiêu đề mặc định vuông vức và thô kệch của hệ điều hành Windows
            dialog.setBackground(new Color(0, 0, 0, 0)); // Đặt nền trong suốt để có thể hiển thị các Panel bo tròn góc tùy chỉnh phía sau

            // Thiết lập cấu trúc Panel bo góc chứa nội dung thông báo nhập tên
            RoundedPanel mainPanel = new RoundedPanel(40, CARD_WHITE, BTN_BLUE, 4);
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JLabel title = new JLabel(titleText, SwingConstants.CENTER);
            title.setFont(new Font(FONT_CUTE, Font.BOLD, 30));
            title.setForeground(BTN_BLUE);
            mainPanel.add(title, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

            JLabel msgLabel = new JLabel(msgText, SwingConstants.CENTER);
            msgLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 22));
            msgLabel.setForeground(TEXT_DARK);
            centerPanel.add(msgLabel, BorderLayout.NORTH);

            // Ô nhập văn bản (Tên người chơi)
            JTextField inputField = new JTextField();
            inputField.setFont(new Font(FONT_CUTE, Font.BOLD, 30));
            inputField.setHorizontalAlignment(JTextField.CENTER);
            inputField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            centerPanel.add(inputField, BorderLayout.CENTER);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            RoundedButton btnOk = new RoundedButton("XÁC NHẬN", 30, BTN_GREEN, Color.WHITE);
            btnOk.addActionListener(e -> {
                SoundManager.playSound("click.wav"); // Phát hiệu ứng âm thanh click nút bấm
                result[0] = inputField.getText(); // Ghi nhận chuỗi ký tự người dùng đã nhập vào mảng kết quả
                dialog.dispose(); // Đóng và hủy hoàn toàn hộp thoại khỏi bộ nhớ tạm
            });
            inputField.addActionListener(e -> btnOk.doClick()); // Nếu gõ chữ xong ấn phím Enter thì tự động kích hoạt sự kiện click nút xác nhận

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setOpaque(false);
            bottomPanel.add(btnOk);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);

            dialog.add(mainPanel);
            dialog.pack(); // Tự động co giãn kích thước cửa sổ hộp thoại vừa vặn với các thành phần giao diện bên trong
            dialog.setLocationRelativeTo(parent); // Căn chỉnh cửa sổ hộp thoại xuất hiện chính xác ở tâm màn hình game
            dialog.setVisible(true); // Hiển thị hộp thoại lên màn hình
            return result[0]; // Trả kết quả tên người chơi về cho lớp MainGUI xử lý ghi file dữ liệu
        }

        // Tạo cấu hình hiển thị giao diện cho Popup Cửa hàng vật phẩm
        public static int showShop(JFrame parent, int currentGold, int itemsUsed) {
            final int[] result = {-1}; // Mặc định nếu người chơi không mua gì bấm tắt x sẽ trả về trị số -1
            JDialog dialog = new JDialog(parent, true);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));

            RoundedPanel mainPanel = new RoundedPanel(40, CARD_WHITE, WOOD_BROWN, 4);
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);

            JLabel title = new JLabel("CỬA HÀNG", SwingConstants.CENTER);
            title.setFont(new Font(FONT_CUTE, Font.BOLD, 32));
            title.setForeground(BTN_YELLOW);
            title.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
            headerPanel.add(title, BorderLayout.CENTER);

            // Nút "X" dùng để tắt cửa sổ cửa hàng
            RoundedButton btnClose = new RoundedButton("X", 20, BTN_RED, Color.WHITE);
            btnClose.setPreferredSize(new Dimension(40, 40));
            btnClose.setFont(new Font("Arial", Font.BOLD, 22));
            btnClose.addActionListener(e -> {
                SoundManager.playSound("click.wav");
                result[0] = -1; dialog.dispose();
            });
            headerPanel.add(btnClose, BorderLayout.EAST);
            mainPanel.add(headerPanel, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

            JLabel infoGold = new JLabel(" " + currentGold + "   |   Đã nhận: " + itemsUsed + "/2", SwingConstants.CENTER);
            infoGold.setIcon(new CoinIcon(28));
            infoGold.setFont(new Font(FONT_CUTE, Font.BOLD, 22));
            infoGold.setForeground(TEXT_DARK);
            infoGold.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            centerPanel.add(infoGold, BorderLayout.NORTH);

            // Tạo Layout dạng lưới nằm ngang gồm 1 hàng và 3 cột chứa 3 loại vật phẩm trợ giúp
            JPanel btnGrid = new JPanel(new GridLayout(1, 3, 15, 0));
            btnGrid.setOpaque(false);

            JPanel item1Panel = createShopItemPanel("item1.png", 50, 0, result, dialog);
            JPanel item2Panel = createShopItemPanel("item2.png", 30, 1, result, dialog);
            JPanel item3Panel = createShopItemPanel("item3.png", 15, 2, result, dialog);

            btnGrid.add(item1Panel); btnGrid.add(item2Panel); btnGrid.add(item3Panel);
            centerPanel.add(btnGrid, BorderLayout.CENTER);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            dialog.add(mainPanel); dialog.pack(); dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
            return result[0];
        }

        // Hàm dựng từng block vật phẩm bao gồm hình ảnh phía trên và nhãn giá vàng hiển thị phía dưới trong Shop
        private static JPanel createShopItemPanel(String imagePath, int price, int choiceIndex, int[] result, JDialog dialog) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);

            JButton btn = createImageButton(imagePath);
            btn.addActionListener(e -> {
                SoundManager.playSound("click.wav");
                result[0] = choiceIndex; // Ghi nhận chỉ số vật phẩm người chơi đã click chọn mua
                dialog.dispose();
            });
            panel.add(btn, BorderLayout.CENTER);

            JLabel priceLabel = new JLabel(" " + price, SwingConstants.CENTER);
            priceLabel.setIcon(new CoinIcon(24));
            priceLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 24));
            priceLabel.setForeground(WOOD_BROWN);
            priceLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            panel.add(priceLabel, BorderLayout.SOUTH);
            return panel;
        }

        // 👉 [VẤN ĐÁP]: Giải thích thuật toán xử lý xóa phông nền trắng của ảnh vật phẩm?
        // Trả lời: Em dùng lớp 'BufferedImage' để trích xuất dữ liệu ảnh. Chạy 2 vòng lặp lồng nhau quét qua từng tọa độ Pixel (x, y)
        // của bức ảnh. Lấy giá trị màu RGB, nếu pixel đó có màu tiệm cận màu trắng (R > 240, G > 240, B > 240) thì em thiết lập
        // kênh Alpha (độ trong suốt) của pixel đó về bằng 0 (0x00FFFFFF). Nhờ đó ảnh hòa vào nền game mà không có khung viền trắng bao quanh.
        private static JButton createImageButton(String imagePath) {
            JButton btn = new JButton();
            btn.setContentAreaFilled(false); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            try {
                BufferedImage img = ImageIO.read(new File(imagePath));
                BufferedImage transImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < img.getWidth(); x++) {
                    for (int y = 0; y < img.getHeight(); y++) {
                        int rgb = img.getRGB(x, y); Color c = new Color(rgb, true);
                        if (c.getRed() > 240 && c.getGreen() > 240 && c.getBlue() > 240) transImg.setRGB(x, y, 0x00FFFFFF);
                        else transImg.setRGB(x, y, rgb);
                    }
                }

                // Thuật toán co giãn ảnh tự động dựa trên chiều rộng đích 140 Pixel để tránh làm bóp méo tỷ lệ gốc của hình ảnh
                int originalWidth = transImg.getWidth(); int originalHeight = transImg.getHeight();
                int targetWidth = 140; int targetHeight = (int) Math.round(((double) originalHeight / originalWidth) * targetWidth);

                Image scaled = transImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                // Tạo ảnh có kích thước lớn hơn 5% phục vụ tạo hiệu ứng động (Animation) khi người chơi di trỏ chuột vào vật phẩm
                Image hoverImg = transImg.getScaledInstance((int)(targetWidth * 1.05), (int)(targetHeight * 1.05), Image.SCALE_SMOOTH);

                btn.setIcon(new ImageIcon(scaled));
                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setIcon(new ImageIcon(hoverImg)); } // Di chuột vào: Phóng to
                    public void mouseExited(java.awt.event.MouseEvent evt) { btn.setIcon(new ImageIcon(scaled)); }    // Rút chuột ra: Thu nhỏ về cũ
                });
            } catch (Exception e) { btn.setText("Lỗi Ảnh"); }
            return btn;
        }

        // Tạo cấu hình cho các loại hộp thoại thông báo kết quả (Thắng/Thua/Cảnh báo) trong game
        public static void showMessage(JFrame parent, String titleText, String msgText, int type) {
            JDialog dialog = new JDialog(parent, true); dialog.setUndecorated(true); dialog.setBackground(new Color(0, 0, 0, 0));
            Color titleColor = BTN_BLUE;
            // Tự động hoán đổi màu sắc chủ đề hộp thoại dựa vào loại thông điệp truyền lên hệ thống
            if (type == TYPE_WARNING) titleColor = BTN_YELLOW; else if (type == TYPE_ERROR) titleColor = BTN_RED; else if (type == TYPE_SUCCESS) titleColor = BTN_GREEN;

            RoundedPanel mainPanel = new RoundedPanel(40, CARD_WHITE, titleColor, 4);
            mainPanel.setLayout(new BorderLayout()); mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JLabel title = new JLabel(titleText, SwingConstants.CENTER);
            title.setFont(new Font(FONT_CUTE, Font.BOLD, 30)); title.setForeground(titleColor);
            mainPanel.add(title, BorderLayout.NORTH);

            // Sử dụng mã HTML để ép văn bản trong nhãn JLabel có thể tự động xuống dòng khi gặp ký tự '\n'
            JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>" + msgText.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
            msgLabel.setFont(new Font(FONT_CUTE, Font.BOLD, 22)); msgLabel.setForeground(TEXT_DARK); msgLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
            mainPanel.add(msgLabel, BorderLayout.CENTER);

            RoundedButton btnOk = new RoundedButton("ĐỒNG Ý", 30, titleColor, Color.WHITE);
            btnOk.addActionListener(e -> {
                SoundManager.playSound("click.wav");
                dialog.dispose();
            });
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); bottomPanel.setOpaque(false); bottomPanel.add(btnOk);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);

            dialog.add(mainPanel); dialog.pack(); dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
        }
    }

    // Thiết kế vẽ vector Icon Đồng xu vàng (sử dụng hoàn toàn bằng toán học thuật toán đồ họa, không dùng file ảnh)
    public static class CoinIcon implements Icon {
        private int size;
        public CoinIcon(int size) { this.size = size; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ vòng viền bóng ngoài màu cam đậm
            g2.setColor(new Color(230, 150, 0)); g2.fillOval(x, y, size, size);
            // Vẽ lõi trong màu vàng tươi tạo khối chiều sâu cho đồng tiền
            g2.setColor(new Color(255, 215, 0)); g2.fillOval(x + 2, y + 2, size - 4, size - 4);

            // Vẽ hình ngôi sao 5 cánh trang trí ở chính tâm đồng xu bằng mảng tọa độ toán học đa giác Polygon
            g2.setColor(new Color(230, 150, 0));
            int cx = x + size / 2, cy = y + size / 2, outerRadius = (int) (size * 0.35), innerRadius = (int) (size * 0.15);
            int[] xPoints = new int[10], yPoints = new int[10];
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI / 5 * i - Math.PI / 2;
                int r = (i % 2 == 0) ? outerRadius : innerRadius;
                xPoints[i] = cx + (int) (Math.cos(angle) * r); yPoints[i] = cy + (int) (Math.sin(angle) * r);
            }
            g2.fillPolygon(xPoints, yPoints, 10); g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    // Lớp xây dựng tấm Panel nền hỗ trợ hiển thị ảnh nền background trải phẳng toàn màn hình
    public static class ImageBackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        public ImageBackgroundPanel(String imagePath) {
            try { backgroundImage = ImageIO.read(new File(imagePath)); } catch (Exception e) {}
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            else { g.setColor(new Color(135, 206, 235)); g.fillRect(0, 0, getWidth(), getHeight()); }
        }
    }

    // 👉 [VẤN ĐÁP]: Giải thích nguyên lý viết code tạo nút bấm bo góc (RoundedButton)?
    // Trả lời: Em sử dụng cơ chế Kế thừa (Inheritance). Tạo lớp con 'RoundedButton' kế thừa từ lớp 'JButton' của Java Swing.
    // Em tiến hành ghi đè (Override) lại phương thức vẽ 'paintComponent(Graphics g)' gốc, dùng cọ vẽ nâng cao 'Graphics2D'
    // bật chế độ khử răng cưa và ra lệnh vẽ khối bo góc bằng phương thức 'g2.fillRoundRect()'.
    public static class RoundedButton extends JButton {
        private int cornerRadius; private Color bgColor;
        public RoundedButton(String text, int radius, Color bgColor, Color fgColor) {
            super(text); this.cornerRadius = radius; this.bgColor = bgColor;
            setFont(new Font(FONT_CUTE, Font.BOLD, 26)); setForeground(fgColor); setPreferredSize(new Dimension(300, 65));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);

            // Lắng nghe di biến chuột để tăng độ sáng màu nền (brighter) tạo hiệu ứng phản hồi trực quan khi người chơi rê chuột vào nút
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { RoundedButton.this.bgColor = bgColor.brighter(); repaint(); }
                public void mouseExited(java.awt.event.MouseEvent evt) { RoundedButton.this.bgColor = bgColor; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Khử răng cưa nét vẽ cong

            // Vẽ hiệu ứng đổ bóng mờ (Shadow effect) lệch xuống dưới 5 Pixel để tạo hiệu ứng 3D nổi cho nút bấm
            g2.setColor(new Color(0, 0, 0, 50)); g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 5, cornerRadius, cornerRadius);
            // Vẽ khối thân nút chính
            g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, cornerRadius, cornerRadius);

            // Đổ màu Gradient dốc từ trắng mờ sang trong suốt ở nửa trên nút bấm tạo hiệu ứng mặt kính phản quang (Glossy Effect)
            GradientPaint gloss = new GradientPaint(0, 0, new Color(255, 255, 255, 100), 0, getHeight() / 2, new Color(255, 255, 255, 0));
            g2.setPaint(gloss); g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() / 2, cornerRadius, cornerRadius);

            // Thuật toán toán học đo đạc kích thước chuỗi văn bản để vẽ chữ nằm chính xác giữa tâm hình học của nút bấm
            FontMetrics metrics = g2.getFontMetrics(getFont()); Icon icon = getIcon(); int iconSpacing = (icon != null) ? 12 : 0;
            int totalWidth = metrics.stringWidth(getText()) + (icon != null ? icon.getIconWidth() + iconSpacing : 0);
            int startX = (getWidth() - totalWidth) / 2, textY = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent() - 2;
            if (icon != null) { int iconY = (getHeight() - icon.getIconHeight()) / 2 - 2; icon.paintIcon(this, g2, startX, iconY); startX += icon.getIconWidth() + iconSpacing; }
            g2.setColor(getForeground()); g2.setFont(getFont()); g2.drawString(getText(), startX, textY); g2.dispose();
        }
    }

    // Cấu trúc Panel bo tròn viền nâng cao làm thẻ hiển thị thông tin nền trong suốt trong game
    public static class RoundedPanel extends JPanel {
        private int cornerRadius; private Color bgColor; private Color borderColor; private int borderThickness;
        public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderThickness) {
            super(); this.cornerRadius = radius; this.bgColor = bgColor; this.borderColor = borderColor; this.borderThickness = borderThickness; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 40)); g2.fillRoundRect(8, 10, getWidth() - 16, getHeight() - 12, cornerRadius, cornerRadius);
            if (borderColor != null && borderThickness > 0) { g2.setColor(borderColor); g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, cornerRadius, cornerRadius); }
            g2.setColor(bgColor); g2.fillRoundRect(borderThickness, borderThickness, getWidth() - 10 - (borderThickness * 2), getHeight() - 10 - (borderThickness * 2), cornerRadius - borderThickness, cornerRadius - borderThickness);
            GradientPaint gloss = new GradientPaint(0, 0, new Color(255, 255, 255, 120), 0, getHeight() / 3, new Color(255, 255, 255, 0));
            g2.setPaint(gloss); g2.fillRoundRect(borderThickness, borderThickness, getWidth() - 10 - (borderThickness * 2), getHeight() / 3, cornerRadius - borderThickness, cornerRadius - borderThickness);
            g2.dispose();
        }
    }
}