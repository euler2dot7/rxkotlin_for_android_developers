package it.golovchenko.android.rxkotlin4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.golovchenko.android.rxkotlin4.pojo.Photo

class PhotoAdapter(private val photos: List<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    override fun getItemCount(): Int = photos.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PhotoViewHolder =
        PhotoViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.photo_list_card_view, viewGroup, false))


    override fun onBindViewHolder(photoViewHolder: PhotoViewHolder, i: Int) {
        val photo = photos[i]
        photoViewHolder.personName.text = photo.title
        photoViewHolder.personAge.text = photo.username
        Picasso.get().load(photo.thumbnailUrl).into(photoViewHolder.personPhoto)
    }

    class PhotoViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var personName: TextView = itemView.findViewById(R.id.person_name)
        internal var personAge: TextView = itemView.findViewById(R.id.person_age)
        internal var personPhoto: ImageView = itemView.findViewById(R.id.person_photo)
    }
}
