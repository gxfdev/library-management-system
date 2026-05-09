package com.library.controller;

import com.library.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Tag(name = "验证码", description = "图形验证码生成与校验")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final int CODE_LENGTH = 4;
    private static final long CAPTCHA_EXPIRE_MS = 300_000;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Map<String, CaptchaEntry> captchaStore = new java.util.concurrent.ConcurrentHashMap<>();

    @Operation(summary = "生成图形验证码")
    @GetMapping("/image")
    public Result<CaptchaData> generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode(CODE_LENGTH);
        long now = Instant.now().toEpochMilli();

        captchaStore.put(captchaKey, new CaptchaEntry(code, now));

        cleanupExpired();

        BufferedImage image = createImage(code);
        String base64 = encodeToBase64(image);

        log.debug("生成验证码: key={}", captchaKey);
        return Result.success(new CaptchaData(base64, captchaKey));
    }

    @Operation(summary = "校验验证码")
    @PostMapping("/verify")
    public Result<Boolean> verifyCaptcha(@RequestParam String code, @RequestParam String captchaKey) {
        if (code == null || code.trim().isEmpty()) {
            return Result.error(400, "验证码不能为空");
        }
        if (code.trim().length() > 10) {
            return Result.error(400, "验证码格式不正确");
        }
        if (captchaKey == null || captchaKey.trim().isEmpty()) {
            return Result.error(400, "验证码Key不能为空");
        }

        CaptchaEntry entry = captchaStore.remove(captchaKey);
        if (entry == null) {
            return Result.error(400, "验证码已过期，请刷新");
        }

        if ((Instant.now().toEpochMilli() - entry.timestamp) > CAPTCHA_EXPIRE_MS) {
            return Result.error(400, "验证码已过期，请刷新");
        }

        boolean match = entry.code.equalsIgnoreCase(code.trim());
        return Result.success(match);
    }

    public static boolean verifyAndRemove(Map<String, CaptchaEntry> store, String captchaKey, String code) {
        if (captchaKey == null || code == null) return false;
        CaptchaEntry entry = store.remove(captchaKey);
        if (entry == null) return false;
        if ((Instant.now().toEpochMilli() - entry.timestamp) > CAPTCHA_EXPIRE_MS) return false;
        return entry.code.equalsIgnoreCase(code.trim());
    }

    public Map<String, CaptchaEntry> getCaptchaStore() {
        return captchaStore;
    }

    private void cleanupExpired() {
        long now = Instant.now().toEpochMilli();
        if (captchaStore.size() > 10000) {
            captchaStore.entrySet().removeIf(e -> (now - e.getValue().timestamp) > CAPTCHA_EXPIRE_MS);
        }
    }

    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private BufferedImage createImage(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(245, 247, 250));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(
                    200 + rndRange(-30, 30),
                    205 + rndRange(-25, 25),
                    215 + rndRange(-20, 20), 80
            ));
            int x1 = rnd(0, WIDTH), y1 = rnd(0, HEIGHT);
            int x2 = x1 + rndRange(-25, 25), y2 = y1 + rndRange(-15, 15);
            g2d.drawLine(x1, y1, x2, y2);
        }

        Font font = new Font("Arial", Font.BOLD | Font.ITALIC, 26);
        g2d.setFont(font);

        int charSpacing = (WIDTH - 16) / Math.max(code.length(), 1);
        for (int i = 0; i < code.length(); i++) {
            float hue = 210f + rndRange(-35, 35);
            Color c = Color.getHSBColor(hue / 360f, 0.55f + rndRange(-10, 10) / 100f, 0.32f + rndRange(-8, 8) / 100f);
            g2d.setColor(c);

            double angle = Math.toRadians(rndRange(-18, 18));
            int cx = 12 + i * charSpacing;
            int cy = HEIGHT / 2 + rndRange(-3, 3);

            g2d.translate(cx, cy);
            g2d.rotate(angle);
            g2d.drawString(String.valueOf(code.charAt(i)), -6, 5);
            g2d.rotate(-angle);
            g2d.translate(-cx, -cy);
        }

        for (int i = 0; i < 40; i++) {
            g2d.setColor(new Color(
                    rndRange(160, 220), rndRange(165, 225), rndRange(175, 235), rndRange(50, 120)
            ));
            g2d.fillOval(rnd(0, WIDTH), rnd(0, HEIGHT), 1, 1);
        }

        g2d.dispose();
        return img;
    }

    private String encodeToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("验证码编码失败", e);
            throw new RuntimeException("验证码生成失败");
        }
    }

    private static int rnd(int min, int max) { return ThreadLocalRandom.current().nextInt(min, max); }
    private static int rndRange(int min, int max) { return rnd(min, max); }

    public record CaptchaData(String image, String captchaKey) {}

    public static class CaptchaEntry {
        public final String code;
        public final long timestamp;

        public CaptchaEntry(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}
