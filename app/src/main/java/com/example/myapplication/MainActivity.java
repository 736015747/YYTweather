package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText cityIdEditText;
    private Button submitButton;
    private Button refreshButton;
    private Button favoriteButton;
    private TextView weatherDataTextView;

//    private SharedPreferences sharedPreferences;
    private List<City> cities;
    private ArrayAdapter<String> adapter;


    private static final String API_BASE_URL = "http://t.weather.sojson.com/api/weather/city/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        favoriteButton=findViewById(R.id.favorite_city);
        cityIdEditText = findViewById(R.id.city_id_edittext);
        submitButton = findViewById(R.id.submit_button);
        refreshButton = findViewById(R.id.refresh_button);
        weatherDataTextView = findViewById(R.id.weather_data_textview);

//        sharedPreferences = getSharedPreferences("WeatherCache", Context.MODE_PRIVATE);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityId = cityIdEditText.getText().toString();
                if (isValidCityId(cityId)) {
                    // Check cache first
                    String cachedData = getWeatherDataFromCache(cityId);
                    if (cachedData != null) {
                        displayWeatherData(cachedData);
                    } else {
                        new FetchWeatherDataTask().execute(cityId);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请输入有效的城市ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityId = cityIdEditText.getText().toString();
                if (isValidCityId(cityId)) {
                    new FetchWeatherDataTask().execute(cityId);
                } else {
                    Toast.makeText(MainActivity.this, "请输入有效的城市ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });

        // 获取ListView组件
        ListView listView = findViewById(R.id.listView);

        // 创建一个用于显示省市数据的列表
        ArrayList<City> cities = new ArrayList<>();
        try {
            // 从city.json文件中读取数据
            JSONArray cityJsonArray = new JSONArray(loadJSONFromAsset());
            City beijing = new City(1, 0, "101010100", "北京", "100000", "010");
            cities.add(beijing);
            for (int i = 0; i < cityJsonArray.length(); i++) {
                JSONObject cityJsonObject = cityJsonArray.getJSONObject(i);
                int id = cityJsonObject.getInt("id");
                int pid = cityJsonObject.getInt("pid");
                String cityCode = cityJsonObject.optString("city_code");
                String cityName = cityJsonObject.getString("city_name");
                String postCode = cityJsonObject.optString("post_code");
                String areaCode = cityJsonObject.optString("area_code");
                if (cityCode.equals("")&&pid==0){
                    City city = new City(id, pid, cityCode, cityName, postCode, areaCode);
                    cities.add(city);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 创建适配器，并设置给ListView
        CityListAdapter adapter = new CityListAdapter(this, cities);
        listView.setAdapter(adapter);


        // 设置ListView的点击事件监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击的城市对象
                City selectedCity = cities.get(position);
                // 判断点击的是省级还是市级行政单位
                if (selectedCity.getPid() == 0) {
                    // 点击的是省级行政单位，可以根据需要处理


                    String cityId = selectedCity.getCityCode();

                    /**/
                    Intent intent = new Intent(MainActivity.this, ChildcityActivity.class);
                    intent.putExtra("intent_cityId", selectedCity.getId());
                    Intent[] intents = {intent}; // 将intent对象放入数组中
                    startActivities(intents); // 使用数组作为参数调用startActivities方法
                    /**/

                } else {
                    // 点击的是市级行政单位，可以根据需要处理
                    // 执行查询天气数据的操作
                    String cityId = selectedCity.getCityCode();
                }
            }
        });
    }


    // 从assets文件夹中读取city.json文件的内容
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("city.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }





    private boolean isValidCityId(String cityId) {
        return cityId.length() == 9;
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
            String code="";
            if (dataObject != null) {
                date = dataObject.optString("date");
                time = dataObject.optString("time");
                wendu = dataObject.optString("wendu");
                shidu = dataObject.optString("shidu");
                pm25 = dataObject.optString("pm25");
                code=cityInfoObject.optString("citykey");
            }
            // 打开或创建数据库
            WeatherDBHelper dbHelper = new WeatherDBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // 插入数据
            ContentValues values = new ContentValues();
            values.put(WeatherDBHelper.COLUMN_CITY, city);
            values.put(WeatherDBHelper.COLUMN_CODE,code);
            values.put(WeatherDBHelper.DATA,data);
            values.put(WeatherDBHelper.COLUMN_DATE, jsonData.optString("date"));
            values.put(WeatherDBHelper.COLUMN_UPDATE_TIME, jsonData.optString("time"));
            values.put(WeatherDBHelper.COLUMN_TEMPERATURE, wendu);
            values.put(WeatherDBHelper.COLUMN_HUMIDITY, shidu);
            values.put(WeatherDBHelper.COLUMN_PM25, pm25);
            db.insert(WeatherDBHelper.TABLE_NAME, null, values);

            // Construct the weather information string and display it in the TextView
            String weatherInfo =  "市：" + city + "\n"
                    + "日期：" + jsonData.optString("date") + "\n"
                    + "数据更新时间：" + jsonData.optString("time") + "\n"
                    + "温度：" + wendu + "°C\n"
                    + "湿度：" + shidu + "\n"
                    + "PM2.5：" + pm25;

            weatherDataTextView.setText(weatherInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

                String cityId = cityIdEditText.getText().toString();
                saveWeatherDataToCache(cityId, data);
            }
        }

        private void saveWeatherDataToCache(String cityId, String data) {
            SharedPreferences sharedPreferences = getSharedPreferences("WeatherCache", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(cityId, data);
            editor.apply();
        }
    }


}
