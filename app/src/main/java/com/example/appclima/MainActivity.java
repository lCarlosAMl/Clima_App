package com.example.appclima;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        homeRL= findViewById(R.id.idRLHome);
        loadingPB= findViewById(R.id.idPBLoading);

        cityNameTV= findViewById(R.id.idTVCityName);
        temperatureTV= findViewById(R.id.idTVTemperature);
        conditionTV= findViewById(R.id.idTVCondition);

        weatherRV= findViewById(R.id.idRVWeather);

        cityEdt= findViewById(R.id.idEdtCity);
        backIV= findViewById(R.id.idIVBack);
        iconIV= findViewById(R.id.idIVIcon);
        searchIV= findViewById(R.id.idIVSearch);

        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            cityName = getCityName(location.getLongitude(), location.getLatitude());
            getWeatherInfo(cityName);
        } else {
            // Manejar el caso en el que la ubicación es nula, por ejemplo, mostrar un mensaje de error.
        }

        //TODO: Establecer "Lima" como ciudad predeterminada
        cityName = "Lima";

        // Mostrar la ciudad
        cityNameTV.setText(cityName);

        // Obtener el clima para Lima
        getWeatherInfo(cityName);

        //Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //cityName = getCityName(location.getLongitude(), location.getLatitude());
        //getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ciudad = cityEdt.getText().toString();
                if(ciudad.isEmpty()){
                    Toast.makeText(MainActivity.this, "Por favor, ingrese una ciudad", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(ciudad);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults [0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Por favor, proporcionar los permisos", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "No se ha encontrado";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            if (addresses != null && !addresses.isEmpty()) {
                for (Address adr : addresses) {
                    if (adr != null) {
                        String ciudad = adr.getLocality();
                        if (ciudad != null && !ciudad.isEmpty()) {
                            cityName = ciudad;
                            // Sal del bucle cuando encuentres una ciudad válida.
                            break;
                        }
                    }
                }
            } else {
                Log.d("TAG", "No se encontraron direcciones");
                Toast.makeText(this, "No se encontraron direcciones", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "Error al obtener direcciones: " + e.getMessage());
            Toast.makeText(this, "Error al obtener direcciones", Toast.LENGTH_SHORT).show();
        }
        return cityName;
    }

    /*
    private String getCityName(double longitude, double latitude){
        String cityName = "No se ha encontrado";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for(Address adr : addresses){
                if(adr!=null){
                    String ciudad = adr.getLocality();
                    if(ciudad!=null && !ciudad.equals("")){
                        cityName = ciudad;
                    }else{
                        Log.d("TAG", "Ciudad no encontrada");
                        Toast.makeText(this, "La ciudad ingresada no se encuentra", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
     */
    private void getWeatherInfo(String cityName){
        String url="http://api.weatherapi.com/v1/forecast.json?key=5d4a6f85f43342dd97902252231409&q="+ cityName +"&days=1&aqi=no&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                try{
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1){
                        //Día
                        Picasso.get().load("https://images.unsplash.com/photo-1518841256641-6b50a157e397?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1887&q=80").into(backIV);
                    }else{
                        Picasso.get().load("https://images.unsplash.com/photo-1472552944129-b035e9ea3744?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1887&q=80").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forcast0.getJSONArray("hour");

                    for(int i=0; i< hourArray.length(); i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String tiempo = hourObj.getString("time");
                        String temperatura = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(tiempo,temperatura,img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Por favor, valide el nombre de la Ciudad", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

}