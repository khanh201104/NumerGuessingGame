package main;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    // Lưu trữ mức phần trăm âm lượng hiện tại (0 - 100) của nhạc nền và hiệu ứng
    public static int currentBGMVolume = 100;
    public static int currentSFXVolume = 100;

    // Đối tượng kiểm soát tệp âm thanh nền đang chạy để có thể tăng/giảm âm lượng trực tiếp
    private static Clip bgClip;

    // 👉 [VẤN ĐÁP]: Tại sao không truyền thẳng giá trị 0-100 vào để chỉnh âm lượng?
    // Trả lời: Vì thư viện Java Swing xử lý âm thanh theo thang đo Decibel (dB) logarit chứ không theo phần trăm tuyến tính.
    public static void setClipVolume(Clip clip, int volumePercent) {
        // Kiểm tra xem dòng phần cứng âm thanh của máy có hỗ trợ kiểm soát âm lượng tổng (MASTER_GAIN) không
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            try {
                // Trích xuất bộ điều khiển âm lượng của đối tượng Clip ra
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                if (volumePercent <= 0) {
                    // Nếu kéo thanh trượt về mức 0, ép âm lượng xuống mức thấp nhất hệ thống hỗ trợ để tắt tiếng
                    control.setValue(control.getMinimum());
                } else {
                    // Áp dụng công thức chuẩn để quy đổi tỷ lệ phần trăm (0-100) sang dải trị số Decibel (dB)
                    float dB = (float) (Math.log10(volumePercent / 100.0) * 20.0);

                    // 👉 [VẤN ĐÁP]: Đoạn mã kiểm tra IF-ELSE dưới đây có tác dụng gì?
                    // Trả lời: Đây là kỹ thuật Chặn biên an toàn (Clamp). Nếu giá trị dB tính toán vượt quá giới hạn loa của hệ điều hành,
                    // hệ thống sẽ tự động ép nó về mức tối đa hoặc tối thiểu cho phép để tránh ném ra ngoại lệ gây treo ứng dụng.
                    if (dB < control.getMinimum()) {
                        dB = control.getMinimum();
                    } else if (dB > control.getMaximum()) {
                        dB = control.getMaximum();
                    }

                    // Thực thi áp dụng mức âm lượng mới vào luồng phát âm thanh
                    control.setValue(dB);
                }
            } catch (Exception e) {
                System.out.println("Lỗi can thiệp âm lượng: " + e.getMessage());
            }
        }
    }

    // Cập nhật âm lượng nhạc nền ngay lập tức khi người chơi thao tác kéo thanh trượt trên giao diện cài đặt
    public static void updateBGMVolume(int volume) {
        currentBGMVolume = volume; // Ghi nhận giá trị phần trăm mới vào bộ nhớ cấu hình
        if (bgClip != null) {
            setClipVolume(bgClip, volume); // Áp dụng ngay lập tức mức âm lượng mới vào tệp nhạc nền đang chạy
        }
    }

    // 👉 [VẤN ĐÁP]: Tại sao lại sử dụng cấu trúc Đa luồng 'new Thread(() -> { ... }).start();' ở đây?
    // Trả lời: Việc nạp tệp âm thanh từ bộ nhớ cứng (ổ đĩa) tiêu tốn tài nguyên. Nếu chạy trực tiếp trên luồng đồ họa chính (Main Thread),
    // ứng dụng sẽ bị khựng hoặc đóng băng khung hình trong tích tắc. Chạy luồng phụ (Thread) giúp nạp âm thanh ngầm mà không ảnh hưởng tới trải nghiệm.
    public static void playSound(String fileName) {
        new Thread(() -> {
            try {
                File soundFile = new File(fileName);
                if (soundFile.exists()) {
                    // Mở luồng nạp dữ liệu âm thanh đầu vào từ file
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);

                    // Đồng bộ mức cấu hình âm lượng hiệu ứng (SFX) từ bộ nhớ cài đặt trước khi phát
                    setClipVolume(clip, currentSFXVolume);

                    clip.start(); // Kích hoạt phát âm thanh hiệu ứng (Phát một lần rồi tự động giải phóng)
                }
            } catch (Exception e) {
                System.out.println("Lỗi phát âm thanh " + fileName + ": " + e.getMessage());
            }
        }).start(); // Kích hoạt luồng phụ chạy độc lập
    }

    // Phương thức phát nhạc nền cho trò chơi
    public static void playBackgroundMusic(String fileName) {
        new Thread(() -> {
            try {
                File soundFile = new File(fileName);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    bgClip = AudioSystem.getClip();
                    bgClip.open(audioIn);

                    // Đồng bộ mức cấu hình âm lượng nhạc nền (BGM) từ bộ nhớ cài đặt
                    setClipVolume(bgClip, currentBGMVolume);

                    // 👉 [VẤN ĐÁP]: Làm thế nào để nhạc nền tự động phát lại khi kết thúc bài?
                    // Trả lời: Sử dụng thuộc tính chỉ định 'Clip.LOOP_CONTINUOUSLY' để ra lệnh cho thư viện âm thanh lặp lại vô hạn tệp tin này.
                    bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgClip.start(); // Khởi chạy tệp nhạc nền
                } else {
                    System.out.println("CẢNH BÁO: Không tìm thấy file " + fileName);
                }
            } catch (Exception e) {
                System.out.println("Lỗi phát nhạc nền: " + e.getMessage());
            }
        }).start();
    }

    // Dừng nhạc nền (sử dụng khi người chơi thoát ứng dụng hoặc kết thúc game)
    public static void stopBackgroundMusic() {
        if (bgClip != null && bgClip.isRunning()) {
            bgClip.stop();
        }
    }
}