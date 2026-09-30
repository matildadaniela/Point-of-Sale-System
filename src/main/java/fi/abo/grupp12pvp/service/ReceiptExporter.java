package fi.abo.grupp12pvp.service;

import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ReceiptExporter {

    private ReceiptExporter() {
    }

    public static void saveAsPng(Sale sale, Path outputPath) throws IOException {
        List<String> lines = createReceiptLines(sale);

        int width = 500;
        int lineHeight = 28;
        int height = 40 + lines.size() * lineHeight;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        graphics.setFill(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setFill(Color.BLACK);
        graphics.setFont(Font.font("Monospaced", 18));

        double y = 30;
        for (String line : lines) {
            graphics.fillText(line, 20, y);
            y += lineHeight;
        }

        WritableImage image = new WritableImage(width, height);
        canvas.snapshot(null, image);

        BufferedImage bufferedImage = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );
        PixelReader pixelReader = image.getPixelReader();

        for (int yPixel = 0; yPixel < height; yPixel++) {
            for (int xPixel = 0; xPixel < width; xPixel++) {
                bufferedImage.setRGB(
                        xPixel,
                        yPixel,
                        pixelReader.getArgb(xPixel, yPixel)
                );
            }
        }

        if (!ImageIO.write(bufferedImage, "png", outputPath.toFile())) {
            throw new IOException("Could not save receipt as PNG");
        }
    }

    private static List<String> createReceiptLines(Sale sale) {
        List<String> lines = new ArrayList<>();

        lines.add("              STORE RECEIPT");
        lines.add("------------------------------------------");

        for (Map.Entry<Product, Integer> entry : sale.getGroupedItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            String itemTotal = product.price()
                    .multiply(java.math.BigDecimal.valueOf(quantity))
                    .toPlainString();

            lines.add(product.name() + " x" + quantity + "  EUR " + itemTotal);
        }

        lines.add("------------------------------------------");
        lines.add("Total:        EUR " + sale.getTotal().toPlainString());
        lines.add("Paid by card: EUR " + sale.getCardPaid().toPlainString());
        lines.add("Paid by cash: EUR " + sale.getCashPaid().toPlainString());
        lines.add("");
        lines.add("Thank you!");

        return lines;
    }
}
