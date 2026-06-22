package service;

import model.Player;
import java.util.List;

public class PlayerManager {
    private FileManager fileManager;

    public PlayerManager() {
        this.fileManager = new FileManager(); // Khởi tạo thợ xây file
    }

    // Hàm 1: Hiển thị Bảng xếp hạng Top người chơi
    public void displayLeaderboard() {
        List<Player> players = fileManager.loadAllPlayers();

        if (players.isEmpty()) {
            System.out.println("\n⚠ Chưa có dữ liệu người chơi nào! Hãy chơi thử một ván trước nhé.");
            return;
        }

        // Sắp xếp danh sách theo Tổng Điểm giảm dần (Từ cao xuống thấp)
        players.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        System.out.println("\n================ BẢNG XẾP HẠNG (TOP ĐIỂM SỐ) ================");
        System.out.printf("%-15s | %-15s | %-10s | %-10s | %-15s\n", "ID", "Tên", "Điểm", "Số màn", "Ngày chơi");
        System.out.println("-----------------------------------------------------------------------------");

        // In ra danh sách (Chỉ lấy tối đa Top 10 người cao nhất để bảng không bị quá dài)
        int limit = Math.min(players.size(), 10);
        for (int i = 0; i < limit; i++) {
            Player p = players.get(i);
            System.out.printf("%-15s | %-15s | %-10d | %-10d | %-15s\n",
                    p.getId(), p.getName(), p.getTotalScore(), p.getLevelsCompleted(), p.getPlayDate());
        }
        System.out.println("=============================================================================");
    }

    // Hàm 2: Hiển thị toàn bộ lịch sử (Chức năng số 3 ở Menu)
    public void displayAllHistory() {
        List<Player> players = fileManager.loadAllPlayers();

        if(players.isEmpty()){
            System.out.println("\n⚠ Chưa có dữ liệu lịch sử.");
            return;
        }

        System.out.println("\n--- TẤT CẢ LỊCH SỬ CHƠI KHÔNG SẮP XẾP ---");
        for(Player p : players) {
            System.out.println(p.toString()); // Gọi hàm toString() từ class Player
        }
    }
}