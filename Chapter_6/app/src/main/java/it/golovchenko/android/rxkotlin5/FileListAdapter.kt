package it.golovchenko.android.rxkotlin5

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import java.io.File

class FileListAdapter(context: Context, resource: Int, objects: List<File>) :
    ArrayAdapter<File>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cnvView = super.getView(position, convertView, parent)
        val file = getItem(position)
        (cnvView as TextView).text = if (file?.isDirectory == true) file.name + "/" else file?.name
        cnvView.tag = file
        return cnvView
    }
}
