package me.ikirby.ithomereader.ui.task

import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.entity.UpdateInfo
import me.ikirby.ithomereader.ui.activity.DialogActivity
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.util.Logger
import org.json.JSONObject
import org.jsoup.Jsoup

fun checkForUpdate(activity: BaseActivity, showToast: Boolean = false) {
    val parameter = "?_=" + System.currentTimeMillis()
    val updateUrl = activity.packageManager
        .getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("update_url")

    activity.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                val json = Jsoup.connect(updateUrl + parameter)
                    .ignoreContentType(true)
                    .timeout(5000).execute().body()
                val info = JSONObject(json)
                UpdateInfo(
                    info.getInt("versionCode"),
                    info.getString("version"),
                    info.getString("log"),
                    info.getString("url") + parameter
                )
            }
        }.onSuccess {
            if (it.versionCode > BuildConfig.VERSION_CODE) {
                val intent = Intent(activity, DialogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(KEY_UPDATE_INFO, it)
                }
                activity.startActivity(intent)
            } else if (showToast) {
                ToastUtil.showToast(R.string.no_update_found)
            }
        }.onFailure {
            Logger.d("Tasks.CheckForUpdate", "Check for update failed", it)
            if (showToast) ToastUtil.showToast(R.string.no_update_found)
        }
    }
}

fun clearCache(activity: BaseActivity) {
    activity.launch {
        withContext(Dispatchers.IO) {
            me.ikirby.ithomereader.util.clearCache()
        }
    }
}

fun cleanUp(activity: BaseActivity) {
    clearCache(activity)
    if (BaseApplication.preferences.getInt(SETTINGS_KEY_VERSION, BuildConfig.VERSION_CODE) <= 146) {
        val customFilter = BaseApplication.preferences
            .getString(SETTINGS_KEY_CUSTOM_FILTER, "")!!
            .split(", ").filter { it.isNotEmpty() }
        BaseApplication.preferences.edit()
            .putString(SETTINGS_KEY_CUSTOM_FILTER, customFilter.joinToString(","))
            .apply()
    }
    BaseApplication.preferences.edit()
        .putInt(SETTINGS_KEY_VERSION, BuildConfig.VERSION_CODE)
        .remove(SETTINGS_KEY_NIGHT_MODE)
        .remove(SETTINGS_KEY_AUTO_NIGHT_MODE)
        .remove(SETTINGS_KEY_WHITE_THEME)
        .apply()
}
