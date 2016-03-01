package locationmanager.com.example;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private LocationManager locationManager;
    private String string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取显示信息的TextView
        textView = (TextView) findViewById(R.id.text_view);
        //获取定位管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //获取所有可用的位置提供器
        List<String> list = locationManager.getProviders(true);
        //判断集合里是否包含GPS位置提供器
        if (list.contains(LocationManager.GPS_PROVIDER)){
            string = LocationManager.GPS_PROVIDER;
        //判断是否含有网络位置提供器
        }else if(list.contains(LocationManager.NETWORK_PROVIDER)){
            string = LocationManager.NETWORK_PROVIDER;
        }else {
            Toast.makeText(this, "未开启定位服务！", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取location加了异常处理
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(string);
            //如果不为空则执行方法解析location
            if (location!=null){
                showLocation(location);
            }
            //位置监听机制每经过5s或移动超过1米则更新location
            locationManager.requestLocationUpdates(string, 5000, 1, locationLinstener);
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager!=null){
            try {
                //移除位置更新监听
                locationManager.removeUpdates(locationLinstener);
            }catch (SecurityException e){
                e.printStackTrace();
            }

        }
    }

    //获取位置监听实例重写四个方法这里只加入了监听位置改变的方法
    LocationListener locationLinstener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //关键代码
    private void showLocation(final Location location){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //拼接url存入字符串缓冲区
                StringBuilder url = new StringBuilder();
                url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                url.append(location.getLatitude()).append(",");
                url.append(location.getLongitude());
                url.append("&sensor=false");
                String address = url.toString();
                Log.d("Debug", "url:" + url);
                //将url传入解析方法中解析
                HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        try{
//                            saveFile(response);
                            //将返回的json数据用JSONObject方法解析
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            if(0<jsonArray.length()){
                                //选择一个较准确的位置并输出
                                JSONObject object = jsonArray.getJSONObject(1);
                                Log.d("Debug", "当前位置：" + object.getString("formatted_address"));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }).start();
    }

//    private void saveFile(String address){
//
//        File file = new File("a.xml");
//
//        try {
//            file.createNewFile();
//            FileOutputStream fos = new FileOutputStream(file);
//            byte[] inbyte = address.getBytes();
//            fos.write(inbyte);
//            fos.flush();
//            fos.close();
//        }catch (IOException i){
//            i.printStackTrace();
//        }
//    }


}
