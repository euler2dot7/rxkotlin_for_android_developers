package it.golovchenko.android.rxkotlin4.network

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET("/services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1")
    fun searchPhotos(
        @Query("api_key") apiKey: String,
        @Query("tags") tags: String,
        @Query("per_page") limit: Int
    ): Observable<FlickrSearchResponse>

    @GET("/services/rest/?method=flickr.photos.getInfo&format=json&nojsoncallback=1")
    fun photoInfo(
        @Query("api_key") apiKey: String,
        @Query("photo_id") photoId: String
    ): Observable<FlickrPhotoInfoResponse>

    @GET("/services/rest/?method=flickr.photos.getSizes&format=json&nojsoncallback=1")
    fun getSizes(
        @Query("api_key") apiKey: String,
        @Query("photo_id") photoId: String
    ): Observable<FlickrPhotosGetSizesResponse>
}
