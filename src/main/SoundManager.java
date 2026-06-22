package main;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    // Mặc định khởi động game là 100% âm lượng
    public static int currentBGMVolume = 100;
    public static int currentSFXVolume = 100;
    private static Clip bgClip;

    // Hàm quy đổi % sang Decibel và áp dụng cho Clip (Đã vá lỗi an toàn tuyệt đối)
    public static void setClipVolume(Clip clip, int volumePercent) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            try {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (volumePercent <= 0) {
                    control.setValue(control.getMinimum()); // Tắt tiếng hoàn toàn
                } else {
                    // Công thức chuẩn chuyển đổi % sang dB
                    float dB = (float) (Math.log10(volumePercent / 100.0) * 20.0);

                    // Chặn lỗi ngầm nếu dB vượt quá giới hạn âm thanh của hệ điều hành
                    if (dB < control.getMinimum()) {
                        dB = control.getMinimum();
                    } else if (dB > control.getMaximum()) {
                        dB = control.getMaximum();
                    }

                    control.setValue(dB);
                }
            } catch (Exception e) {
                System.out.println("Lỗi can thiệp âm lượng: " + e.getMessage());
            }
        }
    }

    // Cập nhật âm lượng nhạc nền ngay lập tức khi kéo thanh trượt
    public static void updateBGMVolume(int volume) {
        currentBGMVolume = volume;
        if (bgClip != null) {
            setClipVolume(bgClip, volume);
        }
    }

    public static void playSound(String fileName) {
        new Thread(() -> {
            try {
                File soundFile = new File(fileName);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);

                    // Áp dụng âm lượng hiệu ứng (SFX) từ thanh cài đặt
                    setClipVolume(clip, currentSFXVolume);

                    clip.start();
                }
            } catch (Exception e) {
                System.out.println("Lỗi phát âm thanh " + fileName + ": " + e.getMessage());
            }
        }).start();
    }

    public static void playBackgroundMusic(String fileName) {
        new Thread(() -> {
            try {
                File soundFile = new File(fileName);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    bgClip = AudioSystem.getClip();
                    bgClip.open(audioIn);

                    // Áp dụng âm lượng nhạc nền (BGM) từ thanh cài đặt
                    setClipVolume(bgClip, currentBGMVolume);

                    bgClip.loop(Clip.LOOP_CONTINUOUSLY); // Lặp vô hạn
                    bgClip.start();
                } else {
                    System.out.println("CẢNH BÁO: Không tìm thấy file " + fileName);
                }
            } catch (Exception e) {
                System.out.println("Lỗi phát nhạc nền: " + e.getMessage());
            }
        }).start();
    }

    public static void stopBackgroundMusic() {
        if (bgClip != null && bgClip.isRunning()) {
            bgClip.stop();
        }
    }
}