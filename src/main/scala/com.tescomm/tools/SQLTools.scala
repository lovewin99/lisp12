package com.tescomm.tools

/**
 * Created by s on 16-1-12.
 */
object SQLTools {
  /**
   * 将insertStr中的变量替换成 list 中对应的元素
   * @param insertStr insert into table bs values ('#','#','#',#,#,#)
   * @param replaceList Array("1","2","3","4","5","6")
   * @param x 默认元素索引 从0开始
   * @return 返回替换后的结果 insert into table bs values ('1','2','3',4,5,6)
   */
  def replaceSQL(insertStr: String, replaceList: Seq[String], x: Int = 0): String = {
    if (x == replaceList.length) {
      insertStr
    } else {
      replaceSQL(insertStr.replaceFirst("#", replaceList(x)), replaceList, x + 1)
    }
  }
}
