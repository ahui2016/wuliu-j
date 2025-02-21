# wuliu-j

Java version of Wuliu File Manager 

## Java Commands

- `javac -cp ".;classes/*" wuliu_j/common/*.java wuliu_j/tools/*.java`
- `java -cp ".;classes/*" wuliu_j.tools.MetaToSimple`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuDB`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuAdd`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuEditMeta`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuSearch`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuChecksum`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuLabelNotes`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuOverwrite`


## 从旧版 wuliu 升级

**特別注意**

- 執行 MetaToSimple 之前, 請先執行 `wuliu-checksum` 檢查舊專案及其備份專案,
  因為執行 MetaToSimple 會更改摘要算法, 會重新計算摘要。
- 如果有備份專案, 請先備份, 因為升級後會忘記舊的備份狀態, 一律當作已完整備份。

升級操作:

1. 如果你已在使用 <https://github.com/ahui2016/wuliu>
2. 那么，可下载 <https://github.com/ahui2016/wuliu-j>, 把 wuliu-j 中的
   wuliu_j, classes, simplemeta 三个资料夹复制到旧项目的根目录下
3. 执行 `java -cp ".;classes/*" wuliu_j.tools.MetaToSimple`
   把 metadata 資料夾中的 json 檔案轉換為 simplemeta。
4. 執行 `java -cp ".;classes/*" wuliu_j.tools.WuliuDB -init` 生成 wuliu_j.db
5. 如果有備份專案, 請手動把 wuliu_j.db 和 simplemeta 複製到備份專案中。

经过上述操作，就能开始正常使用 wuliu-j

注意，旧版 wuliu 的核心关键在于 metadata 資料夾中的 json 檔案,
而 wuliu-j 的核心关键在于 simplemeta 資料夾中的 json 檔案, 而这两种 json 档案的结构不同，
因此一旦开始使用 wuliu-j, 就只能使用 simplemeta, 这意味着无法再降级到旧版 wuliu.


## MetaToSimple

- 把舊的 metadata 轉換為新的 simplemeta.
- 其中 label 和 notes 合併為 notes, 而 keywords/collections/albums 則合併為 label.
- checksum 從 SHA-512 改為 SHA-1, 而且 type 也有改變。

**特別注意**

- 執行 MetaToSimple 之前, 請先執行 `wuliu-checksum` 檢查舊專案及其備份專案,
  因為執行 MetaToSimple 會更改摘要算法, 會重新計算摘要。
- 如果有備份專案, 請先備份, 因為升級後會忘記舊的備份狀態, 一律當作已完整備份。


## WuliuSearch

- 只要勾選了 `id`, 則只尋找 id
- 只要勾選了 `like`, 則只列出 like 大於零的檔案
  - 此時 `result limit` 有效，其他設定無效
- 只要勾選了 `size`, 則按照從大到小的順序列出檔案
  - 此時 `result limit` 有效，其他設定無效
- 如果勾選了 `utime`, 那麼搜尋結果按 utime 排序
  - 此時 `date prefix` 是指 utime 的前綴
  - 關於 date prefix 的功能暫時不做
- 如果勾選了 `ctime`, 那麼搜尋結果按 ctime 排序
  - 此時 `date prefix` 是指 ctime 的前綴
  - 關於 ctime 的功能暫時不做
- filename/label/notes 表面上支持自由组合，但暂时只支持：
  - 三者单独搜寻（选其一）
  - 三者一起搜寻（全选，如果只选择其中两个，也视为全选）
  - 如果搜尋框無內容, 則列出最新的檔案
  - 可選擇 ctime 或 utime, 目前只支持 utime


## TODO

- WuliuLabelNotes 增加搜索功能或与 WuliuSearch 合并

