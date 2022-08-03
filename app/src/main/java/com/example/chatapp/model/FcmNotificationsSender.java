package com.example.chatapp.model;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    String image;


    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAAr5auLdE:APA91bGUa2kT2y9hLh4vvo3BuMZvITiD_MZih4qMecKlZetkXMjvRrSXoYtW0cdn8kxTr7Jr2xLgKFkAeR6JcWSdxKfBYSFNl_UNZDpy3hKACxfZgYmLoVP78BFeoyvaqQnIBjCM7pqA";

    public FcmNotificationsSender(String userFcmToken, String title, String body, Context mContext, Activity mActivity,String image) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.image=image;


    }

    public void SendNotifications() {

        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("icon", "icon"); // enter icon that exists in drawable only
            notiObject.put("image",image);
            mainObj.put("notification", notiObject);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, response -> {

                // code run is got response

            }, error -> {

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
}
