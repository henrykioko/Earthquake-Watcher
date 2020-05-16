package com.example.earthquakewatcher.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.earthquakewatcher.Model.EarthQuake;
import com.example.earthquakewatcher.R;
import com.example.earthquakewatcher.Util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class QuakesListAcitivity extends AppCompatActivity {

    private ArrayList<String> cityList, maneno;
    private ListView listView;
    private RequestQueue queue;
    private ArrayAdapter arrayAdapter;
    private List<EarthQuake> quakeList;

    private AlertDialog.Builder dialogBuilder;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_list_acitivity);

        quakeList= new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        queue = Volley.newRequestQueue(this);
        cityList = new ArrayList<>();
        maneno = new ArrayList<>();

        getAllQuakes(Constants.URL);
    }

    void getAllQuakes(String url){


        final EarthQuake earthQuake = new EarthQuake();

        JsonObjectRequest jsonObjectRequest = new
                JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");

                            for (int i = 0; i<features.length(); i++){
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");

//                                Log.d("Ngori", "onResponse: " +properties.getString("place"));

                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");

                                JSONArray coordinates = geometry.getJSONArray("coordinates");
//                                double lon = coordinates.getDouble(0);
//                                double lat = coordinates.getDouble(1);

//                                Log.d("Vybe", "onResponse: " +lon +" " +lat);

                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                maneno.add(earthQuake.getDetailLink());
                                cityList.add(earthQuake.getPlace());

                            }
                            Log.d("kiimba", "onResponse: " +cityList);

                            arrayAdapter = new ArrayAdapter<>(QuakesListAcitivity.this,
                                    android.R.layout.simple_list_item_1, android.R.id.text1,cityList);

                            listView.setAdapter(arrayAdapter);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    String named = maneno.get(i);
                                    getQuakeDetails(named);


                                }
                            });
                            arrayAdapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);


    }

    private void getQuakeDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            String detailsUrl = "";
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray geoserve = products.getJSONArray("geoserve");

                    for (int i = 0; i<geoserve.length(); i++){

                        JSONObject geoserveObj = geoserve.getJSONObject(i);
                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geourl = contentObj.getJSONObject("geoserve.json");
                        detailsUrl = geourl.getString("url");

//                        Log.d("Babe", "onResponse: " +detailsUrl);
                        getMoreDetails(detailsUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }

    public void getMoreDetails(String url){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                dialogBuilder = new AlertDialog.Builder(QuakesListAcitivity.this);
                View view = getLayoutInflater().inflate(R.layout.popup, null);

                Button dismissButton = (Button) view.findViewById(R.id.dismissPop);
                Button dismissButtonTop = (Button) view.findViewById(R.id.dismissPopTop);
                TextView popList = (TextView) view.findViewById(R.id.popList);
                TextView popListTitle = (TextView) view.findViewById(R.id.popListTitle);
                WebView htmlPop = (WebView) view.findViewById(R.id.htmlWebView);

                StringBuilder stringBuilder = new StringBuilder();

                try {

                    if (response.has("tectonicSummary")
                            && response.getString("tectonicSummary")!= null){

                        JSONObject tectonic = response.getJSONObject("tectonicSummary");

                        if (tectonic.has("text") && tectonic.getString("text")!= null){

                            String text = tectonic.getString("text");

                            htmlPop.loadDataWithBaseURL(null, text,"text/html",
                                    "UTF-8", null);
                        }else{

                            String text = "Sorry! Earthquake history doesn't exist.";
                            htmlPop.loadDataWithBaseURL(null, text,"text/html",
                                    "UTF-8", null);


                        }
                    }

                    JSONArray cities = response.getJSONArray("cities");

                    for (int i =0; i<cities.length(); i++){

                        JSONObject citiesObj = cities.getJSONObject(i);

                        stringBuilder.append("Cities: " +citiesObj.getString("name")
                                + "\n" + "Distance: " +citiesObj.getString("distance")
                                +"\n" +"Population: " +citiesObj.getString("population"));

                        stringBuilder.append("\n\n");

                    }

                    popList.setText(stringBuilder);

                    dialogBuilder.setView(view);
                    final AlertDialog dialog = dialogBuilder.create();
                    dialog.show();

                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }


}
