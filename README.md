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

**特別注意**:  
執行 MetaToSimple 之前, 請先執行 `wuliu-checksum` 進行檢查,
因為執行 MetaToSimple 會更改摘要算法。

## TODO

- export/delete 功能整合到简单的 search 页面中
- WuliuSearch 的 date prefix 默认是 utime
  - 但如果选择了 ctime, 那么 date prefix 就是 ctime
  - rename 功能也整合到 search 页面，通过弹出对话框实现
- WuliuEditMeta 和 WuliuRename 啟動時列出最近檔案

