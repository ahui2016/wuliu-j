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

## WuliuSearch

- 只要勾選了 `id`, 則只尋找 id
- 只要勾選了 `like`, 則只列出 like 大於零的檔案
  - 此時 `result limit` 有效，其他設定無效
- 只要勾選了 `size`, 則按照從大到小的順序列出檔案
  - 此時 `result limit` 有效，其他設定無效
- 如果勾選了 `utime`, 那麼搜尋結果按 utime 排序
  - 此時 `date prefix` 是指 utime 的前綴
- 如果勾選了 `ctime`, 那麼搜尋結果按 ctime 排序
  - 此時 `date prefix` 是指 ctime 的前綴

**特別注意**:  
執行 MetaToSimple 之前, 請先執行 `wuliu-checksum` 進行檢查,
因為執行 MetaToSimple 會更改摘要算法。

## TODO

- export/delete 功能整合到简单的 search 页面中
- WuliuSearch 的 date prefix 默认是 utime
  - 但如果选择了 ctime, 那么 date prefix 就是 ctime
  - rename 功能也整合到 search 页面，通过弹出对话框实现
- WuliuEditMeta 和 WuliuRename 啟動時列出最近檔案

