package cn.kejin.gitbook.entities

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/29
 */

class WWWSearchAuthorPage : WWW() {
    var authors : MutableList<WWWAuthor> = mutableListOf()
    var sum_book = "0"
        set(value) {
            field = getNumString(value)
        }

    var sum_author = "0"
        set(value) {
            field = getNumString(value)
        }

    override fun toString(): String {
        var str = "TopicPage ($sum_book:Books, $sum_author:Authors){\n"
        authors.forEach { str += "$it\n" }
        str += "}\n"

        return str
    }
}