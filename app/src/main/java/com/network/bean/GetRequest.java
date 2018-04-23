package com.network.bean;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by wangliming on 2017/12/11.
 */

public interface GetRequest {

    @GET("ajax.php?a=fy&f=auto&t=auto&w=hello%20world")
    Call<Translation> getCall();
}
