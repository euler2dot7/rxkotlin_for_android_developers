package it.golovchenko.android.rxkotlin4.network

data class FlickrPhotosGetSizesResponse(val sizes: Sizes) {
    fun getSizes(): List<Size> = sizes.size
    data class Sizes(val size: List<Size>)
    data class Size(val label: String, val width: Int, val height: Int, val source: String)
}
