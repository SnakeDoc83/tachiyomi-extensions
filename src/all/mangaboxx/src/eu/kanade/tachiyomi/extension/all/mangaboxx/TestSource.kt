package eu.kanade.tachiyomi.extension.all.mangaboxx

import android.annotation.SuppressLint
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import okhttp3.Request
import java.text.SimpleDateFormat
import rx.Observable
import java.util.Locale

/**
 *  Extending a class purely for my own convenience
 *  In reality, this classes' contents would be added to HttpSource
 */

abstract class TestSource(name: String, url: String, lang: String, dateFormat: SimpleDateFormat = SimpleDateFormat("MMM-dd-yy", Locale.US)): MangaBox(name, url, lang, dateFormat) {

    @SuppressLint("DefaultLocale")
    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        return if (query.startsWith("TAG:")) fetchTagQuery(page, query.substringAfter("TAG:").toLowerCase())
            else client.newCall(searchMangaRequest(page, query, filters))
            .asObservableSuccess()
            .map { response ->
                searchMangaParse(response)
            }
    }

    private fun fetchTagQuery(page: Int, tag: String): Observable<MangasPage> {
        return tagHashMap[tag]?.let { uriPart ->
            client.newCall(tagQueryRequest(page, uriPart))
                .asObservableSuccess()
                .map { response ->
                    searchMangaParse(response)
                }
        } ?: Observable.just(MangasPage(emptyList(), false))
    }

    // only these last two need to be overridden in the final class (if you want to enable global tag search for that source)
    protected open fun tagQueryRequest(page: Int, uriPart: String): Request {
        return GET("", headers)
    }

    protected open val tagHashMap: HashMap<String, String> = hashMapOf()
}
