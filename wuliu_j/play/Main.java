package wuliu_j.play;

import com.fasterxml.jackson.jr.ob.JSON;

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
        Map<String,Object> metadata = JSON.std.mapFrom(metaJson);
        System.out.println(metadata);
    }

    static String readJsonFile() throws IOException {
        Path jsonFile = Path.of(METADATA, JSON_FILENAME);
        return Files.readString(jsonFile);
    }
}
