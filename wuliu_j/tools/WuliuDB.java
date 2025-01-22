package wuliu_j.tools;

import wuliu_j.common.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class WuliuDB {
    static ProjectInfo projInfo;

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }
        switch (args[0]) {
            case "-init" -> initDB();
            default      -> printHelp();
        }
    }

    static void printHelp() {
        System.out.println("""
            $ java -cp ".;classes/*" wuliu_j.tools.WuliuDB [options]
            options:
            -init 第一次使用 wuliu_j 時, 初始化數據庫
            """);
    }

    static void loadsProjInfo() {
        try {
            projInfo = ProjectInfo.fromJsonFile(MyUtil.PROJ_INFO_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void initDB() {
        MyUtil.pathMustNotExists(MyUtil.DB_PATH);
        loadsProjInfo();
        MyUtil.checkNotBackup(projInfo);
        MyUtil.mkdirIfNotExists(MyUtil.SIMPLEMETA_PATH);
        DB db = new DB(MyUtil.WULIU_J_DB);
        db.createTables();
        loadsAllSimplemeta(db);
    }

    /**
     * 把 simplemeta 資料夾內的全部 json 導入到數據庫中。
     * 請在調用本函數之前確認數據庫中沒有內容。
     */
    static void loadsAllSimplemeta(DB db) {
        System.out.println("Loads simplemeta to the database...");
        try (var stream = Files.newDirectoryStream(MyUtil.SIMPLEMETA_PATH, "*.json")) {
            stream.forEach(metaPath -> {
                System.out.print(".");
                insertSimplemeta(metaPath, db);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.print("\ndone\n");
    }

    static void insertSimplemeta(Path metaPath, DB db) {
        try {
            var meta = MyUtil.readJsonFileToMap(metaPath);
            db.insertSimplemeta(meta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
