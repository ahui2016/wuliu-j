package wuliu_j.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.CRC32;

public class Simplemeta {
    public String id;
    public String filename;
    public String checksum; // SHA-1
    public Long size; // the file size, in bytes
    public String type;
    public Integer like;
    public String label;
    public String notes;
    public String ctime;
    public String utime;

    public Simplemeta() {
        // create an empty simplemeta
    }

    public Simplemeta(Path file) {
        this.filename = file.getFileName().toString();
        this.id = Simplemeta.nameToID(this.filename);
        this.checksum = Simplemeta.getFileSHA1(file);
        this.size = file.toFile().length();
        this.type = Simplemeta.typeByFilename(this.filename);
        this.like = 0;
        this.label = "";
        this.notes = "";
        this.ctime = MyUtil.timeNowRFC3339();
        this.utime = this.ctime;
    }

    public static Simplemeta ofMap(Map<String,Object> data) {
        var meta = new Simplemeta();
        meta.readFromMap(data);
        return meta;
    }

    /**
     * 目的: 根据文件名计算出文件 ID, 确保相同的文件名拥有相同的 ID.
     * 實現: 把一个字符串转化为 crc32, 再转化为 36 进制, 作為 ID.
     */
    public static String nameToID(String name) {
        var crc = new CRC32();
        crc.update(name.getBytes());
        return Long.toString(crc.getValue(), 36).toUpperCase();
    }

    /**
     * 讀取 file 的全部內容, 計算其 SHA-1, 轉換為 hex string 返回。
     */
    public static String getFileSHA1(Path file) {
        try {
            var md = MessageDigest.getInstance("SHA-1");
            var hex = HexFormat.of();
            var data = Files.readAllBytes(file);
            var digest = md.digest(data);
            return hex.formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void readFromJsonFile(Path jsonPath) throws IOException {
        var data = MyUtil.readJsonFileToMap(jsonPath);
        this.readFromMap(data);
    }

    public void readFromMap(Map<String,Object> data) {
        this.id = (String) data.get("id");
        this.filename = (String) data.get("filename");
        this.checksum = (String) data.get("checksum");
        this.size = MyUtil.getLongFromMap(data, "size");
        this.type = (String) data.get("type");
        this.like = MyUtil.getIntFromMap(data, "like");
        this.label = (String) data.get("label");
        this.notes = (String) data.get("notes");
        this.ctime = (String) data.get("ctime");
        this.utime = (String) data.get("utime");
    }

    public LinkedHashMap<String,Object> toMap() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.putLast("id", this.id);
        map.putLast("filename", this.filename);
        map.putLast("checksum", this.checksum);
        map.putLast("size", this.size);
        map.putLast("type", this.type);
        map.putLast("like", this.like);
        map.putLast("label", this.label);
        map.putLast("notes", this.notes);
        map.putLast("ctime", this.ctime);
        map.putLast("utime", this.utime);
        return map;
    }

    public boolean isImage() {
        return Simplemeta.isImage(this.type);
    }

    public static boolean isImage(String filetype) {
        return filetype.startsWith("previewable-image");
    }

    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    /**
     * 根據檔名判斷檔案類型。類型是 wuliu-j 私有類型。
     */
    public static String typeByFilename (String name) {
        List<String> imagesSuffix = List.of(
                ".jpg", ".jpeg", ".png", ".gif", ".webp"
        );
        List<String> docsSuffix = List.of(
                ".htm", ".html", ".css", ".json", ".pdf"
        );
        List<String> textSuffix = List.of(
                ".txt", ".md", ".js", ".py", ".go", ".java"
        );
        var suffix = Simplemeta.getFileExtension(name);
        if (imagesSuffix.contains(suffix)) {
            return "previewable-image" + suffix;
        }
        if (docsSuffix.contains(suffix)) {
            return "previewable-docs" + suffix;
        }
        if (textSuffix.contains(suffix)) {
            return "text" + suffix;
        }
        return suffix;
    }
}
