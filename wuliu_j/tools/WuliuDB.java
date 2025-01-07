package wuliu_j.tools;

import wuliu_j.common.DB;
import wuliu_j.common.MyUtil;
import wuliu_j.common.ProjectInfo;

import java.io.IOException;

import static wuliu_j.common.MyUtil.SIMPLEMETA_PATH;

public class WuliuDB {
    static ProjectInfo projInfo;
    static DB db;

    public static void main(String[] args) throws IOException {
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

    static void initDB() throws IOException {
        projInfo = ProjectInfo.fromJsonFile(MyUtil.PROJ_INFO_PATH);
        MyUtil.checkNotBackup(projInfo);
        MyUtil.mkdirIfNotExist(SIMPLEMETA_PATH);
        db = new DB(MyUtil.WULIU_J_DB);
        db.createTables();
    }
}
