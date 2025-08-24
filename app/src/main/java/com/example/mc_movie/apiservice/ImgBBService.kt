import com.example.mc_movie.model.ImgBBResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface ImgBBService {
    @FormUrlEncoded
    @POST("upload")
    fun uploadImage(
        @Query("key") apiKey: String,        // <-- Change here
        @Field("image") base64Image: String
    ): Call<ImgBBResponse>
}
