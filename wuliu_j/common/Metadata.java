package wuliu_j.common;

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

    /**
     * 把舊的 metadata 轉換為新的 simplemeta.
     * 其中 label 和 notes 合併為 notes, 而 keywords/collections/albums 則合併為 label.
     * checksum 從 SHA-512 改為 SHA-1, 而且 type 也有改變。
     */
    public Simplemeta toSimple() {
        SortedSet<String> words = new TreeSet<>();
        words.addAll(this.keywords);
        words.addAll(this.collections);
        words.addAll(this.albums);
        String label = String.join(", ", words);
        String notes = (this.label + " " + this.notes).strip();

        Simplemeta simple = new Simplemeta();
        simple.id = this.id;
        simple.filename = this.filename;
        simple.checksum = Simplemeta.getFileSHA1(MyUtil.FILES_PATH.resolve(this.filename));
        simple.size = this.size;
        simple.type = Simplemeta.typeByFilename(this.filename);
        simple.like = this.like;
        simple.label = label;
        simple.notes = notes;
        simple.ctime = this.ctime;
        simple.utime = this.utime;

        return simple;
    }
}
