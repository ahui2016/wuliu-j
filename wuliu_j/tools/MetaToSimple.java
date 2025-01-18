package wuliu_j.tools;

import wuliu_j.common.Metadata;
import wuliu_j.common.Simplemeta;
import wuliu_j.common.MyUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Convert json file in metadata folder to simplemeta folder.
 * 把舊 Metadata 轉換為新的更簡單的 Simplemeta.
 * 注意: simplemeta 資料來內已存在的 json 檔案會被忽略, 不會被覆蓋。
 */
public class MetaToSimple {
    public static void main(String[] args) {
        final Path metadataPath = Path.of("metadata");
        MyUtil.folderMustExists(metadataPath);
        MyUtil.mkdirIfNotExists(MyUtil.SIMPLEMETA_PATH);

        System.out.println("Convert metadata to simplemeta...");
        try (var stream = Files.newDirectoryStream(metadataPath)) {
            stream.forEach(oldMetaPath -> {
                Path simplemetaPath = getSimplePathFromMeta(oldMetaPath);
                if (Files.exists(simplemetaPath)) {
                    System.out.print("x");
                    return;
                }
                System.out.print(".");
                createSimpleFromMeta(oldMetaPath, simplemetaPath);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.print("\ndone\n");
    }

    static Path getSimplePathFromMeta(Path oldMetaPath) {
        Path filename = oldMetaPath.getFileName();
        return MyUtil.SIMPLEMETA_PATH.resolve(filename);
    }

    static void createSimpleFromMeta(Path oldMetaPath, Path simplemetaPath) {
        try {
            var oldMeta = new Metadata();
            oldMeta.readFromJsonFile(oldMetaPath);
            Simplemeta meta = oldMeta.toSimple();
            MyUtil.writeJsonToFilePretty(meta.toMap(), simplemetaPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
