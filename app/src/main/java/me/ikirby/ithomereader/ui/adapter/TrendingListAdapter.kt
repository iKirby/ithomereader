package me.ikirby.ithomereader.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.post_list_item.view.*
import kotlinx.android.synthetic.main.trending_item.view.*
import me.ikirby.ithomereader.CLIP_TAG_NEWS_LINK
import me.ikirby.ithomereader.KEY_TITLE
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.entity.Trending
import me.ikirby.ithomereader.ui.activity.ArticleActivity
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.openLinkInBrowser
import java.util.*

class TrendingListAdapter(
    private val list: ArrayList<Trending>,
    private val context: Context,
    private var showThumb: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            1 -> {
                view = LayoutInflater.from(context).inflate(R.layout.trending_focus_item, parent, false)
                holder = TrendingFocusViewHolder(view)
                view.setOnClickListener {
                    val (_, title, url) = list[holder.bindingAdapterPosition]
                    val intent = Intent(context, ArticleActivity::class.java).apply {
                        putExtra(KEY_URL, url)
                        putExtra(KEY_TITLE, title)
                    }
                    context.startActivity(intent)
                }
                view.setOnLongClickListener {
                    showPopupMenu(list[holder.bindingAdapterPosition])
                    true
                }
            }
            2 -> {
                view = LayoutInflater.from(context).inflate(R.layout.trending_item, parent, false)
                holder = TrendingListViewHolder(view)
                view.setOnClickListener {
                    val (_, title, url) = list[holder.bindingAdapterPosition]
                    val intent = Intent(context, ArticleActivity::class.java).apply {
                        putExtra(KEY_URL, url)
                        putExtra(KEY_TITLE, title)
                    }
                    context.startActivity(intent)
                }
                view.setOnLongClickListener {
                    showPopupMenu(list[holder.bindingAdapterPosition])
                    true
                }
            }
            else -> {
                view = LayoutInflater.from(context).inflate(R.layout.trending_header, parent, false)
                holder = TrendingHeaderViewHolder(view)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (rank, title, _, _, thumb) = list[position]

        if (holder is TrendingHeaderViewHolder) {
            holder.titleText.text = title
        } else if (holder is TrendingListViewHolder) {
            holder.rankText.text = rank
            holder.titleText.text = title
        } else if (holder is TrendingFocusViewHolder) {
            holder.titleText.text = title
            if (showThumb) {
                holder.titleText.maxLines = 3
                holder.thumbImage.visibility = View.VISIBLE
                Glide.with(context).load(thumb).into(holder.thumbImage)
            } else {
                holder.titleText.maxLines = 2
                holder.thumbImage.visibility = View.GONE
            }
        }

    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            list[position].rank != null -> 2
            list[position].url != null -> 1
            else -> 0
        }
    }

    fun setShowThumb(showThumb: Boolean) {
        this.showThumb = showThumb
    }

    internal inner class TrendingListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rankText: TextView = itemView.rank
        var titleText: TextView = itemView.title
    }

    internal inner class TrendingHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView = itemView.title

    }

    internal inner class TrendingFocusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbImage: ImageView = itemView.post_thumb
        var titleText: TextView = itemView.post_title

        init {
            thumbImage.clipToOutline = true
        }
    }

    private fun showPopupMenu(post: Trending) {
        if (post.url == null) {
            return
        }
        UiUtil.showBottomSheetMenu(context, object : BottomSheetMenu.BottomSheetMenuListener {
            override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                inflater.inflate(R.menu.main_context, menu)
                if (post.thumb == null) {
                    menu.removeItem(R.id.view_thumb)
                }
            }

            override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                when (item.itemId) {
                    R.id.share -> {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, post.title + "\n" + post.url)
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(
                                share,
                                context.getString(R.string.share) + " " + post.title
                            )
                        )
                    }
                    R.id.copy_link -> copyToClipboard(CLIP_TAG_NEWS_LINK, post.url)
                    R.id.view_thumb -> {
                        val intent = Intent(context, ImageViewerActivity::class.java).apply {
                            putExtra(KEY_URL, post.thumb)
                        }
                        context.startActivity(intent)
                    }
                    R.id.open_in_browser -> openLinkInBrowser(context, post.url)
                }
            }
        })
    }
}
