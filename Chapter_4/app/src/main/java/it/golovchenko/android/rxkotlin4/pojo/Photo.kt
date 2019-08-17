package it.golovchenko.android.rxkotlin4.pojo


import it.golovchenko.android.rxkotlin4.network.FlickrPhotoInfoResponse
import it.golovchenko.android.rxkotlin4.network.FlickrPhotosGetSizesResponse

class Photo(val id: String, val title: String, val username: String, val thumbnailUrl: String?) {
    companion object {
        fun createPhoto(
            photoInfo: FlickrPhotoInfoResponse.PhotoInfo, sizes: List<FlickrPhotosGetSizesResponse.Size>
        ) = Photo(
            photoInfo.id, photoInfo.title._content, photoInfo.owner.username,
            sizes.firstOrNull { it.label == "Square" }?.source
        )
    }
}
