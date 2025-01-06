package wuliu_j.play;

import com.fasterxml.jackson.jr.ob.JSON;
import wuliu_j.common.Metadata;
import wuliu_j.common.Simplemeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Main {
    private static final String METADATA = "metadata";
    private static final String SIMPLEMETA = "simplemeta";
    private static final String JSON_FILENAME = "abc.pdf.json";

    public static void main(String[] args) throws IOException {
        String metaJson = readJsonFile();
        Map<String,Object> data = JSON.std.mapFrom(metaJson);
        Simplemeta meta = Metadata.of(data).toSimple();
        writeSimplemeta(meta);
    }

    static String readJsonFile() throws IOException {
        Path jsonFile = Path.of(METADATA, JSON_FILENAME);
        return Files.readString(jsonFile);
    }

    static void writeSimplemeta(Simplemeta meta) throws IOException {
        Path filePath = Path.of(SIMPLEMETA, JSON_FILENAME);
        System.out.printf("write %s", filePath);
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                .write(meta.toMap(), filePath.toFile());
    }
}
