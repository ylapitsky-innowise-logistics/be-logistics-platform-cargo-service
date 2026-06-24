package com.innowise.logistics.cargoservice.util.testdata;

import org.springframework.web.multipart.MultipartFile;

public class TestImageRunner {

    public static void main(String[] args) {
        // Здесь мы не используем Spring, поэтому создаём сервис вручную (или через @Autowired в тесте)
        // Но для простоты — вызываем генерацию без загрузки в сервис
        try {
            MultipartFile file = ImageGenerator.generateImage(
                    800, 600,
                    "Тестовое изображение",
                    "test_cargo_image.png"
            );

            // Файл уже сохранён на диск и готов к использованию
            System.out.println("✅ Изображение сгенерировано и сохранено!");
            System.out.println("📁 Файл: " + file.getOriginalFilename());
            System.out.println("📏 Размер: " + file.getSize() + " байт");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}