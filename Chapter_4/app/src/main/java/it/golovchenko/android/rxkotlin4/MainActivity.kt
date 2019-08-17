package it.golovchenko.android.rxkotlin4

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import it.golovchenko.android.rxkotlin4.network.FlickrApi
import it.golovchenko.android.rxkotlin4.network.FlickrPhotoInfoResponse
import it.golovchenko.android.rxkotlin4.network.FlickrPhotosGetSizesResponse
import it.golovchenko.android.rxkotlin4.network.FlickrSearchResponse
import it.golovchenko.android.rxkotlin4.pojo.Photo
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class MainActivity : AppCompatActivity() {
    private val mSearchText by lazy(LazyThreadSafetyMode.NONE) { findViewById<EditText>(R.id.search_text) }
    private val mSearchButton by lazy(LazyThreadSafetyMode.NONE) { findViewById<Button>(R.id.search_button) }
    private val mMainList by lazy(LazyThreadSafetyMode.NONE) { findViewById<RecyclerView>(R.id.main_list) }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMainList.layoutManager = LinearLayoutManager(this)

        val builder = OkHttpClient.Builder()

        // Log信息拦截器
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        //设置 Debug Log 模式
        builder.addInterceptor(loggingInterceptor)

        val restAdapter = Retrofit.Builder()

            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.flickr.com")
            .client(builder.build())
            .build()

        val api = restAdapter.create(FlickrApi::class.java)
        val apiKey = BuildConfig.FLICKR_API_KEY

        val searchButtonObservable = mSearchButton.clicks()

        val searchTextInput = mSearchText.textChanges().map(CharSequence::toString)

        searchTextInput.map { it.length >= 3 }
            .distinctUntilChanged()
            .doOnNext { Log.d(TAG, "onCreate : $it ") }
            .subscribe(mSearchButton::setEnabled)


        searchButtonObservable
            .doOnNext { Log.d(TAG, "Click performed : ") }
            .withLatestFrom(searchTextInput) { _, searchText -> searchText }
            .doOnNext { Log.d(TAG, "Search started for: $it") }
            .flatMap { api.searchPhotos(apiKey, it, 3).subscribeOn(Schedulers.io()) }
            .map(FlickrSearchResponse::getPhotos)
            .onErrorReturn {
                it.printStackTrace()
                Log.e(TAG, "onCreate : ", it)
                ArrayList()
            }
            .doOnNext { Log.d(TAG, "Photo list size : ${it.size}") }
            .flatMap { photos ->
                if (photos.isNotEmpty())
                    Observable.fromIterable(photos)
                        .doOnNext { Log.d(TAG, "Processing photo  " + it.id) }
                        .concatMap { photo ->
                            Observables.combineLatest(
                                api.photoInfo(apiKey, photo.id).subscribeOn(Schedulers.io())
                                    .map(FlickrPhotoInfoResponse::photo),
                                api.getSizes(apiKey, photo.id).subscribeOn(Schedulers.io())
                                    .map(FlickrPhotosGetSizesResponse::getSizes),
                                Photo.Companion::createPhoto
                            )
                        }
                        .doOnNext { Log.d(TAG, "Finished processing photo " + it.id) }
                        .toList()
                        .doOnSuccess { Log.d(TAG, "Finished processing all photos") }
                        .toObservable()
                else Observable.just(ArrayList())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mMainList.adapter = PhotoAdapter(it)
            }) {
                Log.d(TAG, "onError : ", it)
            }
    }
}
