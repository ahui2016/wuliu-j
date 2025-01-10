package wuliu_j.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Metadata extends Simplemeta {
    List<String> keywords;
    List<String> collections;
    List<String> albums;

    @Override
    public void readFromMap(Map<String,Object> data) {
        super.readFromMap(data);
        this.keywords = MyUtil.getStrListFromMap(data, "keywords");
        this.collections = MyUtil.getStrListFromMap(data, "collections");
        this.albums = MyUtil.getStrListFromMap(data, "albums");
    }

    public Simplemeta toSimple() {
        SortedSet<String> words = new TreeSet<>();
        words.addAll(this.keywords);
        words.addAll(this.collections);
        words.addAll(this.albums);
        List<String> wordList = new ArrayList<>(words);
        String notes = String.join(", ", wordList);
        String label = (this.label + " " + this.notes).strip();

        Simplemeta simple = new Simplemeta();
        simple.id = this.id;
        simple.filename = this.filename;
        simple.checksum = this.checksum;
        simple.size = this.size;
        simple.type = this.type;
        simple.like = this.like;
        simple.label = label;
        simple.notes = notes;
        simple.ctime = this.ctime;
        simple.utime = this.utime;

        return simple;
    }
}
