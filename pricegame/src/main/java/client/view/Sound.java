package client.view;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Nhạc nền + hiệu ứng âm thanh (file WAV trong resources/sounds).
 * Mọi lỗi (không có thiết bị âm thanh, thiếu file) đều bị bỏ qua để game vẫn chạy bình thường.
 */
public final class Sound {
    public enum Effect {
        CORRECT("correct", 4f), WRONG("wrong", 0f), WIN("win", 3f), LOSE("lose", -2f),
        CHALLENGE("challenge", 6f), TICK("tick", -4f);

        final String file;
        final float gainDb;
        Effect(String file, float gainDb) { this.file = file; this.gainDb = gainDb; }
    }

    private static final float MUSIC_LOBBY = 0.35f;
    private static final float MUSIC_GAME = 0.16f;

    private static final Map<String, byte[]> CACHE = new ConcurrentHashMap<>();
    private static final List<Runnable> MUTE_LISTENERS = new CopyOnWriteArrayList<>();

    private static Clip music;
    private static float musicVolume = MUSIC_LOBBY;
    private static boolean muted;
    private static boolean musicWanted;

    private Sound() {}

    // ---------- nhạc nền ----------
    public static synchronized void startMusic() {
        musicWanted = true;
        if (muted || (music != null && music.isRunning())) return;
        try {
            stopClip();
            music = open("bgm");
            applyGain(music, musicVolume);
            music.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            music = null;
        }
    }

    public static synchronized void stopMusic() {
        musicWanted = false;
        stopClip();
    }

    /** Vào trận: hạ nhỏ nhạc nền để nghe rõ hiệu ứng; về sảnh: tăng lại. */
    public static synchronized void setInGame(boolean inGame) {
        musicVolume = inGame ? MUSIC_GAME : MUSIC_LOBBY;
        if (music != null) applyGain(music, musicVolume);
    }

    private static void stopClip() {
        if (music != null) {
            music.stop();
            music.close();
            music = null;
        }
    }

    // ---------- hiệu ứng ----------
    public static void play(Effect e) {
        if (muted) return;
        try {
            Clip c = open(e.file);
            setGainDb(c, e.gainDb);
            c.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.STOP) c.close(); });
            c.start();
        } catch (Exception ignored) { }
    }

    // ---------- tắt / bật tiếng ----------
    public static boolean isMuted() { return muted; }

    public static synchronized void setMuted(boolean m) {
        muted = m;
        if (m) stopClip();
        else if (musicWanted) startMusic();
        for (Runnable r : MUTE_LISTENERS) r.run();
    }

    public static void toggleMute() { setMuted(!muted); }

    public static void onMuteChange(Runnable r) { MUTE_LISTENERS.add(r); }

    // ---------- hỗ trợ ----------
    private static Clip open(String name) throws Exception {
        byte[] data = CACHE.computeIfAbsent(name, n -> {
            try (InputStream in = Sound.class.getResourceAsStream("/sounds/" + n + ".wav")) {
                if (in == null) return new byte[0];
                return in.readAllBytes();
            } catch (IOException e) {
                return new byte[0];
            }
        });
        if (data.length == 0) throw new IOException("Thiếu file âm thanh: " + name);
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        return clip;
    }

    /** Đặt âm lượng tuyến tính 0..1 (qua MASTER_GAIN, đơn vị dB). */
    private static void applyGain(Clip c, float volume) {
        if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl g = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        float db = volume <= 0.0001f ? g.getMinimum() : (float) (20.0 * Math.log10(volume));
        g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), db)));
    }

    private static void setGainDb(Clip c, float db) {
        if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl g = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), db)));
    }
}
