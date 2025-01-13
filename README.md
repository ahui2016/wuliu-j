# wuliu-j

Java version of Wuliu File Manager 

## Java Commands

- `javac -cp ".;classes/*" wuliu_j/common/*.java wuliu_j/tools/*.java`
- `java -cp ".;classes/*" wuliu_j.tools.MetaToSimple`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuDB`
- `java -cp ".;classes/*" wuliu_j.tools.WuliuAdd`

## MetaToSimple

- 把舊的 metadata 轉換為新的 simplemeta.
- 其中 label 和 notes 合併為 notes, 而 keywords/collections/albums 則合併為 label.
- checksum 從 SHA-512 改為 SHA-1, 而且 type 也有改變。

**特別注意**:  
執行 MetaToSimple 之前, 請先執行 `wuliu-checksum` 進行檢查,
因為執行 MetaToSimple 會更改摘要算法。

