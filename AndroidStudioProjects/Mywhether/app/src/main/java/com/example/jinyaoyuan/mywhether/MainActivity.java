package com.example.jinyaoyuan.mywhether;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import android.os.Handler;

import java.lang.reflect.Field;
import java.util.logging.LogRecord;
import java.util.zip.GZIPInputStream;

import bean.TodayBean;
import util.NetUtil;
import util.PinYin;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {//对应一个layout.xml

    private ImageView mUpdateBtn;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv;
    private ImageView weatherImg, pmImg;

    private static final int UPDATE_TODAY_WEATHER = 1;


    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayBean) msg.obj);
                    break;
                default:
                    break;
            }


        }

    };


    void initView() {
        cityTv = (TextView) findViewById(R.id.content_city_name);
        timeTv = (TextView) findViewById(R.id.content_time);
        humidityTv = (TextView) findViewById(R.id.content_wet_degree);
        weekTv = (TextView) findViewById(R.id.content_date);
        pmDataTv = (TextView) findViewById(R.id.content_pm_degree);
        pmQualityTv = (TextView) findViewById(R.id.content_polluted_degree);
        pmImg = (ImageView) findViewById(R.id.content_headimage);
        temperatureTv = (TextView) findViewById(R.id.content_temperature);
        climateTv = (TextView) findViewById(R.id.content_weather);
        windTv = (TextView) findViewById(R.id.content_wind);
        weatherImg = (ImageView) findViewById(R.id.image_qing);
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//必须要重写
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        initView();
    }

    void updateTodayWeather(TodayBean todayBean) {
        Log.d("myapp3", todayBean.toString());
        cityTv.setText(todayBean.getCity());
        timeTv.setText(todayBean.getUpdatetime() + "发布");
        humidityTv.setText("湿度 " + todayBean.getShidu());
        pmDataTv.setText(todayBean.getPm25());
        pmQualityTv.setText(todayBean.getQuality());
        weekTv.setText(todayBean.getDate());
        temperatureTv.setText(todayBean.getHigh() + "~" + todayBean.getLow());
        climateTv.setText(todayBean.getType());
        windTv.setText("风力：" + todayBean.getFengli());

        int pmValue = Integer.parseInt(todayBean.getPm25().trim());
        String pmImgStr = "0_50";
        if (pmValue > 50 && pmValue < 201) {
            int startV = (pmValue - 1) / 50 * 50 + 1;
            int endV = ((pmValue - 1) / 50 + 1) * 50;
            pmImgStr = Integer.toString(startV) + "_" + endV;
        } else if (pmValue >= 201 && pmValue < 301) {
            pmImgStr = "201_300";
        } else if (pmValue >= 301) {
            pmImgStr = "greater_300";
        }
        String typeImg = "biz_plugin_weather_" + PinYin.converterToSpell(todayBean.getType());
        Class aClass = R.drawable.class;
        int typeId = -1;
        int pmImgId = -1;
        try {

            Field field = aClass.getField(typeImg);
            Object value = field.get(new Integer(0));
            typeId = (int) value;

            Field pmField = aClass.getField("biz_plugin_weather_" + pmImgStr);
            Object pmImgO = pmField.get(new Integer(0));
            pmImgId = (int) pmImgO;
        } catch (Exception e) { //e.printStackTrace();
            if (-1 == typeId)
                typeId = R.drawable.biz_plugin_weather_qing;
            if (-1 == pmImgId)
                pmImgId = R.drawable.biz_plugin_weather_0_50;
        } finally {
            Drawable drawable = getResources().getDrawable(typeId);
            weatherImg.setImageDrawable(drawable);
            drawable = getResources().getDrawable(pmImgId);
            pmImg.setImageDrawable(drawable);
            Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("myWeather", "网络Ok");
                queryWeatherCode(cityCode);

            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_LONG).show();
            }


        }
    }


    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(address);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        InputStream responseStream = entity.getContent();

                        responseStream = new GZIPInputStream(responseStream);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            response.append(str);
                        }

                        String responseStr = response.toString();
                        Log.d("myWeather", responseStr);
                        TodayBean todayBean = parseXML(responseStr);
                        if (todayBean != null) {
                            Log.d("myapp2", todayBean.toString());
                            Message msg = new Message();
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayBean;
                            mHandler.sendMessage(msg);


                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private TodayBean parseXML(String xmldata) {
        TodayBean todayBean = null;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();

            int fengxiangCount = 0;
            int fengliCount = 0;
            int dateCount = 0;
            int highCount = 0;
            int lowCount = 0;
            int typeCount = 0;

            Log.d("myapp2", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayBean = new TodayBean();
                        }
                        if (todayBean != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayBean.setCity(xmlPullParser.getText());
                                Log.d("myapp2", "city:" + xmlPullParser.getText());

                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayBean.setUpdatetime(xmlPullParser.getText());
                                Log.d("myapp2", "updatetime:" + xmlPullParser.getText());

                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayBean.setShidu(xmlPullParser.getText());
                                Log.d("myapp2", "shidu:" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayBean.setWendu(xmlPullParser.getText());
                                Log.d("myapp2", "wendu:" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayBean.setPm25(xmlPullParser.getText());
                                Log.d("myapp2", "pm25:" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayBean.setQuality(xmlPullParser.getText());
                                Log.d("myapp2", "quality:" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayBean.setFengxiang(xmlPullParser.getText());
                                Log.d("myapp2", "fengxiang:" + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayBean.setFengli(xmlPullParser.getText());
                                Log.d("myapp2", "fengli:" + xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayBean.setDate(xmlPullParser.getText());
                                Log.d("myapp2", "date:" + xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayBean.setHigh(xmlPullParser.getText());
                                Log.d("myapp2", "high:" + xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayBean.setLow(xmlPullParser.getText());
                                Log.d("myapp2", "low:" + xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayBean.setType(xmlPullParser.getText());
                                Log.d("myapp2", "type:" + xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;


                    case XmlPullParser.END_TAG:
                        break;

                }


                eventType = xmlPullParser.next();

            }


        } catch (Exception e) {
            e.printStackTrace();

        }

        return todayBean;
    }

}
