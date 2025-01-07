package wuliu_j.common;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class MyUtil {
    public static final Path SIMPLEMETA_PATH = Path.of("simplemeta");
    public static final Path PROJ_INFO_PATH = Path.of("project.json");
    public static final String WULIU_J_DB = "wuliu_j.db";

    /**
     * 確保 folder 存在, 如果不存在或有同名檔案, 則拋出異常。
     * 如果 folder 存在則無事發生。
     */
    public static void folderMustExists(Path folder) {
        if (Files.notExists(folder) || !Files.isDirectory(folder)) {
            throw new RuntimeException("Not Found Folder: " + folder);
        }
    }

    public static void mkdirIfNotExist(Path folder) {
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

    static List<String> getStrListFromMap(Map<String,Object> data, String key) {
        Object obj = data.get(key);
        if (obj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            var list = (List<String>) obj;
            return list;
        }
        throw new RuntimeException(String.format("%s is not a string list", key));
    }
}
