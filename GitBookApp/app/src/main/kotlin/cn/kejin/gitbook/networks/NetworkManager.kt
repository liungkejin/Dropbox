package cn.kejin.gitbook.networks

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/8
 */

import cn.kejin.gitbook.UserAccount
import cn.kejin.gitbook.common.debug
import cn.kejin.gitbook.common.error
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

/**
 * Control all network jobs, GitBook REST APIs
 */
class NetworkManager private constructor()// init
{
    companion object {
        val TAG = "NetworkManager"

        val DEF_PAGE_LIMIT = 20

        val RESET_PWD_URL = "https://www.gitbook.com/settings/password/reset"
        val REGISTER_URL = "https://www.gitbook.com/join"
        val BASE_API_URL = "https://api.gitbook.com/"
        val BASE_WWW_URL = "https://www.gitbook.com/"

        val JSON_TYPE = MediaType.parse("application/json; charset:utf-8")

        val DEF_CONNECT_TIMEOUT_SEC = 5

        val instance = NetworkManager()
    }

    private var mHttpClient = OkHttpClient.Builder()
            //            .authenticator({ route, response ->
            //                var value = UserAccount.getToken();
            //                response.request().newBuilder().addHeader("Authorization",
            //                        Credentials.basic(UserAccount.mUser.name, value)).build()
            //            })
            .connectTimeout(DEF_CONNECT_TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .readTimeout((DEF_CONNECT_TIMEOUT_SEC * 2).toLong(), TimeUnit.SECONDS)
            .writeTimeout((DEF_CONNECT_TIMEOUT_SEC * 2).toLong(), TimeUnit.SECONDS).build()

    /**
     * setting okhttpclient 'connect', 'read' and 'write' timeout,
     * the unit is TimeUnit.MILLISECONDS
     * @param connect set connect timeout
     * *
     * @param read set read timeout
     * *
     * @param write set write timeout
     */
    fun setHttpTimeout(connect: Long = mHttpClient.connectTimeoutMillis().toLong(),
                       read: Long = mHttpClient.readTimeoutMillis().toLong(),
                       write: Long = mHttpClient.writeTimeoutMillis().toLong()) {

        mHttpClient = mHttpClient.newBuilder()
                .connectTimeout(connect, TimeUnit.MILLISECONDS)
                .readTimeout(read, TimeUnit.MILLISECONDS)
                .writeTimeout(write, TimeUnit.MICROSECONDS).build();
    }

    /**
     * get absolute URL
     */
    fun getApiAbsUrl(uri: String): String =
            BASE_API_URL.removeSuffix("/") + "/" + uri.removePrefix("/")

    fun getWWWAbsUrl(uri: String): String =
            BASE_WWW_URL.removeSuffix("/") + "/" + uri.removePrefix("/")

    /**
     * get method
     */
    fun <Model> get(uri: String, callback: HttpCallback<Model>, auth: Boolean = false): Call? {
        var url = uri;
        if (!uri.startsWith("http") && !uri.startsWith("https")) {
            url = getApiAbsUrl(uri);
        }

        error(TAG, "Method: GET, URL: $url, Auth: $auth")

        val builder = Request.Builder().url(url).get()
        if (auth) {
            if (!UserAccount.isSignedIn()) {
                callback.onFailure(HttpCallback.E_NOT_SIGN, "not singed")
                return null;
            }

            builder.addHeader("Authorization", UserAccount.getAuthValue());
        }

        var call = mHttpClient.newCall(builder.build());
        call.enqueue(callback)

        return call;
    }

    /**
     * post method
     */
    fun <Model> post(uri: String, json: String,
                     callback: HttpCallback<Model>, auth: Boolean = false): Call? {

        var url = uri;
        if (!uri.startsWith("http") && !uri.startsWith("https")) {
            url = getApiAbsUrl(uri);
        }

        error(TAG, "Method: GET, URL: $url, Auth: $auth, Json: $json")

        val body = RequestBody.create(JSON_TYPE, json);
        val builder = Request.Builder();

        if (auth) {
            if (!UserAccount.isSignedIn()) {
                callback.onFailure(HttpCallback.E_NOT_SIGN, "not singed")
                return null;
            }

            builder.addHeader("Authorization", UserAccount.getAuthValue());
        }

        builder.addHeader("Content-Type", "application/json").url(url).post(body);

        var call = mHttpClient.newCall(builder.build())
        call.enqueue(callback);

        return call;
    }

    /**
     * REST APIs
     */

    /**
     * Sign In to user account
     * Method: GET
     */
    fun signIn(username: String, pwd: String, callback: HttpCallback<Models.MyAccount>): Call {
        val url = getApiAbsUrl("account")
        error(TAG, "URL: $url , UserName: $username, Pwd: $pwd")

        val builder = Request.Builder().url(url).get()
        builder.addHeader("Authorization", Credentials.basic(username, pwd));

        val call = mHttpClient.newCall(builder.build())
        call.enqueue(callback)

        return call
    }

    ////////////////////   Books  //////////////////////////////////////////////////////////
    /**
     * List my books. This includes books from organizations the user can access.
     */
    fun getMyBooks(page: Int, limit: Int = DEF_PAGE_LIMIT, callback: HttpCallback<Models.Books>)
            = get("/books?page=$page&limit=$limit", callback, true)

    /**
     * List books by myself
     */
    fun getMyAuthBooks(page: Int, limit: Int = DEF_PAGE_LIMIT, callback: HttpCallback<Models.Books>)
            = get("/books/author?page=$page&limit=$limit", callback, true)

    /**
     * get all public books
     */
    fun getPublicBooks(page: Int, limit: Int = DEF_PAGE_LIMIT, callback: HttpCallback<Models.Books>)
            = get("/books/all?page=$page&limit=$limit", callback)

    /**
     * get details about a book
     */
    fun getBookDetail(id: String, callback: HttpCallback<Models.ABookDetail>) = get("/book/$id", callback)

    ///////////////////// Authors ///////////////////////////////////////////////////////////
    /**
     * get details about author
     */
    fun getAuthorDetail(name: String, callback: HttpCallback<Models.Account>) = get("/author/$name", callback)

    /**
     * get author's avatar url
     */
    fun getAuthorAvatarUrl(name: String) = getApiAbsUrl("/author/$name/avatar")

    ////////////////////// Topics ///////////////////////////////////////////////////////////
    /**
     * get all topics
     */
    fun getAllTopics(page: Int, limit: Int = DEF_PAGE_LIMIT, callback: HttpCallback<Models.Topics>)
            = get("/topics?page=$page&limit=$limit", callback)

    /**
     * search topics of 'key'
     */
    fun searchTopics(key: String, page: Int, limit: Int = DEF_PAGE_LIMIT, callback: HttpCallback<Models.Topics>)
            = get("/topics?key=$key&page=$page&limit=$limit", callback)

    /**
     * get specified topic
     */
    fun getTopic(id: String, callback: HttpCallback<Models.ATopic>)
            = get("/topic/$id", callback)


    //////////////////// Versions //////////////////////////////////////////////////////////////
    /**
     * get all branches for a book
     */
    fun getBookBranches(id: String, callback: HttpCallback<List<Models.ABranch>>)
            = get("/book/$id/versions/branches", callback)

    fun getBookVersionTags(id: String, callback: HttpCallback<List<Models.ABranch>>)
            = get("/book/$id/versions/tags", callback)


    //////////////////// Contents //////////////////////////////////////////////////////////////
    fun getBookContents(id: String,
                        file: String,
                        lang: String = "",
                        callback: HttpCallback<Models.BookContents>): Call? {

        var filename = file;
        var index = file.lastIndexOf('.')
        if (index > 0) {
            filename.removeRange(index..file.length - 1)
            filename += ".json"
        }

        var url = "/book/$id/contents/"
        if (!lang.isNullOrEmpty()) {
            url += "/$lang/";
        }
        url += filename

        return get(url, callback)
    }

    fun getBookVersionContents(id: String, version: String, file: String, lang: String = "",
                               callback: HttpCallback<Models.BookContents>): Call? {

        var filename = file;
        var index = file.lastIndexOf('.')
        if (index > 0) {
            filename.removeRange(index..file.length - 1)
            filename += ".json"
        }

        var url = "/book/$id/contents/"
        if (!lang.isNullOrEmpty()) {
            url += "/$lang/";
        }
        url += "v/$version/$filename"

        return get(url, callback)
    }


    //////////////////////// WWW API ///////////////////////////////////////////////////////////
    /**
     * parse a book html
     */
    fun parseABook(e: Element): Models.WWWBook {
        var book = Models.WWWBook();
        val header = e.getElementsByClass("book-header")
        if (header.isNotEmpty()) {
            val href = header[0].getElementsByTag("a")
            if (href.isNotEmpty()) {
                book.details = href[0].attr("href");
                book.title = href[0].text();
            }
        }

        val meta = e.getElementsByClass("book-meta")
        if (meta.isNotEmpty()) {
            val info = meta[0].getElementsByTag("span");
            if (info.size == 2) {
                book.star_num = info[0].text();
                book.pub_time = info[1].text();
            }
        }

        val summary = e.getElementsByClass("book-summary");
        if (summary.isNotEmpty()) {
            book.summary = summary[0].text()
        }

        val author = e.getElementsByClass("book-footer");
        if (author.isNotEmpty()) {
            val links = author[0].getElementsByTag("a");
            if (links.size == 2) {
                book.author.avatar = links[0].getElementsByTag("img").attr("src")
                book.author.url = links[1].attr("href")
                book.author.name = links[1].text()
            }
        }

        return book;
    }


    fun parseATopic(e: Element): Models.WWWTopic {
        var topic = Models.WWWTopic();

        val a = e.getElementsByTag("a");
        if (a.isNotEmpty()) {
            topic.url = a[0].attr("href")
            topic.name = a[0].getElementsByClass("name").text()
            topic.num = a[0].getElementsByClass("count").text()
        }

        return topic;
    }

    fun parseExplorePage(body: String): Models.WWWExplorePage {
        val ePage = Models.WWWExplorePage()

        val doc = Jsoup.parse(body)
        val books = doc.getElementsByClass("book");
        for (book in books) {
            ePage.books.add(parseABook(book));
        }

        val topicsList = doc.getElementsByClass("topics-list")
        if (topicsList.isNotEmpty()) {
            for (topic in topicsList[0].getElementsByTag("li")) {
                ePage.topics.add(parseATopic(topic));
            }
        }

        return ePage
    }

    fun getExplorePage(page: Int, callback: HttpCallback<Models.WWWExplorePage>): Call? {
        val url = getWWWAbsUrl("/explore?page=$page")

        return get(url, object : HttpCallback<Models.WWWExplorePage>(Models.WWWExplorePage::class.java) {
            override fun onSuccess(body: String) {

                val ePage = parseExplorePage(body)
                post { onResponse(true, ePage) }
            }

            override fun onResponse(success: Boolean, model: Models.WWWExplorePage?, code: Int, msg: String) {
                callback.onResponse(success, model, code, msg)
            }
        })
    }

    fun parseTopicsPage(body: String): Models.WWWTopicsPage {
        val page = Models.WWWTopicsPage()

        val doc = Jsoup.parse(body)
        val topicsList = doc.getElementsByClass("topics-byname");
        topicsList.forEach {
            it.children().forEach {
                topic ->
                val t = Models.WWWTopic();
                if (topic.className() == "entry letter") {
                    t.letter = topic.text()
                }
                else {
                    val link = topic.getElementsByTag("a");
                    t.url = link.attr("href");
                    t.name = link.text();

                    t.num = topic.getElementsByTag("span").text();
                }

                page.topics.add(t)
            }
        }

        return page
    }

    fun getTopicsPage(callback: HttpCallback<Models.WWWTopicsPage>): Call? {
        val url = getWWWAbsUrl("/explore/topics")

        return get(url, object : HttpCallback<Models.WWWTopicsPage>(Models.WWWTopicsPage::class.java) {
            override fun onSuccess(body: String) {
                val page = parseTopicsPage(body)
                post { onResponse(true, page) }
            }

            override fun onResponse(success: Boolean, model: Models.WWWTopicsPage?, code: Int, msg: String) {
                callback.onResponse(success, model, code, msg)
            }
        })
    }

    enum class SearchSort { default, stars, updated }

    fun parseAAuthor(author: Element): Models.WWWAuthor {
        val w = Models.WWWAuthor();
        w.avatar = author.getElementsByTag("img").attr("src")
        val infos = author.getElementsByClass("user-infos");
        if (infos.isNotEmpty()) {
            val tag = infos[0].getElementsByTag("a");
            w.url = tag.attr("href");
            w.name = tag.text();
        }

        w.join_time = author.getElementsByTag("p").text()

        return w;
    }

    fun parseBookSearchPage(body: String) : Models.WWWSearchBookPage {
        val page = Models.WWWSearchBookPage()

        val doc = Jsoup.parse(body)
        val head = doc.getElementsByClass("pagehead")
        if (head.isNotEmpty()) {
            val menu = head[0].getElementsByClass("menu")
            if (menu.isNotEmpty()) {
                val m = menu[0].getElementsByTag("a")
                if (m.size == 2) {
                    page.sum_book = m[0].text()
                    page.sum_author = m[1].text()
                }
            }
        }

        doc.getElementsByClass("book").forEach { page.books.add(parseABook(it)) };

        return page;
    }

    fun parseAuthorSearchPage(body: String) : Models.WWWSearchAuthorPage {
        val page = Models.WWWSearchAuthorPage()

        val doc = Jsoup.parse(body)
        val head = doc.getElementsByClass("pagehead")
        if (head.isNotEmpty()) {
            val menu = head[0].getElementsByClass("menu")
            if (menu.isNotEmpty()) {
                val m = menu[0].getElementsByTag("a")
                if (m.size == 2) {
                    page.sum_book = m[0].text()
                    page.sum_author = m[1].text()
                }
            }
        }

        doc.getElementsByClass("user-inner").forEach {
            page.authors.add(parseAAuthor(it))
        };

        return page;
    }

    fun getSearchBookPage(key: String, sort: SearchSort,
                          callback: HttpCallback<Models.WWWSearchBookPage>): Call? {
        val url = getWWWAbsUrl("/search?q=$key&sort=$sort&type=books")

        return get(url, object : HttpCallback<Models.WWWSearchBookPage>(Models.WWWSearchBookPage::class.java) {
            override fun onSuccess(body: String) {
                val page = parseBookSearchPage(body)
                post { onResponse(true, page) }
            }

            override fun onResponse(success: Boolean, model: Models.WWWSearchBookPage?, code: Int, msg: String) {
                callback.onResponse(success, model, code, msg)
            }
        })
    }

    fun getSearchAuthorPage(key: String, sort: SearchSort,
                            callback: HttpCallback<Models.WWWSearchAuthorPage>): Call? {
        var csort = sort;
        if (sort == SearchSort.stars) {
            csort = SearchSort.default;
        }

        val url = getWWWAbsUrl("/search?q=$key&sort=$csort&type=authors")

        return get(url, object : HttpCallback<Models.WWWSearchAuthorPage>(Models.WWWSearchAuthorPage::class.java) {
            override fun onSuccess(body: String) {
                val page = parseAuthorSearchPage(body)
                post { onResponse(true, page) }
            }

            override fun onResponse(success: Boolean, model: Models.WWWSearchAuthorPage?, code: Int, msg: String) {
                callback.onResponse(success, model, code, msg)
            }
        })
    }
}
