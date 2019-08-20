package it.golovchenko.android.rxkotlin6_exercise

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class MainActivity : AppCompatActivity() {
    private val mChangeBackground by lazy(LazyThreadSafetyMode.NONE) { findViewById<Button>(R.id.changeBackground) }
    private val mContainer by lazy(LazyThreadSafetyMode.NONE) { findViewById<FrameLayout>(R.id.container) }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    val colors = listOf(Color.BLUE, Color.WHITE, Color.LTGRAY, Color.RED, Color.CYAN)
    lateinit var viewModel: BackgroundViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = BackgroundViewModel(mChangeBackground.clicks())
        viewModel.color().subscribe(mContainer::setBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        viewModel.subscribe()
    }

    override fun onPause() {
        super.onPause()
        viewModel.unsubscribe()
    }

}

class BackgroundViewModel(val request: Observable<Unit>) {
    private val colors = listOf(Color.WHITE, Color.BLUE, Color.LTGRAY, Color.RED, Color.CYAN)
    private val subscription = CompositeDisposable()
    private val color = BehaviorSubject.createDefault(colors[0])

    fun subscribe() = subscription.add(
        request.observeOn(io()).map { colors[Random().nextInt(colors.size - 1)] }.subscribe(color::onNext)
    )

    fun unsubscribe() = subscription.clear()

    fun color(): Observable<Int> = color.subscribeOn(mainThread())

}