package wuliu_j.common;

import com.fasterxml.jackson.jr.ob.JSON;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MyUtil {
    public static final DateTimeFormatter RFC3339 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
    public static final Path INPUT_PATH = Path.of("input");
    public static final Path FILES_PATH = Path.of("files");
    public static final Path SIMPLEMETA_PATH = Path.of("simplemeta");
    public static final Path PROJ_INFO_PATH = Path.of("project.json");
    public static final String WULIU_J_DB = "wuliu_j.db";
    public static final Path DB_PATH = Path.of(WULIU_J_DB);

    public static final Font FONT_BOLD_20 = new Font("sanserif", Font.BOLD, 20);
    public static final Font FONT_18 = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    public static final Font FONT_16 = new Font(Font.SANS_SERIF, Font.PLAIN, 16);

    /**
     * 確保 folder 存在, 如果不存在或有同名檔案, 則拋出異常。
     * 如果 folder 存在則無事發生。
     */
    public static void folderMustExists(Path folder) {
        if (Files.notExists(folder) || !Files.isDirectory(folder)) {
            throw new RuntimeException("Not Found Folder: " + folder);
        }
    }

    /**
     * 確保 path 不存在, 如果存在則拋出異常。 如果 path 不存在則無事發生。
     */
    public static void pathMustNotExists(Path path) {
        if (Files.exists(path)) {
            System.out.printf("檔案已存在: " + path);
            System.exit(0);
        }
    }

    public static void mkdirIfNotExists(Path folder) {
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            return;
        }
        // Files.createDirectory 會檢查是否存在同名檔案。
        // if (Files.isRegularFile(folder)) {}
        try {
            Files.createDirectory(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkNotBackup(ProjectInfo info) {
        if (info.IsBackup) {
            throw new RuntimeException("這是備份專案, 不可使用該功能");
        }
    }

    public static Map<String,Object> readJsonFileToMap(Path jsonPath) throws IOException {
        String json = Files.readString(jsonPath);
        return JSON.std.mapFrom(json);
    }

    public static void writeJsonToFilePretty(Map<String,Object> map, File file) throws IOException {
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(map, file);
    }

    public static Long getLongFromMap(Map<String,Object> map, String key) {
        Number n = (Number) map.get(key);
        return n.longValue();
    }

    public static Integer getIntFromMap(Map<String,Object> map, String key) {
        Number n = (Number) map.get(key);
        return n.intValue();
    }

    public static List<String> getStrListFromMap(Map<String,Object> data, String key) {
        Object obj = data.get(key);
        if (obj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            var list = (List<String>) obj;
            return list;
        }
        throw new RuntimeException(String.format("%s is not a string list", key));
    }

    /**
     * 從 folder 獲取一個檔案, 忽略 folder 中的資料夾。
     */
    public static Path getOneFileFrom(Path folder) {
        try (var paths = Files.list(folder)) {
            var maybe = paths.filter(Files::isRegularFile).findAny();
            if (maybe.isEmpty()) {
                System.err.printf("ERROR! 在%s資料夾中未發現檔案%n", folder);
                System.exit(0);
            }
            return maybe.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String timeNowRFC3339() {
        return OffsetDateTime.now().format(RFC3339);
    }

    /**
     * 讀檔案 file 獲取圖片, 確保該圖片是正方形, 並限制圖片的尺寸。
     */
    public static Image getImageCropLimit(File file, int limit) throws IOException {
        var img = ImageIO.read(file);
        img = Picture.cropCenter(img);
        return Picture.resizeLimit(img, limit);
    }

    public static Path getSimplemetaPath(String filename) {
        return SIMPLEMETA_PATH.resolve(filename + ".json");
    }
}

class Picture {
    /**
     * 確保該圖片是正方形。
     * 如果不是正方形, 則截取其中間部分, 返回正方形的圖片。
     */
    static BufferedImage cropCenter(BufferedImage img) {
        var w = img.getWidth();
        var h = img.getHeight();
        if (w == h) {
            return img;
        }
        int x = 0;
        int y = 0;
        float wF = w;
        float hF = h;
        if (w < h) {
            h = w;
            var yF = (hF - wF) / 2F;
            y = Math.round(yF);
        }
        if (w > h) {
            w = h;
            var xF = (wF - hF) / 2F;
            x = Math.round(xF);
        }
        return img.getSubimage(x, y, w, h);
    }

    /**
     * 使用本函數前請先使用 getImageCropCenter() 確保 img 是正方形的圖片。
     * 當圖片 img 的邊長大於 limit, 則縮小圖片。
     */
    static Image resizeLimit(BufferedImage img, int limit) {
        if (img.getWidth() <= limit) {
            return img;
        }
        return img.getScaledInstance(limit, limit, BufferedImage.SCALE_SMOOTH);
    }
}
