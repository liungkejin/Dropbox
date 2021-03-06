package cn.kejin.gitbook.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import cn.kejin.android.views.ExRecyclerAdapter
import cn.kejin.android.views.ExRecyclerView
import cn.kejin.gitbook.R
import cn.kejin.gitbook.SearchActivity
import cn.kejin.gitbook.TopicBooksActivity
import cn.kejin.gitbook.adapters.BooksAdapter
import cn.kejin.gitbook.base.BaseMainFragment
import cn.kejin.gitbook.common.dpToPx
import cn.kejin.gitbook.common.error
import cn.kejin.gitbook.controllers.PageController
import cn.kejin.gitbook.controllers.PageDriver
import cn.kejin.gitbook.entities.ATopic
import cn.kejin.gitbook.entities.Topics
import cn.kejin.gitbook.entities.WWWExplorePage
import cn.kejin.gitbook.entities.WWWTopic
import cn.kejin.gitbook.navmenu.INavMenu
import cn.kejin.gitbook.networks.*

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/10
 */
class ExploreFragment : BaseMainFragment()
{
    companion object {
        val TAG = "ExploreFragment"

        val ONE_PAGE_BOOKS_NUM = 9
    }

    override fun getLayoutId(): Int = R.layout.fragment_explore
    override fun getMenuLayoutId(): Int = R.menu.menu_explore

    /**
     * Topics Header
     */
    lateinit var topicsAdapter: TopicsAdapter

    lateinit var headerTopicView :View

    /**
     * Main Book List
     */
    lateinit var swipeRefresh : SwipeRefreshLayout

    /**
     * Grid Books
     */
    lateinit var booksList : ExRecyclerView
    val booksAdapter : BooksAdapter
            by lazy { BooksAdapter(activity) }

    /**
     * Page Driver
     */
    lateinit var pageDriver : PageDriver

    override fun initializeView(view: View)
    {
        /**
         * 初始化 header view
         */
        topicsAdapter = TopicsAdapter(activity)
        headerTopicView = View.inflate(mainActivity, R.layout.layout_explore_topics_header, null)
        val list = headerTopicView.findViewById(R.id.topicsHorList) as RecyclerView
        list.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
        list.adapter = topicsAdapter

        /**
         * 初始化 swipe, recycler
         */
        swipeRefresh = view.findViewById(R.id.swipeRefresh) as SwipeRefreshLayout
        swipeRefresh.setDistanceToTriggerSync(dpToPx(130f))

        booksList = view.findViewById(R.id.booksRecyclerView) as ExRecyclerView
        booksList.setHasFixedSize(true)
        booksList.layoutManager = GridLayoutManager(activity, 2)

        booksList.adapter = booksAdapter

        pageDriver = PageDriver(swipeRefresh, booksList, pageCallback)
        pageDriver.refresh()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.searchAction -> {
                startActivity(SearchActivity::class.java)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * load data from server
     */
    private val pageCallback  = object : PageDriver.ICallback {
        override fun onRefreshFailed() {
            error(TAG, "refresh failed")
        }

        override fun onLoading(page: Int) {
            if (page == 0) {
                Net.restApi.getAllTopics(page, RestApi.DEF_PAGE_LIMIT,
                        object : HttpCallback<Topics>(Topics::class.java) {
                    override fun onResponse(model: Topics?, exception: HttpException?) {
                        if (exception == null && model != null) {
                            topicsAdapter.set(model.list)
                            booksList.addHeader(headerTopicView)
                            booksList.smoothScrollToPosition(0)
                        }
                    }
                })
            }

            Net.wwwApi.getExplorePage(page,
                    object : HttpCallback<WWWExplorePage>(WWWExplorePage::class.java) {
                        override fun onResponse(model: WWWExplorePage?, exception: HttpException?) {
                            var result = PageController.Result.SUCCESS
                            if (exception == null) {
                                if (model!!.books.size < ONE_PAGE_BOOKS_NUM) {
                                    result = PageController.Result.NO_MORE
                                }

                                bindModelToView(page, model)
                            }
                            else {
                                exception.process(swipeRefresh)
                                result = PageController.Result.FAILED
                            }

                            pageDriver.finish(result)
                        }
                    })
        }
    }

    fun bindModelToView(page : Int, model : WWWExplorePage)
    {
        if (page == 0) {
            booksAdapter.set(model.books)
        }
        else {
            booksAdapter.addAll(model.books)
        }
    }

    inner class TopicsAdapter(activity: Activity) :
            ExRecyclerAdapter<ATopic, TopicsAdapter.Holder>(activity)
    {
        override fun onBindViewHolder(holder: Holder?, position: Int) {
            holder?.bindView(data[position], position)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder? {
            return Holder(inflateView(R.layout.item_topic_tag, parent))
        }

        inner class Holder(itemView: View) : ExViewHolder<ATopic>(itemView) {
            override fun bindView(model: ATopic, pos: Int) {
                val text = findView(R.id.topic) as TextView;
                text.text = "${model.name}  | ${model.books}"

                itemView.setOnClickListener {
                    TopicBooksActivity.startMe(activity, model.name)
                }
            }
        }
    }
}