package it.golovchenko.android.rxkotlin1

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.search_results)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        listView.adapter = adapter
        val editText = findViewById<EditText>(R.id.edit_text)
        editText.textChanges()
            .doOnNext { adapter.clear() }
            .filter { it.length >= 3 }
            .delay(50, TimeUnit.MILLISECONDS)
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.addAll((1..4).map { " $it ${Math.random()}" }) }
        //Exercise
        val message = findViewById<TextView>(R.id.message)
        editText.textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { message.text = if (it.length >= 7) " Text to log" else "Rx Text" }
    }
}
