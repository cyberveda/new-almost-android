package com.cyberveda.client.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Class for modeling the response when querying http://192.168.43.25:8000/
 * See example response here: https://gist.github.com/cyberveda/ae03573737067c9269701ea662460205
 */
class FeedbackListSearchResponse(

    @SerializedName("results")
    @Expose
    var results: List<FeedbackSearchResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String
) {

    override fun toString(): String {
        return "FeedbackListSearchResponse(results=$results, detail='$detail')"
    }
}