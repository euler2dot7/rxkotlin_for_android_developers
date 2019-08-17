package it.golovchenko.android.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val mLabelOne by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.label_one) }
    private val mLabelTwo by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.label_two) }
    private val mLabelThree by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.label_three) }
    private val mResetBtn by lazy(LazyThreadSafetyMode.NONE) { findViewById<Button>(R.id.reset_btn) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val interval =
            Observable.interval(1, TimeUnit.SECONDS).map(Long::toString).observeOn(AndroidSchedulers.mainThread())
//Ex 1
//        with(SerialDisposable()) {
//            listOf(mLabelOne, mLabelTwo, mLabelThree).forEach { label ->
//                label.clicks().subscribe { set(interval.subscribe(label::setText)) }
//            }
//        }

//Ex 2
        val cd = CompositeDisposable()
        listOf(mLabelOne, mLabelTwo, mLabelThree).map { label ->
            label.clicks().switchMap { interval }.subscribe(label::setText)
        }.forEach { cd.add(it) }


        mResetBtn.clicks().subscribe {
            cd.clear()
            listOf(mLabelOne, mLabelTwo, mLabelThree).map { label ->
                label.clicks().switchMap { interval }.subscribe(label::setText)
            }.forEach { cd.add(it) }
        }

    }

}
