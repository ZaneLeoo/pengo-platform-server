package com.ruoyi.web.service.mes;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 超大幅面工程图（如 A0/A1/A2x3 国标 CAD 图纸）智能预处理组件： 1. 自动识别 JPEG EXIF 旋转标记与无 EXIF
 * 躺倒图纸方向（基于国标标题栏象限定位）； 2. 低内存方式定位右侧主明细表 + 标题栏（超长自动纵向分段并保留重叠行）及标题栏左侧续表； 3.
 * 仅裁切目标区域并旋转扶正，避免对上亿像素整图分配副本导致堆内存溢出； 4. 普通中小尺寸图纸（最大边 < 3200px）自动跳过，零额外开销。
 */
@Component
public class BomAiCadImagePreprocessor {

    private static final Logger log = LoggerFactory.getLogger(BomAiCadImagePreprocessor.class);
    static final int CAD_MIN_LONG_EDGE = 3200;
    private static final int MAX_SLICE_HEIGHT_FULL = 4300;
    private static final int CHUNK_HEIGHT_FULL = 2600;
    private static final int CHUNK_OVERLAP_FULL = 380;
    private static final int MAX_UPLOAD_EDGE = 2600;
    private static final int DARK_THRESHOLD = 150;

    /** 预处理生成的待上传图片切片。 */
    public static class PreparedImage {
        private final String filename;
        private final String contentType;
        private final byte[] content;
        private final boolean cadSlice;

        public PreparedImage(String filename, String contentType, byte[] content, boolean cadSlice) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
            this.cadSlice = cadSlice;
        }

        public String getFilename() {
            return filename;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getContent() {
            return content;
        }

        public boolean isCadSlice() {
            return cadSlice;
        }
    }

    /**
     * 对单张原始图片字节进行预处理；若非超大工程图则直接透传原图，若为超大 CAD 图纸则返回扶正后的高清明细表切片列表。
     */
    public List<PreparedImage> prepareImageBytes(String filename, String contentType, byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length == 0) {
            return Collections.emptyList();
        }
        BufferedImage rawImage;
        try {
            rawImage = ImageIO.read(new ByteArrayInputStream(rawBytes));
        } catch (IOException ignored) {
            return List.of(new PreparedImage(filename, safeContentType(contentType), rawBytes, false));
        }
        if (rawImage == null) {
            return List.of(new PreparedImage(filename, safeContentType(contentType), rawBytes, false));
        }
        try {
            int rawW = rawImage.getWidth();
            int rawH = rawImage.getHeight();
            int exifOrientation = readJpegExifOrientation(rawBytes);
            int exifCcwDeg = exifOrientationToCcwDegrees(exifOrientation);
            if (Math.max(rawW, rawH) < CAD_MIN_LONG_EDGE) {
                if (exifCcwDeg == 0) {
                    return List.of(new PreparedImage(filename, safeContentType(contentType), rawBytes, false));
                }
                BufferedImage upright = rotateCcw(rawImage, exifCcwDeg);
                try {
                    return List.of(new PreparedImage(
                            replaceExtensionWithPng(filename), "image/png", encodePng(upright), false));
                } finally {
                    upright.flush();
                }
            }
            return sliceLargeCadImage(filename, rawImage, exifCcwDeg);
        } catch (Exception exception) {
            log.warn("CAD 高清预处理异常，回退原图上传 ({}): {}", filename, exception.getMessage());
            return List.of(new PreparedImage(filename, safeContentType(contentType), rawBytes, false));
        } finally {
            rawImage.flush();
        }
    }

    /**
     * 对已解码的 {@link BufferedImage}（如 PDFBox 渲染的超大 A1/A0 工程图页）进行高清切片预处理。
     */
    public List<PreparedImage> prepareBufferedImage(String baseFilename, BufferedImage image) throws IOException {
        if (image == null) {
            return Collections.emptyList();
        }
        if (Math.max(image.getWidth(), image.getHeight()) < CAD_MIN_LONG_EDGE) {
            return List.of(new PreparedImage(baseFilename, "image/png", encodePng(image), false));
        }
        return sliceLargeCadImage(baseFilename, image, 0);
    }

    private List<PreparedImage> sliceLargeCadImage(String filename, BufferedImage rawImage, int exifCcwDeg)
            throws IOException {
        int rawW = rawImage.getWidth();
        int rawH = rawImage.getHeight();

        // 1. 低内存构造 min-pool 缩略图（直接从原图降采样，绝不复制上亿像素旋转图）
        GrayGrid rawSmall = buildMinPoolGray(rawImage, 1500);
        int step = rawSmall.step;
        GrayGrid afterExif = rotateGridCcw(rawSmall, exifCcwDeg);

        // 2. 基于国标标题栏象限密度自动判定是否仍需旋转 90/180/270 度
        int extraCcwDeg = detectCadRotationCcw(afterExif);
        int totalCcwDeg = (exifCcwDeg + extraCcwDeg) % 360;
        GrayGrid uprightSmall = extraCcwDeg == 0 ? afterExif : rotateGridCcw(afterExif, extraCcwDeg);

        int uprightFullW = (totalCcwDeg == 90 || totalCcwDeg == 270) ? rawH : rawW;
        int uprightFullH = (totalCcwDeg == 90 || totalCcwDeg == 270) ? rawW : rawH;

        // 3. 在扶正缩略图上定位右侧主明细表 + 标题栏及左侧续表
        List<NamedBox> boxes = detectTableRegions(uprightSmall, step, uprightFullW, uprightFullH);
        if (boxes.isEmpty()) {
            // 兜底：返回整体适度缩放的扶正图
            BufferedImage fullFallback = extractRotatedScaledCrop(
                    rawImage, totalCcwDeg, 0, 0, uprightFullW, uprightFullH, MAX_UPLOAD_EDGE);
            try {
                return List.of(new PreparedImage(
                        replaceExtensionWithPng(filename), "image/png", encodePng(fullFallback), false));
            } finally {
                fullFallback.flush();
            }
        }

        String stem = stripExtension(filename);
        List<PreparedImage> result = new ArrayList<>();
        for (NamedBox box : boxes) {
            BufferedImage crop = extractRotatedScaledCrop(
                    rawImage, totalCcwDeg, box.x0, box.y0, box.x1, box.y1, MAX_UPLOAD_EDGE);
            try {
                byte[] png = encodePng(crop);
                String sliceName = stem + "-" + box.name + ".png";
                log.info("CAD slice {}: raw={}x{}, rotateCCW={}, box=[{},{},{},{}], out={}x{} ({} bytes)",
                        sliceName, rawW, rawH, totalCcwDeg, box.x0, box.y0, box.x1, box.y1,
                        crop.getWidth(), crop.getHeight(), png.length);
                result.add(new PreparedImage(sliceName, "image/png", png, true));
            } finally {
                crop.flush();
            }
        }
        return result;
    }

    /** 定位国标 CAD 工程图的右侧主明细表+标题栏（超长自动纵向分段）与左侧续表。 */
    private List<NamedBox> detectTableRegions(GrayGrid g, int step, int fullW, int fullH) {
        int sw = g.width;
        int sh = g.height;

        // 1. 检测内外图框边界（容忍扫描轻微倾斜：3px 带宽）
        int rightBorder = sw - 1;
        int yFrom = (int) (sh * 0.12);
        int yTo = (int) (sh * 0.88);
        int vNeed = (int) ((yTo - yFrom) * 0.78);
        for (int x = sw - 3; x > (int) (sw * 0.90); x--) {
            int cnt = 0;
            for (int y = yFrom; y < yTo; y++) {
                if (g.minHoriz3(x, y) < DARK_THRESHOLD) {
                    cnt++;
                }
            }
            if (cnt > vNeed) {
                rightBorder = x;
                break;
            }
        }

        int bottomBorder = sh - 1;
        int xFrom = (int) (sw * 0.12);
        int xTo = (int) (sw * 0.88);
        int hNeed = (int) ((xTo - xFrom) * 0.78);
        for (int y = sh - 3; y > (int) (sh * 0.90); y--) {
            int cnt = 0;
            for (int x = xFrom; x < xTo; x++) {
                if (g.minVert3(x, y) < DARK_THRESHOLD) {
                    cnt++;
                }
            }
            if (cnt > hNeed) {
                bottomBorder = y;
                break;
            }
        }

        int topBorder = 0;
        for (int y = 2; y < (int) (sh * 0.10); y++) {
            int cnt = 0;
            for (int x = xFrom; x < xTo; x++) {
                if (g.minVert3(x, y) < DARK_THRESHOLD) {
                    cnt++;
                }
            }
            if (cnt > hNeed) {
                topBorder = y;
                break;
            }
        }

        int innerH = Math.max(1, bottomBorder - topBorder);

        // 2. 沿内图框底部向上追踪，查找右侧标题栏 + 主明细表的左侧垂直边界墙
        int xMin = Math.max(0, (int) (rightBorder - sw * 0.32));
        int xMax = Math.max(xMin + 1, (int) (rightBorder - sw * 0.12));
        int probeStartY = Math.max(topBorder + 1, (int) (bottomBorder - innerH * 0.03));
        int probeStopY = Math.max(topBorder, (int) (bottomBorder - innerH * 0.08));

        List<int[]> wallCandidates = new ArrayList<>(); // [x, span, topY]
        for (int x = xMin; x < xMax; x++) {
            int startY = -1;
            for (int y = probeStartY; y > probeStopY; y--) {
                if (g.hasDarkInHoriz5(x, y, DARK_THRESHOLD)) {
                    startY = y;
                    break;
                }
            }
            if (startY < 0) {
                continue;
            }
            int gap = 0;
            int topY = startY;
            int curX = x;
            for (int y = startY; y > topBorder; y--) {
                int hitDx = Integer.MIN_VALUE;
                int[] offsets = {0, -1, 1, -2, 2};
                for (int dx : offsets) {
                    int nx = clamp(curX + dx, 0, sw - 1);
                    if (g.get(nx, y) < DARK_THRESHOLD) {
                        hitDx = dx;
                        break;
                    }
                }
                if (hitDx != Integer.MIN_VALUE) {
                    topY = y;
                    gap = 0;
                    if (Math.abs((curX + hitDx) - x) <= 5) {
                        curX += hitDx;
                    }
                } else {
                    gap++;
                    if (gap > 12) {
                        break;
                    }
                }
            }
            int span = startY - topY;
            if (span >= (int) (innerH * 0.25)) {
                int spanW = Math.max(1, rightBorder - x);
                int hConfirm = 0;
                int lastHy = -999;
                for (int y = topY; y < probeStartY; y++) {
                    int hcnt = 0;
                    for (int xx = x; xx < rightBorder; xx++) {
                        if (g.get(xx, y) < DARK_THRESHOLD) {
                            hcnt++;
                        }
                    }
                    if (hcnt >= spanW * 0.72 && (y - lastHy) > 5) {
                        hConfirm++;
                        lastHy = y;
                    }
                }
                if (hConfirm >= 4) {
                    wallCandidates.add(new int[]{x, span, topY});
                }
            }
        }

        int tbLeft;
        int wallTopY;
        if (!wallCandidates.isEmpty()) {
            int maxSpan = 0;
            for (int[] c : wallCandidates) {
                maxSpan = Math.max(maxSpan, c[1]);
            }
            int[] best = wallCandidates.get(0);
            for (int[] c : wallCandidates) {
                if (c[1] >= maxSpan * 0.90) {
                    best = c;
                    break;
                }
            }
            tbLeft = best[0];
            wallTopY = best[2];
        } else {
            tbLeft = (int) (rightBorder - sw * 0.24);
            wallTopY = (int) (bottomBorder - innerH * 0.45);
        }

        int bomW = Math.max(1, rightBorder - tbLeft);

        // 3. 在 [tbLeft .. rightBorder] 内向上扫描水平分隔线，确认主明细表顶边
        List<Integer> mainHLines = new ArrayList<>();
        for (int y = bottomBorder - 2; y > topBorder + 2; y--) {
            int hcnt = 0;
            for (int x = tbLeft + 2; x < rightBorder - 2; x++) {
                if (g.get(x, y) < DARK_THRESHOLD) {
                    hcnt++;
                }
            }
            if (hcnt >= bomW * 0.68) {
                if (mainHLines.isEmpty() || (mainHLines.get(mainHLines.size() - 1) - y) > 3) {
                    mainHLines.add(y);
                }
            }
        }

        int mainBomTop = wallTopY;
        int prevY = bottomBorder;
        for (int y : mainHLines) {
            double gapFrac = (double) (prevY - y) / innerH;
            double maxAllowed = prevY > bottomBorder - innerH * 0.34 ? 0.22 : 0.08;
            if (gapFrac > maxAllowed) {
                break;
            }
            mainBomTop = Math.min(mainBomTop, y);
            prevY = y;
        }

        int padX = Math.max(5, (int) (bomW * 0.04));
        int padY = Math.max(5, (int) (innerH * 0.018));

        List<NamedBox> slices = new ArrayList<>();
        int rx0 = clamp((tbLeft - padX) * step, 0, fullW);
        int ry0 = clamp((mainBomTop - padY) * step, 0, fullH);
        int rx1 = clamp((rightBorder + padX) * step, 0, fullW);
        int ry1 = clamp((bottomBorder + padY) * step, 0, fullH);
        addPossiblyChunkedBox(slices, "main", rx0, ry0, rx1, ry1);

        // 4. 检测标题栏/主明细表左侧的“续表”（紧邻 tbLeft 左侧）
        int contSearchX0 = Math.max(0, (int) (tbLeft - bomW * 1.15));
        int contSearchX1 = Math.max(contSearchX0 + 1, tbLeft - 3);
        int probeX0 = Math.max(0, (int) (tbLeft - bomW * 0.80));
        int probeW = Math.max(1, contSearchX1 - probeX0);
        List<Integer> contHLines = new ArrayList<>();
        for (int y = bottomBorder - 2; y > topBorder + 2; y--) {
            int hcnt = 0;
            for (int x = probeX0; x < contSearchX1; x++) {
                if (g.get(x, y) < DARK_THRESHOLD) {
                    hcnt++;
                }
            }
            if (hcnt >= probeW * 0.72) {
                if (contHLines.isEmpty() || (contHLines.get(contHLines.size() - 1) - y) > 3) {
                    contHLines.add(y);
                }
            }
        }

        if (contHLines.size() >= 5) {
            Collections.sort(contHLines);
            int maxPitch = Math.max(8, (int) (innerH * 0.045));
            List<int[]> clusters = new ArrayList<>();
            int cStart = contHLines.get(0);
            int cPrev = cStart;
            int cCount = 1;
            for (int i = 1; i < contHLines.size(); i++) {
                int y = contHLines.get(i);
                if (y - cPrev <= maxPitch) {
                    cPrev = y;
                    cCount++;
                } else {
                    if (cCount >= 5) {
                        clusters.add(new int[]{cStart, cPrev});
                    }
                    cStart = y;
                    cPrev = y;
                    cCount = 1;
                }
            }
            if (cCount >= 5) {
                clusters.add(new int[]{cStart, cPrev});
            }

            int ci = 0;
            for (int[] cluster : clusters) {
                int cy0 = cluster[0];
                int cy1 = cluster[1];
                int chPx = Math.max(1, cy1 - cy0);
                if (chPx < innerH * 0.08) {
                    continue;
                }
                List<Integer> vCols = new ArrayList<>();
                boolean inV = false;
                for (int x = contSearchX0; x < contSearchX1; x++) {
                    int vcnt = 0;
                    for (int y = cy0; y < cy1; y++) {
                        if (g.minHoriz3(x, y) < DARK_THRESHOLD) {
                            vcnt++;
                        }
                    }
                    if (vcnt >= chPx * 0.75) {
                        if (!inV) {
                            vCols.add(x);
                            inV = true;
                        }
                    } else {
                        inV = false;
                    }
                }
                if (vCols.size() >= 4) {
                    ci++;
                    int cx0 = clamp((vCols.get(0) - padX) * step, 0, fullW);
                    int cy0f = clamp((cy0 - padY) * step, 0, fullH);
                    int cx1 = clamp((tbLeft + padX) * step, 0, fullW);
                    int cy1f = clamp((cy1 + padY) * step, 0, fullH);
                    addPossiblyChunkedBox(slices, "cont" + ci, cx0, cy0f, cx1, cy1f);
                }
            }
        }
        return slices;
    }

    private void addPossiblyChunkedBox(List<NamedBox> slices, String prefix, int x0, int y0, int x1, int y1) {
        if (x1 - x0 < 200 || y1 - y0 < 200) {
            return;
        }
        if (y1 - y0 <= MAX_SLICE_HEIGHT_FULL) {
            slices.add(new NamedBox(prefix, x0, y0, x1, y1));
            return;
        }
        int curY1 = y1;
        int part = 1;
        while (curY1 > y0 + CHUNK_OVERLAP_FULL) {
            int curY0 = Math.max(y0, curY1 - CHUNK_HEIGHT_FULL);
            if (curY0 - y0 < 550) {
                curY0 = y0;
            }
            slices.add(new NamedBox(prefix + "-p" + part, x0, curY0, x1, curY1));
            if (curY0 == y0) {
                break;
            }
            curY1 = curY0 + CHUNK_OVERLAP_FULL;
            part++;
        }
    }

    /** 评估四个角块的表格线条与墨迹密度，找到国标标题栏（应位于右下角）对应的逆时针旋转角度。 */
    private int detectCadRotationCcw(GrayGrid g) {
        int w = g.width;
        int h = g.height;
        int mx = (int) (w * 0.04);
        int my = (int) (h * 0.04);
        int cw = Math.max(10, (int) (w * 0.28));
        int ch = Math.max(10, (int) (h * 0.28));

        double s0 = cornerScore(g, w - mx - cw, h - my - ch, w - mx, h - my);
        double s90 = cornerScore(g, mx, h - my - ch, mx + cw, h - my);
        double s180 = cornerScore(g, mx, my, mx + cw, my + ch);
        double s270 = cornerScore(g, w - mx - cw, my, w - mx, my + ch);

        double best = s0;
        int bestDeg = 0;
        if (s90 > best) {
            best = s90;
            bestDeg = 90;
        }
        if (s180 > best) {
            best = s180;
            bestDeg = 180;
        }
        if (s270 > best) {
            bestDeg = 270;
        }
        return bestDeg;
    }

    private double cornerScore(GrayGrid g, int x0, int y0, int x1, int y1) {
        int sx0 = clamp(x0, 0, g.width - 1);
        int sy0 = clamp(y0, 0, g.height - 1);
        int sx1 = clamp(x1, sx0 + 1, g.width);
        int sy1 = clamp(y1, sy0 + 1, g.height);
        int cw = sx1 - sx0;
        int ch = sy1 - sy0;
        int dark = 0;
        int hRuns = 0;
        int vRuns = 0;
        for (int y = sy0; y < sy1; y++) {
            int rowDark = 0;
            for (int x = sx0; x < sx1; x++) {
                if (g.get(x, y) < 160) {
                    rowDark++;
                }
            }
            dark += rowDark;
            if (rowDark >= cw * 0.35) {
                hRuns++;
            }
        }
        for (int x = sx0; x < sx1; x++) {
            int colDark = 0;
            for (int y = sy0; y < sy1; y++) {
                if (g.get(x, y) < 160) {
                    colDark++;
                }
            }
            if (colDark >= ch * 0.35) {
                vRuns++;
            }
        }
        return ((double) dark / Math.max(1, cw * ch)) * 100.0 + hRuns * 0.6 + vRuns * 0.6;
    }

    /**
     * 直接在原始大图中按逆映射定位子区域，仅复制并旋转该局部切片，彻底避免分配整张旋转大图。
     */
    private BufferedImage extractRotatedScaledCrop(BufferedImage raw, int ccwDeg, int ux0, int uy0, int ux1, int uy1,
            int maxEdge) {
        int rawW = raw.getWidth();
        int rawH = raw.getHeight();
        int rx0;
        int ry0;
        int rx1;
        int ry1;
        if (ccwDeg == 0) {
            rx0 = ux0;
            ry0 = uy0;
            rx1 = ux1;
            ry1 = uy1;
        } else if (ccwDeg == 90) {
            // upright(ux, uy) comes from raw(rx = rawW - 1 - uy, ry = ux)
            rx0 = rawW - uy1;
            rx1 = rawW - uy0;
            ry0 = ux0;
            ry1 = ux1;
        } else if (ccwDeg == 180) {
            // upright(ux, uy) comes from raw(rx = rawW - 1 - ux, ry = rawH - 1 - uy)
            rx0 = rawW - ux1;
            rx1 = rawW - ux0;
            ry0 = rawH - uy1;
            ry1 = rawH - uy0;
        } else {
            // 270 CCW (= 90 CW): upright(ux, uy) comes from raw(rx = uy, ry = rawH - 1 -
            // ux)
            rx0 = uy0;
            rx1 = uy1;
            ry0 = rawH - ux1;
            ry1 = rawH - ux0;
        }

        rx0 = clamp(rx0, 0, rawW - 1);
        ry0 = clamp(ry0, 0, rawH - 1);
        rx1 = clamp(rx1, rx0 + 1, rawW);
        ry1 = clamp(ry1, ry0 + 1, rawH);

        BufferedImage sub = raw.getSubimage(rx0, ry0, rx1 - rx0, ry1 - ry0);
        BufferedImage upright = ccwDeg == 0 ? copyAsRgb(sub) : rotateCcw(sub, ccwDeg);
        if (Math.max(upright.getWidth(), upright.getHeight()) <= maxEdge) {
            return upright;
        }
        try {
            double scale = (double) maxEdge / Math.max(upright.getWidth(), upright.getHeight());
            int nw = Math.max(1, (int) Math.round(upright.getWidth() * scale));
            int nh = Math.max(1, (int) Math.round(upright.getHeight() * scale));
            BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaled.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.drawImage(upright, 0, 0, nw, nh, null);
            } finally {
                g2d.dispose();
            }
            return scaled;
        } finally {
            upright.flush();
        }
    }

    private BufferedImage copyAsRgb(BufferedImage src) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dst.createGraphics();
        try {
            g2d.drawImage(src, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return dst;
    }

    private BufferedImage rotateCcw(BufferedImage src, int ccwDeg) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (ccwDeg == 0) {
            return copyAsRgb(src);
        }
        int nw = (ccwDeg == 90 || ccwDeg == 270) ? h : w;
        int nh = (ccwDeg == 90 || ccwDeg == 270) ? w : h;
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dst.createGraphics();
        try {
            if (ccwDeg == 90) {
                g2d.translate(0, w);
                g2d.rotate(-Math.PI / 2.0);
            } else if (ccwDeg == 180) {
                g2d.translate(w, h);
                g2d.rotate(Math.PI);
            } else if (ccwDeg == 270) {
                g2d.translate(h, 0);
                g2d.rotate(Math.PI / 2.0);
            }
            g2d.drawImage(src, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return dst;
    }

    /** 块最小值降采样（保留 CAD 细表格线不发虚，同时极大降低内存占用）。 */
    private GrayGrid buildMinPoolGray(BufferedImage image, int targetMaxEdge) {
        int w = image.getWidth();
        int h = image.getHeight();
        int step = Math.max(1, (int) Math.round((double) Math.max(w, h) / targetMaxEdge));
        int sw = Math.max(1, w / step);
        int sh = Math.max(1, h / step);
        byte[] data = new byte[sw * sh];
        int[] rowBuf = new int[w];
        for (int sy = 0; sy < sh; sy++) {
            int y0 = sy * step;
            int y1 = Math.min(h, y0 + step);
            int[] minRow = new int[sw];
            for (int i = 0; i < sw; i++) {
                minRow[i] = 255;
            }
            for (int y = y0; y < y1; y++) {
                image.getRGB(0, y, w, 1, rowBuf, 0, w);
                for (int sx = 0; sx < sw; sx++) {
                    int x0 = sx * step;
                    int x1 = Math.min(w, x0 + step);
                    int m = minRow[sx];
                    for (int x = x0; x < x1; x++) {
                        int rgb = rowBuf[x];
                        int r = (rgb >> 16) & 0xFF;
                        int gg = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        int lum = (r * 77 + gg * 150 + b * 29) >> 8;
                        if (lum < m) {
                            m = lum;
                            if (m == 0) {
                                break;
                            }
                        }
                    }
                    minRow[sx] = m;
                }
            }
            int base = sy * sw;
            for (int sx = 0; sx < sw; sx++) {
                data[base + sx] = (byte) minRow[sx];
            }
        }
        return new GrayGrid(sw, sh, step, data);
    }

    private GrayGrid rotateGridCcw(GrayGrid src, int ccwDeg) {
        if (ccwDeg == 0) {
            return src;
        }
        int w = src.width;
        int h = src.height;
        int nw = (ccwDeg == 90 || ccwDeg == 270) ? h : w;
        int nh = (ccwDeg == 90 || ccwDeg == 270) ? w : h;
        byte[] out = new byte[nw * nh];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                byte v = src.data[y * w + x];
                int nx;
                int ny;
                if (ccwDeg == 90) {
                    nx = y;
                    ny = w - 1 - x;
                } else if (ccwDeg == 180) {
                    nx = w - 1 - x;
                    ny = h - 1 - y;
                } else {
                    nx = h - 1 - y;
                    ny = x;
                }
                out[ny * nw + nx] = v;
            }
        }
        return new GrayGrid(nw, nh, src.step, out);
    }

    /** 读取 JPEG APP1 EXIF Orientation 标签（1/3/6/8），非 JPEG 或无 EXIF 返回 1。 */
    static int readJpegExifOrientation(byte[] data) {
        if (data == null || data.length < 12 || (data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
            return 1;
        }
        int offset = 2;
        while (offset + 4 <= data.length) {
            if ((data[offset] & 0xFF) != 0xFF) {
                break;
            }
            int marker = data[offset + 1] & 0xFF;
            offset += 2;
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }
            if (offset + 2 > data.length) {
                break;
            }
            int length = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
            if (length < 2 || offset + length > data.length) {
                break;
            }
            if (marker == 0xE1 && length >= 14
                    && data[offset + 2] == 'E' && data[offset + 3] == 'x'
                    && data[offset + 4] == 'i' && data[offset + 5] == 'f'
                    && data[offset + 6] == 0 && data[offset + 7] == 0) {
                int tiffStart = offset + 8;
                boolean littleEndian;
                if (data[tiffStart] == 'I' && data[tiffStart + 1] == 'I') {
                    littleEndian = true;
                } else if (data[tiffStart] == 'M' && data[tiffStart + 1] == 'M') {
                    littleEndian = false;
                } else {
                    return 1;
                }
                int ifdOffset = readInt32(data, tiffStart + 4, littleEndian);
                int ifdStart = tiffStart + ifdOffset;
                if (ifdStart < tiffStart || ifdStart + 2 > offset + length) {
                    return 1;
                }
                int entryCount = readUint16(data, ifdStart, littleEndian);
                for (int i = 0; i < entryCount; i++) {
                    int entry = ifdStart + 2 + i * 12;
                    if (entry + 12 > offset + length) {
                        break;
                    }
                    int tag = readUint16(data, entry, littleEndian);
                    if (tag == 0x0112) {
                        return readUint16(data, entry + 8, littleEndian);
                    }
                }
                return 1;
            }
            offset += length;
        }
        return 1;
    }

    static int exifOrientationToCcwDegrees(int orientation) {
        switch (orientation) {
            case 3 :
                return 180;
            case 6 :
                return 270; // 90 CW = 270 CCW
            case 8 :
                return 90; // 90 CCW
            default :
                return 0;
        }
    }

    private static int readUint16(byte[] data, int pos, boolean littleEndian) {
        if (littleEndian) {
            return (data[pos] & 0xFF) | ((data[pos + 1] & 0xFF) << 8);
        }
        return ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
    }

    private static int readInt32(byte[] data, int pos, boolean littleEndian) {
        if (littleEndian) {
            return (data[pos] & 0xFF)
                    | ((data[pos + 1] & 0xFF) << 8)
                    | ((data[pos + 2] & 0xFF) << 16)
                    | ((data[pos + 3] & 0xFF) << 24);
        }
        return ((data[pos] & 0xFF) << 24)
                | ((data[pos + 1] & 0xFF) << 16)
                | ((data[pos + 2] & 0xFF) << 8)
                | (data[pos + 3] & 0xFF);
    }

    private byte[] encodePng(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", out)) {
                throw new IOException("PNG 编码失败");
            }
            return out.toByteArray();
        }
    }

    private static String safeContentType(String contentType) {
        return (contentType == null || contentType.trim().isEmpty()) ? "image/png" : contentType;
    }

    private static String stripExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "drawing";
        }
        String trimmed = filename.trim();
        int dot = trimmed.lastIndexOf('.');
        return dot > 0 ? trimmed.substring(0, dot) : trimmed;
    }

    private static String replaceExtensionWithPng(String filename) {
        return stripExtension(filename) + ".png";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class NamedBox {
        private final String name;
        private final int x0;
        private final int y0;
        private final int x1;
        private final int y1;

        private NamedBox(String name, int x0, int y0, int x1, int y1) {
            this.name = name;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
    }

    private static class GrayGrid {
        private final int width;
        private final int height;
        private final int step;
        private final byte[] data;

        private GrayGrid(int width, int height, int step, byte[] data) {
            this.width = width;
            this.height = height;
            this.step = step;
            this.data = data;
        }

        private int get(int x, int y) {
            return data[y * width + x] & 0xFF;
        }

        private int minHoriz3(int x, int y) {
            int m = get(x, y);
            if (x > 0) {
                m = Math.min(m, get(x - 1, y));
            }
            if (x + 1 < width) {
                m = Math.min(m, get(x + 1, y));
            }
            return m;
        }

        private int minVert3(int x, int y) {
            int m = get(x, y);
            if (y > 0) {
                m = Math.min(m, get(x, y - 1));
            }
            if (y + 1 < height) {
                m = Math.min(m, get(x, y + 1));
            }
            return m;
        }

        private boolean hasDarkInHoriz5(int x, int y, int threshold) {
            for (int dx = -2; dx <= 2; dx++) {
                int nx = clamp(x + dx, 0, width - 1);
                if (get(nx, y) < threshold) {
                    return true;
                }
            }
            return false;
        }
    }
}
