package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FavoriteActivity extends AppCompatActivity {
    private ListView listView;
    private FavoriteCityAdapter adapter;
    private Set<String> favorites;

    private static final String API_BASE_URL = "http://t.weather.sojson.com/api/weather/city/";

    private TextView weather_data_textview3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // 获取ListView组件
        listView = findViewById(R.id.listView3);
        weather_data_textview3=findViewById(R.id.weather_data_textview3);
        // 从SharedPreferences中获取收藏的城市数据
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        Set<String> citySet = sharedPreferences.getStringSet("citySet", null);
        favorites=new HashSet<>();
        HashMap<String, String> cityMap = new HashMap<>();
        if (citySet != null) {

            for (String cityEntry : citySet) {
                // 使用之前定义的分隔符将城市名和城市代码拆分
                String[] parts = cityEntry.split(":");
                if (parts.length == 2) {
                    String city = parts[0];
                    String cityCode = parts[1];
                    cityMap.put(city, cityCode);
                    favorites.add(city);
                }

        }}
        // 现在您可以使用cityMap进行后续的操作
        // 创建适配器，并设置给ListView
        adapter = new FavoriteCityAdapter(this, new ArrayList<>(favorites));
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName = adapter.getItem(position);  // 获取点击的城市名
                String cityCode = cityMap.get(cityName);  // 根据城市名获取对应的cityCode

                String cachedData = getWeatherDataFromCache(cityCode);
                if (cachedData != null) {
                    displayWeatherData(cachedData);
                } else {
                    new FavoriteActivity.FetchWeatherDataTask().execute(cityCode);
                }
            }
        });
    }

    private String getWeatherDataFromCache(String cityId) {
        Cursor cursor=getWeatherDataByCityCode(cityId);

        if (cursor != null && cursor.moveToFirst()) {
            // 从游标中提取data列的值
            @SuppressLint("Range") String weatherData = cursor.getString(cursor.getColumnIndex(WeatherDBHelper.DATA));
            // 关闭游标
            Log.d("yyt", "getWeatherDataFromCache: "+weatherData);
            cursor.close();
            // 返回查询结果
            return weatherData;
        }
        return null;
    }

    public Cursor getWeatherDataByCityCode(String cityCode) {
        WeatherDBHelper dbHelper = new WeatherDBHelper(this); // 实例化WeatherDBHelper类
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 构建查询语句
        String query = "SELECT " + WeatherDBHelper.DATA +
                " FROM " + WeatherDBHelper.TABLE_NAME +
                " WHERE " + WeatherDBHelper.COLUMN_CODE + " = ?";
        // 执行查询
        Cursor cursor = db.rawQuery(query, new String[]{cityCode});

        // 返回查询结果的游标
        return cursor;
    }
    private   class FetchWeatherDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String cityId = params[0];
            String apiUrl = API_BASE_URL + cityId;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            if (data != null) {
                displayWeatherData(data);
            }

        }

    }
    private void displayWeatherData(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);

            // Extract the city information from the "cityInfo" object
            JSONObject cityInfoObject = jsonData.optJSONObject("cityInfo");
            String city = "";
            if (cityInfoObject != null) {
                city = cityInfoObject.optString("city");
            }

            // Extract the weather data from the "data" object
            JSONObject dataObject = jsonData.optJSONObject("data");
            String date = "";
            String time = "";
            String wendu = "";
            String shidu = "";
            String pm25 = "";
            if (dataObject != null) {
                date = dataObject.optString("date");
                time = dataObject.optString("time");
                wendu = dataObject.optString("wendu");
                shidu = dataObject.optString("shidu");
                pm25 = dataObject.optString("pm25");
            }
            SharedPreferences sharedPreferences = getSharedPreferences("WeatherCache", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("city", city);
            editor.putString("date", jsonData.optString("date"));
            editor.putString("time", jsonData.optString("time"));
            editor.putString("wendu", wendu);
            editor.putString("shidu", shidu);
            editor.putString("pm25", pm25);
            editor.apply();



            // Construct the weather information string and display it in the TextView
            String weatherInfo =  "市：" + city + "\n"
                    + "日期：" + jsonData.optString("date") + "\n"
                    + "数据更新时间：" + jsonData.optString("time") + "\n"
                    + "温度：" + wendu + "°C\n"
                    + "湿度：" + shidu + "\n"
                    + "PM2.5：" + pm25;

            weather_data_textview3.setText(weatherInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
