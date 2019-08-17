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
        var convertView = super.getView(position, convertView, parent)
        val file = getItem(position)
        (convertView as TextView).text = if (file?.isDirectory == true) file.name + "/" else file?.name
        convertView.tag = file
        return convertView
    }
}
