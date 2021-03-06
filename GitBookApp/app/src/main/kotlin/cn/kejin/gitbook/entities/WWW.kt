package cn.kejin.gitbook.entities

import java.io.Serializable

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/29
 */
open class WWW : Serializable{
    companion object {
        /**
         * parse first number string
         */
        fun getNumString(value: String) : String {
            var n = ""
            for(c in value) {
                if (c.isDigit()) {
                    n += c;
                }
                else if (n.isNotEmpty()) {
                    break;
                }
            }

            if (n.isEmpty()) n="0"
            return n
        }
    }
}