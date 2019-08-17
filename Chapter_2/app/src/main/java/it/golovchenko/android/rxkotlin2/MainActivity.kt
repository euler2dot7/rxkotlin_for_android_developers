package it.golovchenko.android.rxkotlin2

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import it.golovchenko.android.rxkotlin2.network.Entry
import it.golovchenko.android.rxkotlin2.network.FeedObservable

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val feedUrls = listOf(
            "https://news.google.com/?output=atom",
            "http://www.theregister.co.uk/software/headlines.atom",
            "http://www.linux.com/news/soware?format=feed&type=atom"
        )

        Observable.combineLatest(feedUrls.map {
            FeedObservable.getFeed(it)
                .retry(3)
                .onErrorReturn {
                    Log.d(TAG, "onCreate error : $it ")
                    ArrayList()

                }
        }) { listLists ->
            listLists.toMutableList().flatMap { it as Collection<Entry> }
        }
            .map { it.sorted().toList() }
            .flatMap { Observable.fromIterable(it) }
            .take(16)
            .map(Entry::toString)
            .toList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::drawList)
    }

    private fun drawList(list: List<String>) {
        findViewById<ListView>(R.id.feedList).adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
    }


}
