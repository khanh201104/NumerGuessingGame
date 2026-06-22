package model;

public class Player {
    private String id;
    private String name;
    private int totalScore;
    private int levelsCompleted;
    private String playDate;

    // Constructor để tạo đối tượng Player mới
    public Player(String id, String name, int totalScore, int levelsCompleted, String playDate) {
        this.id = id;
        this.name = name;
        this.totalScore = totalScore;
        this.levelsCompleted = levelsCompleted;
        this.playDate = playDate;
    }

    // Các hàm Getters để FileManager có thể lấy dữ liệu ghi ra file
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public String getPlayDate() {
        return playDate;
    }

    // Hàm toString để in thông tin ra Bảng xếp hạng cho đẹp (dùng cho tính năng sau)
    @Override
    public String toString() {
        return String.format("ID: %-5s | Tên: %-15s | Điểm: %-6d | Màn hoàn thành: %-2d | Ngày chơi: %s",
                id, name, totalScore, levelsCompleted, playDate);
    }
}