package com.innowise.logistics.cargoservice.util.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 🖼️ Генератор Картинок
 * Утилита для генерации тестовых изображений с текстом.
 */
@Slf4j
@RequiredArgsConstructor
public class ImageGenerator implements Generator<MultipartFile>{

    @Override
    public MultipartFile[] generate(int quantity) {
        return new MultipartFile[0];
    }

    /**
     * Генерирует изображение с текстом, занимающим максимальную площадь,
     * сохраняет его в файл и возвращает как MultipartFile.
     *
     * @param width        ширина изображения в пикселях
     * @param height       высота изображения в пикселях
     * @param message      текст, который будет написан на изображении
     * @param fileName     имя файла (например, "test_image.png")
     * @return MultipartFile готовый для отправки в контроллер
     */
    public static MultipartFile generateImage(int width, int height, String message, String fileName) {
        try {
            // 1. Создаем изображение
            BufferedImage image = createImageWithText(width, height, message);

            // 2. Конвертируем BufferedImage в byte[]
            byte[] imageBytes = convertToBytes(image, "png");

            // 3. Сохраняем изображение в файл (в корневую директорию проекта)
            saveImageToFile(image, fileName);

            // 4. Создаем MultipartFile из byte[]
            return new InMemoryMultipartFile(fileName, "image/png", imageBytes);

        } catch (IOException e) {
            throw new RuntimeException("Не удалось сгенерировать изображение: " + e.getMessage(), e);
        }
    }

    /**
     * Генерирует изображение с текстом, занимающим максимальную площадь,
     * и сохраняет его в файл.
     *
     * @param width        ширина изображения в пикселях
     * @param height       высота изображения в пикселях
     * @param message      текст, который будет написан на изображении
     * @param fileName     имя файла (например, "test_image.png")
     * @return Path к сохраненному файлу
     */
    public static Path generateAndSaveImage(int width, int height, String message, String fileName) {
        try {
            BufferedImage image = createImageWithText(width, height, message);
            return saveImageToFile(image, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить изображение: " + e.getMessage(), e);
        }
    }

    /**
     * Создает BufferedImage с текстом, занимающим максимальную площадь.
     */
    private static BufferedImage createImageWithText(int width, int height, String message) {
        // 1. Создаем изображение
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 2. Настройка рендеринга для лучшего качества
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 3. Заливаем фон (белый или светло-серый)
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 4. Рисуем рамку (для красоты)
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(5, 5, width - 10, height - 10);

            // 5. Подбираем максимальный размер шрифта
            Font font = findMaxFontSize(g2d, message, width, height);

            // 6. Рисуем текст
            g2d.setColor(Color.BLACK);
            g2d.setFont(font);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int textHeight = fm.getHeight();

            // Центрируем текст
            int x = (width - textWidth) / 2;
            int y = (height - textHeight) / 2 + fm.getAscent();

            // Рисуем текст с тенью для лучшей читаемости
            g2d.setColor(Color.GRAY);
            g2d.drawString(message, x + 2, y + 2);
            g2d.setColor(Color.BLACK);
            g2d.drawString(message, x, y);

            // 7. Добавляем информацию о размерах внизу (мелким шрифтом)
            String sizeInfo = width + "x" + height + " px";
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics smallFm = g2d.getFontMetrics();
            int infoX = width - smallFm.stringWidth(sizeInfo) - 15;
            int infoY = height - 15;
            g2d.drawString(sizeInfo, infoX, infoY);

            return image;

        } finally {
            g2d.dispose();
        }
    }

    /**
     * Подбирает максимальный размер шрифта, при котором текст полностью помещается в изображение.
     */
    private static Font findMaxFontSize(Graphics2D g2d, String text, int maxWidth, int maxHeight) {
        // Начинаем с небольшого размера и увеличиваем
        int fontSize = 10;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        FontMetrics fm = g2d.getFontMetrics(font);

        // Пока текст помещается, увеличиваем размер
        while (fm.stringWidth(text) < maxWidth - 40 && fm.getHeight() < maxHeight - 40) {
            fontSize++;
            font = new Font("Arial", Font.BOLD, fontSize);
            fm = g2d.getFontMetrics(font);
        }

        // Возвращаем предыдущий размер, который точно помещался
        return new Font("Arial", Font.BOLD, Math.max(fontSize - 1, 10));
    }

    /**
     * Сохраняет изображение в файл в корневой директории проекта.
     */
    private static Path saveImageToFile(BufferedImage image, String fileName) throws IOException {
        // Определяем корневую директорию проекта (user.dir)
        String rootDir = System.getProperty("user.dir");
        Path outputPath = Paths.get(rootDir, "generated_images");

        // Создаем папку, если её нет
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            System.out.println("Создана папка: " + outputPath.toAbsolutePath());
        }

        // Полный путь к файлу
        Path filePath = outputPath.resolve(fileName);

        // Сохраняем изображение
        ImageIO.write(image, "png", filePath.toFile());
        System.out.println("✅ Изображение сохранено: " + filePath.toAbsolutePath());

        return filePath;
    }

    /**
     * Конвертирует BufferedImage в массив байтов.
     */
    private static byte[] convertToBytes(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        }
    }

    /**
     * Вспомогательный класс для создания MultipartFile из byte[] в памяти.
     */
    private static class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String contentType;
        private final byte[] content;

        public InMemoryMultipartFile(String name, String contentType, byte[] content) {
            this.name = name;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}

