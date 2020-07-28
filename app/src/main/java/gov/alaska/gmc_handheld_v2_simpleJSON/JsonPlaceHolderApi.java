package gov.alaska.gmc_handheld_v2_simpleJSON;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface JsonPlaceHolderApi {


    @GET("inventory.json")
    Call<ResponseBody> getInventory(
            @Query("barcode") String barcode
    );
}
