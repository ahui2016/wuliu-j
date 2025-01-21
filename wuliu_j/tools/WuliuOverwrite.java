package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;
import wuliu_j.common.Simplemeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class WuliuOverwrite {
    private static DB db;

    public static void main(String[] args) throws IOException {
        initAndCheck();
        var files = getBufferFiles();
        for (var file : files) {
            overwriteFile(file, db);
        }
    }

    static void initAndCheck() throws IOException {
        var projInfo = MyUtil.initCheck();
        MyUtil.checkNotBackup(projInfo);
        db = new DB(MyUtil.WULIU_J_DB);
    }

    private static List<Path> getBufferFiles() {
        List<Path> files = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(MyUtil.BUFFER_PATH)) {
            for (var file : stream) {
                if (Files.isDirectory(file)) {
                    System.out.println("自動忽略資料夾: " + file);
                    continue;
                }
                if (Files.isRegularFile(file)) {
                    files.add(file);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    private static void overwriteFile(Path file, DB db) throws IOException {
        var metaPath = MyUtil.getSimplemetaPath(file.getFileName().toString());
        var metaJson = MyUtil.readJsonFileToMap(metaPath);
        var meta = Simplemeta.ofMap(metaJson);

        var checksum = Simplemeta.getFileSHA1(file);
        if (checksum.equals(meta.checksum)) {
            System.out.println("檔案內容沒有變化: " + meta.filename);
            return;
        }
        var result = db.getMetaByChecksum(checksum);
        if (result.isPresent()) {
            var conflict = result.get();
            System.out.println("已存在相同內容的檔案:");
            System.out.println("buffer: " + meta.filename);
            System.out.println("files: " + conflict.filename);
            return;
        }
        meta.checksum = checksum;
        meta.utime = MyUtil.timeNowRFC3339();
        meta.size = Files.size(file);

        System.out.println("files <== " + file);
        var target = MyUtil.FILES_PATH.resolve(file.getFileName());
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        MyUtil.writeJsonToFilePretty(meta.toMap(), metaPath.toFile());
        db.updateOverwriteFile(meta);
    }
}
