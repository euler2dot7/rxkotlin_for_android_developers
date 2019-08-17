package it.golovchenko.android.rxkotlin5

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.itemClickEvents
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val RW_STORAGE = 1001
    }

    private val mListView by lazy(LazyThreadSafetyMode.NONE) { findViewById<ListView>(R.id.list_view) }
    private val backEventObservable = PublishSubject.create<Any>()
    private val homeEventObservable = PublishSubject.create<Any>()
    private val composDisposable = CompositeDisposable()
    private lateinit var adapter: FileListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = FileListAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        mListView.adapter = adapter
        supportActionBar?.title = "RX File browser"
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), RW_STORAGE)
        else initWithPermission()
    }

    private fun initWithPermission() {
        val root = Environment.getExternalStorageDirectory()
        val changeDir = BehaviorSubject.createDefault(root)
        val goHome = homeEventObservable.map { Environment.getExternalStorageDirectory() }
        val goBack = backEventObservable.map { changeDir.value?.parentFile ?: root }
        val goSelection = mListView.itemClickEvents().map { it.clickedView?.tag }.filter { (it as File).isDirectory }.map { it as File }

        composDisposable.addAll(
            Observable.merge(goHome, goBack, goSelection).observeOn(AndroidSchedulers.mainThread()).subscribe { changeDir.onNext(it) },
            changeDir.subscribeOn(AndroidSchedulers.mainThread()).subscribe { this.supportActionBar?.subtitle = it.path },
            changeDir.subscribeOn(Schedulers.io()).switchMap { file ->
                Observable.create<List<File>> { emiter ->
                    try {
                        file.listFiles()?.apply { emiter.onNext(this.toList()) }
                        emiter.onComplete()
                    } catch (e: Exception) {
                        emiter.onError(e)
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread()).subscribe({
                adapter.clear()
                adapter.addAll(it)
            }, {
                Log.d(TAG, "initWithPermission Error: ", it)
            }
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu : ")
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_home).clicks().subscribe(homeEventObservable)
        menu.findItem(R.id.action_back).clicks().subscribe(backEventObservable)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        composDisposable.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PERMISSION_GRANTED) initWithPermission()
        else supportActionBar?.subtitle = " Doesn't work without permissions !!!"
    }
}
