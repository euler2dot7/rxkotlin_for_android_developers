package it.golovchenko.android.rxkotlin5

import android.os.Environment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File


class FileBrowserViewModel(
    goHomeBtn: Observable<Any>,
    goUpBtn: Observable<Any>,
    private val goToLocation: Observable<File>,
    private val setFileList: (List<File>) -> Unit,
    private val setDir: (String) -> Unit
) {

    private val backEventObservable = PublishSubject.create<Any>()
    private val homeEventObservable = PublishSubject.create<Any>()
    private val composDisposable = CompositeDisposable()
    private val viewsDisposable = CompositeDisposable()
    private val root = Environment.getExternalStorageDirectory()
    private val filesSubject = BehaviorSubject.create<List<File>>()
    private val currentDir = BehaviorSubject.createDefault(root)
    private val goHome = homeEventObservable.map { Environment.getExternalStorageDirectory() }
    private val goBack = backEventObservable.map { currentDir.value?.parentFile ?: root }

    init {
        goHomeBtn.subscribe(homeEventObservable)
        goUpBtn.subscribe(backEventObservable)
    }

    fun subscribe() = composDisposable.addAll(
        Observable.merge( goHome, goBack, goToLocation )
            .observeOn(mainThread()).subscribe(currentDir::onNext),
        currentDir.subscribeOn(Schedulers.io()).switchMap { file ->
            Observable.create<List<File>> { emitter ->
                try {
                    file.listFiles()?.apply { emitter.onNext(this.toList()) }
                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }.subscribe(filesSubject::onNext)
    )


    fun unsubscribe() = composDisposable.clear()


    fun bindView() = viewsDisposable.addAll(
        filesSubject.observeOn(AndroidSchedulers.mainThread())?.subscribe { setFileList(it) },
        currentDir.observeOn(AndroidSchedulers.mainThread())?.subscribe { setDir(it.path) }
    )


    fun unbindView(): Unit = viewsDisposable.clear()


}