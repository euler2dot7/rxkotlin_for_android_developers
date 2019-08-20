package it.golovchenko.android.rxkotlin5

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.itemClickEvents
import io.reactivex.subjects.PublishSubject
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val RW_STORAGE = 1001
    }

    private val mListView by lazy(LazyThreadSafetyMode.NONE) { findViewById<ListView>(R.id.list_view) }
    private val backEventObservable = PublishSubject.create<Any>()
    private val homeEventObservable = PublishSubject.create<Any>()
    private lateinit var adapter: FileListAdapter
    private var viewModel: FileBrowserViewModel? = null

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
        val goSelection = mListView.itemClickEvents().map { it.clickedView?.tag }.filter { (it as File).isDirectory }.map { it as File }
        viewModel = FileBrowserViewModel(
            homeEventObservable.hide(), backEventObservable.hide(),
            goSelection, ::setFileList, ::setDir
        )
        viewModel?.subscribe()
    }

    override fun onResume() {
        super.onResume()
        viewModel?.bindView()
    }

    override fun onPause() {
        super.onPause()
        viewModel?.unbindView()
    }

    private fun setFileList(fileList: List<File>) {
        adapter.clear()
        adapter.addAll(fileList)
    }


    private fun setDir(dir: String) {
        supportActionBar?.subtitle = dir
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
        viewModel?.unsubscribe()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PERMISSION_GRANTED) initWithPermission()
        else supportActionBar?.subtitle = " Doesn't work without permissions !!!"
    }
}
