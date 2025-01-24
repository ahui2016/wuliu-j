package wuliu_j.common;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DB {
    private final String path;
    private final Jdbi jdbi;

    public String getPath() {
        return this.path;
    }

    public DB(String dbPath) {
        this.path = dbPath;
        this.jdbi = Jdbi.create("jdbc:sqlite:" + dbPath);
        // this.jdbi.registerRowMapper(ConstructorMapper.factory(Simplemeta.class));
    }

    public void createTables() {
        this.jdbi.useHandle(
    handle -> handle.createScript(Stmt.CREATE_TABLES).execute());
    }

    public void insertSimplemeta(Map<String,Object> meta) {
        jdbi.useHandle(handle -> {
            handle.createUpdate(Stmt.INSERT_SIMPLEMETA)
                    .bindMap(meta).execute();
            handle.createUpdate(Stmt.INSERT_FILE_CHECKED)
                    .bind("id", meta.get("id"))
                    .bind("checked", meta.get("utime"))
                    .bind("damaged", 0)
                    .execute();
        });
    }

    public void insertSimplemeta(Simplemeta meta) {
        insertSimplemeta(meta.toMap());
    }

    public void updateSimplemeta(Simplemeta meta) {
        jdbi.useHandle(handle ->
                handle.createUpdate(Stmt.UPDATE_SIMPLEMETA)
                      .bindMap(meta.toMap()).execute());
    }

    public void deleteSimplemeta(String id) {
        jdbi.useHandle(handle -> {
            handle.createUpdate(Stmt.DELETE_SIMPLEMETA)
                    .bind("id", id).execute();
            handle.createUpdate(Stmt.DELETE_FILE_CHECKED)
                    .bind("id", id).execute();
        });
    }

    public List<String> getRecentLabels(int limit) {
        return jdbi.withHandle(handle ->
                handle.select(Stmt.GET_RECENT_LABELS)
                      .bind("limit", limit)
                      .mapTo(String.class)
                      .list());
    }

    public List<Simplemeta> getByFilenameLimit(String filename, int limit) {
        var result = jdbi.withHandle(handle ->
                handle.select(Stmt.GET_BY_FILENAME_LIMIT)
                        .bind("filename", "%"+filename+"%")
                        .bind("limit", limit)
                        .mapToMap()
                        .list());
        return result.stream().map(Simplemeta::ofMap).toList();
    }

    public List<Simplemeta> getRecentMetaLimit(int limit) {
        var result = jdbi.withHandle(handle ->
                handle.select(Stmt.GET_RECENT_META_LIMIT)
                        .bind("limit", limit)
                        .mapToMap()
                        .list());
        return result.stream().map(Simplemeta::ofMap).toList();
    }

    public Optional<Simplemeta> getMetaByID(String fileID) {
        var opt = jdbi.withHandle(handle ->
                handle.select(Stmt.GET_META_BY_ID)
                        .bind("id", fileID)
                        .mapToMap()
                        .findOne());
        return opt.map(Simplemeta::ofMap);
    }

    public Optional<Simplemeta> getMetaByChecksum(String checksum) {
        var opt = jdbi.withHandle(handle ->
                handle.select(Stmt.GET_META_BY_CHECKSUM)
                      .bind("checksum", checksum)
                      .mapToMap()
                      .findOne());
        return opt.map(Simplemeta::ofMap);
    }

    public void updateMetaPart(Simplemeta metaPart) {
        jdbi.useHandle(handle ->
                handle.createUpdate(Stmt.UPDATE_META_PART)
                        .bindMap(metaPart.toMap())
                        .execute());
    }

    public void updateOverwriteFile(Simplemeta meta) {
        jdbi.useHandle(handle ->
                handle.createUpdate(Stmt.UPDATE_OVERWRITE_FILE)
                        .bindMap(meta.toMap())
                        .execute());
    }

    public long countSimplemeta() {
        return jdbi.withHandle(handle ->
                handle.select(Stmt.COUNT_SIMPLEMETA)
                        .mapTo(long.class)
                        .one());
    }

    public List<String> getIdsNeedCheck(String datetime) {
        return jdbi.withHandle(handle ->
                handle.select(Stmt.GET_IDS_NEED_CHECK)
                        .bind("checked", datetime)
                        .mapTo(String.class)
                        .list());
    }

    public List<String> getDamagedIds() {
        return jdbi.withHandle(handle ->
                handle.select(Stmt.GET_DAMAGED_IDS)
                        .mapTo(String.class)
                        .list());
    }

    public Optional<Integer> getDamagedByID(String fileID) {
        return jdbi.withHandle(handle ->
                handle.select(Stmt.GET_DAMAGED_BY_ID)
                        .mapTo(Integer.class)
                        .findOne());
    }

    public void updateCheckedDamaged(String fileID, String checked, int damaged) {
        jdbi.useHandle(handle ->
                handle.createUpdate(Stmt.UPDATE_CHECKED_DAMAGED)
                        .bind("checked", checked)
                        .bind("damaged", damaged)
                        .bind("id", fileID)
                        .execute());
    }

    public void renewChecked() {
        jdbi.useHandle(handle ->
                handle.createScript(Stmt.DELETE_ALL_FILE_CHECKED).execute());
        jdbi.useHandle(handle -> {
            var allMetas = handle.select(Stmt.GET_ALL_METAS).mapToMap().stream();
            allMetas.forEach(metaMap -> {
                var meta = Simplemeta.ofMap(metaMap);
                handle.createUpdate(Stmt.INSERT_FILE_CHECKED)
                        .bind("id", meta.id)
                        .bind("checked", meta.utime)
                        .bind("damaged", 0)
                        .execute();
            });
        });
    }
}
