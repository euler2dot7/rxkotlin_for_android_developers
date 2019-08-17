package it.golovchenko.android.rxkotlin4.network

data class FlickrSearchResponse private constructor(private val photos: Photos) {

    fun getPhotos(): List<Photo> = photos.photo

    private data class Photos(var photo: List<Photo>)

    data class Photo(val id: String, private val owner: String, private val title: String) {
        override fun toString(): String = title
    }
}
