package service;

import model.Player;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    // Đường dẫn tới file CSV lưu dữ liệu
    private static final String FILE_PATH = "data/players.csv";

    // BƯỚC 1: Hàm ghi 1 lượt chơi mới vào cuối file (Append)
    public void saveSingleMatch(Player player) {
        // Đảm bảo thư mục "data" tồn tại trước khi lưu
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdir();
        }

        // FileWriter với tham số 'true' để ghi tiếp vào cuối file (append) chứ không ghi đè mất dữ liệu cũ
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            // Định dạng CSV phân tách bằng dấu phẩy
            String line = player.getId() + "," +
                    player.getName() + "," +
                    player.getTotalScore() + "," +
                    player.getLevelsCompleted() + "," +
                    player.getPlayDate();
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Lỗi khi lưu dữ liệu file: " + e.getMessage());
        }
    }

    // BƯỚC 2: Hàm đọc toàn bộ dữ liệu từ file lên danh sách (Dùng cho Bảng xếp hạng)
    public List<Player> loadAllPlayers() {
        List<Player> playerList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return playerList; // Nếu chưa có file (chưa ai chơi) thì trả về list rỗng
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    // Phục hồi lại đối tượng Player từ chuỗi text
                    Player p = new Player(data[0], data[1], Integer.parseInt(data[2]),
                            Integer.parseInt(data[3]), data[4]);
                    playerList.add(p);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("⚠ Lỗi khi đọc dữ liệu file: " + e.getMessage());
        }
        return playerList;
    }
}