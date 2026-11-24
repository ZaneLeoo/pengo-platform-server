package com.ruoyi.web.service.mes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link BomAiCadImagePreprocessor}. */
class BomAiCadImagePreprocessorTest {

    private final BomAiCadImagePreprocessor preprocessor = new BomAiCadImagePreprocessor();

    @Test
    void passesThroughSmallNormalImagesUnchanged() throws Exception {
        BufferedImage small = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 600);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(small, "png", out);
        byte[] bytes = out.toByteArray();

        List<BomAiCadImagePreprocessor.PreparedImage> prepared = preprocessor.prepareImageBytes(
                "small.png", "image/png", bytes);

        assertEquals(1, prepared.size());
        assertFalse(prepared.get(0).isCadSlice());
        assertEquals(bytes.length, prepared.get(0).getContent().length);
    }

    @Test
    void mapsExifOrientationsToCcwDegrees() {
        assertEquals(0, BomAiCadImagePreprocessor.exifOrientationToCcwDegrees(1));
        assertEquals(180, BomAiCadImagePreprocessor.exifOrientationToCcwDegrees(3));
        assertEquals(270, BomAiCadImagePreprocessor.exifOrientationToCcwDegrees(6));
        assertEquals(90, BomAiCadImagePreprocessor.exifOrientationToCcwDegrees(8));
    }

    @Test
    void parsesMinimalLittleEndianExifOrientationTag() {
        // Minimal JPEG SOI + APP1 EXIF header with Orientation = 8 (90 CCW)
        byte[] jpeg = new byte[]{
                (byte) 0xFF, (byte) 0xD8,
                (byte) 0xFF, (byte) 0xE1, 0x00, 0x22,
                'E', 'x', 'i', 'f', 0x00, 0x00,
                'I', 'I', 0x2A, 0x00, 0x08, 0x00, 0x00, 0x00,
                0x01, 0x00,
                0x12, 0x01, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xD9
        };
        assertEquals(8, BomAiCadImagePreprocessor.readJpegExifOrientation(jpeg));
    }

    @Test
    void detectsAndSlicesRotatedSyntheticCadDrawing() throws Exception {
        // Synthetic 4000x2800 landscape CAD drawing with outer border & dense
        // bottom-right title block + BOM grid,
        // then rotated 90 CW -> 2800x4000 portrait so title block sits in bottom-left!
        int w = 4000;
        int h = 2800;
        BufferedImage upright = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = upright.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        // Inner frame border
        for (int t = 0; t < 6; t++) {
            g.drawRect(80 + t, 80 + t, w - 160 - 2 * t, h - 160 - 2 * t);
        }
        // Title block + BOM column on right (x = 3050..3920, y = 1200..2720)
        int x0 = 3050;
        int x1 = 3920;
        for (int t = 0; t < 5; t++) {
            g.drawLine(x0 + t, 1200, x0 + t, 2720);
        }
        for (int y = 1200; y <= 2720; y += 80) {
            g.fillRect(x0, y, x1 - x0, 4);
        }
        for (int x = x0 + 100; x < x1; x += 150) {
            g.fillRect(x, 1200, 4, 1520);
        }
        g.dispose();

        // Rotate 90 CW (270 CCW) into portrait 2800x4000
        BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D rg = rotated.createGraphics();
        rg.translate(h, 0);
        rg.rotate(Math.PI / 2.0);
        rg.drawImage(upright, 0, 0, null);
        rg.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(rotated, "png", out);

        List<BomAiCadImagePreprocessor.PreparedImage> slices = preprocessor.prepareImageBytes(
                "synthetic-cad.png", "image/png", out.toByteArray());

        assertFalse(slices.isEmpty());
        assertTrue(slices.get(0).isCadSlice());
        BufferedImage crop = ImageIO.read(new java.io.ByteArrayInputStream(slices.get(0).getContent()));
        // Upright BOM column crop should be taller than wide (~950x1600)
        assertTrue(crop.getHeight() > crop.getWidth());
    }
}
