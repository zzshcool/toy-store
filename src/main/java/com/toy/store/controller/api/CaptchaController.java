package com.toy.store.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

@Controller
public class CaptchaController {

    @GetMapping("/api/captcha")
    public void getCaptcha(HttpSession session, HttpServletResponse response) throws IOException {
        int width = 100;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // 背景
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, width, height);

        // 隨機字串
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            char c = chars.charAt(random.nextInt(chars.length()));
            captcha.append(c);
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(String.valueOf(c), 15 + i * 20, 28);
        }

        // 干擾線
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }

        session.setAttribute("captcha", captcha.toString());

        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }
}
