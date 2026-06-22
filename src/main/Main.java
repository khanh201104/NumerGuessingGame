package main;

import service.GameEngine;
import service.PlayerManager;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Khởi tạo các cỗ máy xử lý
        GameEngine engine = new GameEngine();
        PlayerManager playerManager = new PlayerManager();

        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n==================================");
            System.out.println("   GAME ĐOÁN SỐ ");
            System.out.println("==================================");
            System.out.println("1. Chơi Game mới");
            System.out.println("2. Xem Bảng xếp hạng (Top Điểm)");
            System.out.println("3. Xem toàn bộ Lịch sử chơi");
            System.out.println("4. Thoát");
            System.out.print("Chọn chức năng (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    engine.startCampaign(); // Chạy game và tự động lưu điểm ở cuối
                    break;
                case "2":
                    playerManager.displayLeaderboard(); // Gọi chức năng sắp xếp và in Bảng xếp hạng
                    break;
                case "3":
                    playerManager.displayAllHistory(); // Gọi chức năng in lịch sử thô
                    break;
                case "4":
                    System.out.println("Cảm ơn bạn đã chơi game. Tạm biệt!");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Lựa chọn không hợp lệ! Vui lòng chỉ nhập số từ 1 đến 4.");
            }
        }
        scanner.close();
    }
}