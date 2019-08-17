package it.golovchenko.android.rxkotlin2.network

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.IOException

class FeedObservable {
    companion object {
        private val TAG = FeedObservable::class.java.simpleName
        fun getFeed(url: String): Observable<List<Entry>> = create(
            url
        ).retry(3)
            .subscribeOn(Schedulers.io())
            .map { FeedParser().parse(it.body()?.byteStream()) ?: listOf() }


        private fun create(url: String): Observable<Response> =
            //  ObservableOnSubscribe is useful for commented code
            Observable.create(ObservableOnSubscribe<Response> {
                val client = OkHttpClient()

                client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "create ERROR:", e)
                        it.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        it.onNext(response)
                        it.onComplete()
                        if (!response.isSuccessful) it.onError(Exception("error"))
                    }
                })
//                try {
//                    val response = client.newCall(Request.Builder().url(url).build()).execute()
//                    it.onNext(response)
//                    it.onComplete()
//                    if (!response.isSuccessful) it.onError(Exception("error"))
//                } catch (e: Exception) {
//                    Log.e(TAG, "create ERROR:", e)
//                    it.onError(e)
//                }


            })
//                .subscribeOn(Schedulers.io())


    }
}