package br.edu.ifspsaocarlos.sdm.mensageiro.helper;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

/**
 * Created by kaiov on 26/06/2016.
 */
public class VolleyHelper {

    private final RequestQueue requestQueue;
    private final Context context;

    public VolleyHelper(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.context = context;
    }

    public void post(String url, JSONModel model, final VolleyCallback volleyCallback) {

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                model.toJSON(),
                new Response.Listener() {

                    @Override
                    public void onResponse(Object response) {
                        try {
                            volleyCallback.onSuccess((JSONObject) response, context);
                        } catch (Exception e) {
                            volleyCallback.onFatalError(e, context);
                        }
                    }

                },
                new Response.ErrorListener() {

                    public void onErrorResponse(VolleyError error) {
                        volleyCallback.onError(error, context);
                    }
                });

        requestQueue.add(request);
    }

    public void get(String url, final VolleyCallback volleyCallback) {

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener() {

                    @Override
                    public void onResponse(Object response) {
                        try {
                            if(response instanceof String) {
                                volleyCallback.onSuccess(new JSONObject(response.toString()), context);
                            }else if (response instanceof JSONObject) {
                                volleyCallback.onSuccess((JSONObject) response, context);
                            }
                        } catch (Exception e) {
                            volleyCallback.onFatalError(e, context);
                        }
                    }
                },
                new Response.ErrorListener() {

                    public void onErrorResponse(VolleyError error) {
                        volleyCallback.onError(error, context);
                    }
                });

        requestQueue.add(request);
    }

    public void getMany(Iterable<String> urls, final VolleyCallback volleyCallback) {

        for(String url : urls) {
            StringRequest request = new StringRequest(
                    Request.Method.GET,
                    url,
                    new Response.Listener() {

                        @Override
                        public void onResponse(Object response) {
                            try {
                                if (response instanceof String) {
                                    volleyCallback.onSuccess(new JSONObject(response.toString()), context);
                                } else if (response instanceof JSONObject) {
                                    volleyCallback.onSuccess((JSONObject) response, context);
                                }
                            } catch (Exception e) {
                                volleyCallback.onFatalError(e, context);
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            volleyCallback.onError(error, context);
                        }
                    });

            requestQueue.add(request);
        }
    }

    public static interface VolleyCallback {
        public void onSuccess(JSONObject jsonObject, Context context) throws Exception;
        public void onError(VolleyError volleyError, Context context);
        public void onFatalError(Exception e, Context context);
    }

    public static interface JSONModel {
        public JSONObject toJSON();
    }

}
