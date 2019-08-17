package it.golovchenko.android.rxkotlin4.network

data class FlickrPhotoInfoResponse(val photo: PhotoInfo) {

    data class PhotoInfo(
        val id: String,
        val owner: FlickrOwner,
        val title: FlickrTitle,
        val views: Int,
        val urls: FlickrUrls
    )

    data class FlickrTitle(val _content: String)
    data class FlickrUrls(val urls: List<FlickrUrl>)
    data class FlickrUrl(val type: String, val content: String)
    data class FlickrOwner(val username: String)
}
