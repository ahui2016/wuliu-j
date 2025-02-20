# wuliu-j

Java version of Wuliu File Manager 

## Java Commands

- `javac -cp ".;classes/*" wuliu_j/common/*.java wuliu_j/tools/*.java`
- `java -cp ".;classes/*" wuliu_j.tools.MetaToSimple`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuDB`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuAdd`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuEditMeta`

## MetaToSimple

- 把舊的 metadata 轉換為新的 simplemeta.
- 其中 label 和 notes 合併為 notes, 而 keywords/collections/albums 則合併為 label.
- checksum 從 SHA-512 改為 SHA-1, 而且 type 也有改變。

**特別注意**

執行 MetaToSimple 之前, 請先執行 `wuliu-checksum` 進行檢查,
因為執行 MetaToSimple 會更改摘要算法。


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

- WuliuList 列出全部 label 和 notes

