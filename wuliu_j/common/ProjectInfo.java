package wuliu_j.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectInfo {
    String RepoName;            // 用于判断资料夹是否 Wuliu 专案
    String ProjectName;         // 备份时要求专案名称相同
    boolean IsBackup;           // 是否副本（副本禁止添加、删除等）
    List<String> Projects;      // 第一个是主专案，然后是备份专案
    List<String> LastBackupAt;  // 上次备份时间
    Integer CheckInterval;      // 检查完整性, 单位: day
    Integer CheckSizeLimit;     // 检查完整性, 单位: MB
    Integer ExportSizeLimit;    // 導出檔案體積上限，單位: MB

    public static ProjectInfo fromJsonFile(Path jsonPath) throws IOException {
        Map<String,Object> data = MyUtil.readJsonFileToMap(jsonPath);
        return of(data);
    }

    public static ProjectInfo of(Map<String,Object> data) {
        ProjectInfo info = new ProjectInfo();
        info.RepoName = (String) data.get("RepoName");
        info.ProjectName = (String) data.get("ProjectName");
        info.IsBackup = (boolean) data.get("IsBackup");
        info.Projects = MyUtil.getStrListFromMap(data, "Projects");
        info.LastBackupAt = MyUtil.getStrListFromMap(data, "LastBackupAt");
        info.CheckInterval = MyUtil.getIntFromMap(data, "CheckInterval");
        info.CheckSizeLimit = MyUtil.getIntFromMap(data, "CheckSizeLimit");
        info.ExportSizeLimit = MyUtil.getIntFromMap(data, "CheckSizeLimit");
        return info;
    }

    public LinkedHashMap<String, Object> toMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.putLast("RepoName", RepoName);
        map.putLast("ProjectName", ProjectName);
        map.putLast("IsBackup", IsBackup);
        map.putLast("Projects", Projects);
        map.putLast("LastBackupAt", LastBackupAt);
        map.putLast("CheckInterval", CheckInterval);
        map.putLast("CheckSizeLimit", CheckSizeLimit);
        map.putLast("ExportSizeLimit", ExportSizeLimit);
        return map;
    }
}
