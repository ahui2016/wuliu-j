package wuliu_j.util;

import com.fasterxml.jackson.jr.ob.JSON;
import wuliu_j.model.Simplemeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MyUtil {

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

    public static Map<String,Object> readJsonFileToMap(Path jsonPath) throws IOException {
        String json = Files.readString(jsonPath);
        return JSON.std.mapFrom(json);
    }

    public static void writeJsonToFilePretty(Map<String,Object> map, File file) throws IOException {
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(map, file);
    }
}
