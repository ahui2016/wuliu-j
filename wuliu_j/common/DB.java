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
        jdbi.withHandle(handle ->
                handle.createUpdate(Stmt.INSERT_SIMPLEMETA)
                      .bindMap(meta).execute());
    }

    public void insertSimplemeta(Simplemeta meta) {
        insertSimplemeta(meta.toMap());
    }

    public void updateSimplemeta(Simplemeta meta) {
        jdbi.withHandle(handle ->
                handle.createUpdate(Stmt.UPDATE_SIMPLEMETA)
                      .bindMap(meta.toMap()).execute());
    }

    public void deleteSimplemeta(String id) {
        jdbi.withHandle(handle ->
                handle.createUpdate(Stmt.DELETE_SIMPLEMETA)
                      .bind("id", id).execute());
    }

    public List<String> getRecentLabels(int limit) {
        return jdbi.withHandle(handle ->
                handle.select(Stmt.GET_RECENT_LABELS)
                      .bind("limit", limit)
                      .mapTo(String.class)
                      .list());
    }

    public Optional<Simplemeta> getMetaByChecksum(String checksum) {
        var opt = jdbi.withHandle(handle ->
                handle.select(Stmt.GET_META_BY_CHECKSUM)
                      .bind("checksum", checksum)
                      .mapToMap()
                      .findOne());
        return opt.map(Simplemeta::ofMap);
    }
}
