package wuliu_j.tools;

import wuliu_j.common.MyUtil;

import static wuliu_j.common.MyUtil.SIMPLEMETA_PATH;

public class WuliuDB {
    public static void main(String[] args) {
    }

    static void printHelp() {
        System.out.println("""
            $ java -cp ".;classes/*" wuliu_j.tools.WuliuDB [options]
            options:
            -init 初始化數據庫
            """);
    }

    static void initDB() {
        MyUtil.mkdirIfNotExist(SIMPLEMETA_PATH);

    }
}
