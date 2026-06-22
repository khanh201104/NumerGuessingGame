package service;

import model.Level;
import java.util.Random;
import java.util.Scanner;
import model.Player;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GameEngine {
    private Scanner scanner;
    private Random random;

    // Constructor khởi tạo các công cụ cần thiết
    public GameEngine() {
        this.scanner = new Scanner(System.in);
        this.random = new Random();
    }

    // Hàm chạy chiến dịch 30 màn chơi (Tích hợp Cửa hàng & Hệ thống Eco Gold)
    public void startCampaign() {
        int currentLevelNumber = 1;
        int totalScore = 0; // Điểm số dùng để xếp hạng
        int gold = 0;       // Tiền tệ dùng để mua vật phẩm
        boolean isGameOver = false;

        System.out.println("\n--- BẮT ĐẦU CHUỖI THỬ THÁCH 30 MÀN ---");

        // Vòng lặp lớn: Chạy cho đến khi thua hoặc hoàn thành màn 30
        while (!isGameOver && currentLevelNumber <= 30) {
            // Tải cấu hình màn chơi hiện tại
            Level level = new Level(currentLevelNumber);

            int currentMin = level.getMinRange();
            int currentMax = level.getMaxRange();
            int secretNumber = random.nextInt((currentMax - currentMin) + 1) + currentMin;
            int guessesLeft = level.getMaxGuesses();
            boolean isLevelCleared = false;
            int itemsUsed = 0; // Đếm số vật phẩm đã dùng trong màn (tối đa 2)

            System.out.println("\n==================================");
            System.out.println(">>> MÀN " + currentLevelNumber + " <<<");
            System.out.println("Đoán số từ " + currentMin + " đến " + currentMax + ".");
            System.out.println("==================================");

            System.out.println("\n--- BẮT ĐẦU ĐOÁN ---");

            // ==========================================
            // VÒNG LẶP ĐOÁN SỐ & MỞ CỬA HÀNG (Bấm 'S')
            // ==========================================
            while (guessesLeft > 0) {
                // Nhắc nhở khoảng đoán hiện tại
                System.out.println("\n[Khoảng đoán: " + currentMin + " - " + currentMax + "]");
                System.out.print("Nhập số bạn đoán (hoặc gõ 'S' mở Cửa Hàng) - Còn " + guessesLeft + " lượt: ");
                String input = scanner.nextLine().trim();

                // 1. NẾU CHỌN MỞ CỬA HÀNG
                if (input.equalsIgnoreCase("S")) {
                    if (itemsUsed >= 2) {
                        System.out.println("⚠ Cảnh báo: Bạn đã dùng tối đa 2 vật phẩm cho màn này!");
                        continue;
                    }

                    System.out.println("\n[CỬA HÀNG VẬT PHẨM] - Tiền của bạn: " + gold + " Gold - Đã dùng: " + itemsUsed + "/2");
                    System.out.println("1. Lộ 1 chữ số ngẫu nhiên (Giá: 50 Gold)");
                    System.out.println("2. Thu hẹp 50% khoảng cách (Giá: 30 Gold)");
                    System.out.println("3. Thêm 1 lượt đoán (Giá: 15 Gold)");
                    System.out.println("0. Đóng Cửa Hàng");
                    System.out.print("Chọn vật phẩm (0-3): ");

                    String shopChoice = scanner.nextLine().trim();
                    if (shopChoice.equals("0")) {
                        System.out.println("-> Đã đóng Cửa Hàng.");
                        continue;
                    }

                    int price = 0;
                    switch (shopChoice) {
                        case "1": price = 50; break;
                        case "2": price = 30; break;
                        case "3": price = 15; break;
                        default:
                            System.out.println(" Lựa chọn không hợp lệ!");
                            continue;
                    }

                    if (gold < price) {
                        System.out.println(" Cảnh báo: Bạn không đủ Vàng để mua vật phẩm này!");
                        continue;
                    }

                    // Trừ tiền và ghi nhận sử dụng
                    gold -= price;
                    itemsUsed++;
                    System.out.println(" Mua thành công! Số dư hiện tại: " + gold + " Gold.");

                    // Áp dụng hiệu ứng vật phẩm
                    switch (shopChoice) {
                        case "1":
                            String secretStr = String.valueOf(secretNumber);
                            int randomCharIndex = random.nextInt(secretStr.length());
                            System.out.println("Chữ số ở vị trí thứ " + (randomCharIndex + 1) + " là số [" + secretStr.charAt(randomCharIndex) + "]");
                            break;
                        case "2":
                            int midPoint = (currentMin + currentMax) / 2;
                            if (secretNumber <= midPoint) {
                                currentMax = midPoint;
                            } else {
                                currentMin = midPoint + 1;
                            }
                            System.out.println("Khoảng cách đã được thu hẹp lại từ " + currentMin + " đến " + currentMax);
                            break;
                        case "3":
                            guessesLeft++;
                            System.out.println("Số lượt đoán của bạn đã tăng lên thành " + guessesLeft);
                            break;
                    }
                    continue; // Mua xong quay lại đầu vòng lặp để tiếp tục
                }

                // 2. NẾU NHẬP SỐ ĐỂ ĐOÁN
                int guess;
                try {
                    guess = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println(" Lỗi: Vui lòng nhập số nguyên hoặc chữ 'S' để mở Cửa Hàng!");
                    continue;
                }

                if (guess < currentMin || guess > currentMax) {
                    System.out.println(" Cảnh báo: Số đoán phải nằm trong khoảng " + currentMin + " - " + currentMax + "!");
                    continue;
                }

                guessesLeft--;

                if (guess == secretNumber) {
                    isLevelCleared = true;
                    break;
                } else if (guess < secretNumber) {
                    System.out.println("-> Số bí mật LỚN HƠN " + guess);
                } else {
                    System.out.println("-> Số bí mật NHỎ HƠN " + guess);
                }
            }

            // ==========================================
            // 3. XỬ LÝ PHẦN THƯỞNG KHI QUA MÀN
            // ==========================================
            if (isLevelCleared) {
                // Tính điểm xếp hạng
                int levelScore = level.getBaseScore() + (guessesLeft * level.getMultiplier());
                totalScore += levelScore;

                // Hệ thống Eco: Tiền cơ bản + Lợi tức
                int baseGold = 10;
                int interest = 0;

                if (gold >= 30) interest = 3;
                else if (gold >= 20) interest = 2;
                else if (gold >= 10) interest = 1;

                int totalGoldEarned = baseGold + interest;
                gold += totalGoldEarned; // Cộng vào ví

                System.out.println("\nQUA MÀN THÀNH CÔNG!");
                System.out.println(" Điểm nhận được: " + levelScore + " (Tổng Điểm: " + totalScore + ")");
                System.out.println(" Thu nhập vòng này: " + baseGold + " Vàng (Cơ bản) + " + interest + " Vàng (Lợi tức)");
                System.out.println(" Số dư ví hiện tại: " + gold + " Gold");

                currentLevelNumber++; // Tăng level chơi tiếp
            } else {
                System.out.println("\n💀 GAME OVER! Bạn đã hết lượt ở màn " + currentLevelNumber + ".");
                System.out.println("Số bí mật của màn này là: " + secretNumber);
                System.out.println("Tổng điểm chung cuộc đạt được: " + totalScore);
                isGameOver = true; // Thoát game
            }
        }

        // ==========================================
        // 4. KẾT QUẢ CUỐI CÙNG (PHÁ ĐẢO)
        // ==========================================
        if (currentLevelNumber > 30) {
            System.out.println("\n QUÁ XUẤT SẮC! Bạn đã phá đảo toàn bộ 30 màn chơi!");
            System.out.println("Tổng điểm kỷ lục của bạn: " + totalScore);
            System.out.println("Tổng số Vàng còn dư: " + gold + " Gold");
        }
        // ==========================================
        // 5. LƯU LỊCH SỬ NGƯỜI CHƠI (TÍCH HỢP FILE I/O)
        // ==========================================
        System.out.println("\n--- LƯU KẾT QUẢ ---");
        System.out.print("Nhập tên của bạn để lưu hồ sơ: ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) {
            playerName = "NguoiChoiBiAn"; // Tên mặc định nếu bấm Enter không nhập gì
        }

        // Tạo ID duy nhất bằng cách lấy thời gian mili-giây hiện tại
        String playerId = "P" + System.currentTimeMillis();

        // Lấy ngày chơi hiện tại (định dạng dd/MM/yyyy)
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Tính số màn đã hoàn thành (nếu thua ở màn hiện tại thì số màn qua là currentLevelNumber - 1)
        int levelsCompletedCount = (currentLevelNumber > 30) ? 30 : (currentLevelNumber - 1);

        // Đóng gói dữ liệu vào class Player
        Player newPlayer = new Player(playerId, playerName, totalScore, levelsCompletedCount, currentDate);

        // Gọi FileManager để ghi đè xuống CSV
        FileManager fileManager = new FileManager();
        fileManager.saveSingleMatch(newPlayer);

        System.out.println(" Đã lưu thành công lịch sử của [" + playerName + "] vào hệ thống!");
    }
}