package com.newfact.newfacts;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.newfact.newfacts.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.LOCATION_SERVICE;

public class FragmentLocation extends Fragment {
    String[] permission_list = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    LocationManager locationManager;
    GoogleMap map;
    ArrayList<Double> lat_lst = new ArrayList<Double>();
    ArrayList<Double> lng_lst = new ArrayList<Double>();
    ArrayList<String> name_lst = new ArrayList<String>();
    ArrayList<String> vicinity_lst = new ArrayList<String>();
    ArrayList<Marker> marker_lst = new ArrayList<Marker>();
    ArrayList<MapItem> item_lst = new ArrayList<MapItem>();

    ListView listView;
    MyAdapter adapter;
    Location currentLocation;
    Location cafeLocation;
    android.app.ActionBar actionBar;
    View header;
    androidx.appcompat.widget.AppCompatTextView title_bar;

    String url;
    String rating;
    String assign;
    boolean isRunning = false;

    DisplayImgHandler handler;
    RatingDialog dialog;
    SetImgThread setImgThread;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location, container, false);

        // custom title_bar
//        actionBar.setTitle("주변 매장 정보 ");
//        actionBar = getActivity().getActionBar();
//        actionBar.setDisplayShowCustomEnabled(false);
//        actionBar.setDisplayOptions(actionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setCustomView(R.layout.bar_layout);
//
//        header = getLayoutInflater().inflate(R.layout.bar_layout, null, false);
//        title_bar = header.findViewById(R.id.custom_bar);
//        title_bar.setText("주변 매장 정보");

        listView = (ListView) v.findViewById(R.id.listview);
        adapter = new MyAdapter();
        handler = new DisplayImgHandler();

        // 알림창 띄우기
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setMessage("위생등급 데이터를 가져오고 있습니다.");
        b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = b.create();
        alertDialog.show();

        // 권환 요청 및 구글 api 받아오기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission_list, 0);
        } else {
            init();
        }

        return v;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return;
            }
        }
        init();
    }

    public void init() {
        FragmentManager fragmentManager = this.getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        MapReadyCallback callback1 = new MapReadyCallback();
        mapFragment.getMapAsync(callback1);
    }


    // 구글 지도 사용 준비가 완료되면 동작하는 콜백
    class MapReadyCallback implements OnMapReadyCallback {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            // 현재 위치
            getMyLocation();
        }
    }

    // 현재 위치를 측정하는 메서드
    public void getMyLocation() {
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        // 권한 확인 작업
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                return;
            }
        }

        // 이전에 측정했던 값을 가져옴
        Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location1 != null) {
            setMyLocation(location1);
        } else {
            if (location2 != null) {
                setMyLocation(location2);
            }
        }
        // 새롭게 측정함
        GetMyLocationListener listener = new GetMyLocationListener();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, listener);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10f, listener);
        }
    }

    public void setMyLocation(Location location) {

        currentLocation = new Location("current");
        currentLocation.setLatitude(location.getLatitude());
        currentLocation.setLongitude(location.getLongitude());
//        Log.d("test123", "위도: " + currentLocation.getLatitude());
//        Log.d("test123", "경도: " + currentLocation.getLongitude());

        // 위도와 경도값을 관리하는 객체
        LatLng position = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        CameraUpdate update1 = CameraUpdateFactory.newLatLng(position);
        CameraUpdate update2 = CameraUpdateFactory.zoomTo(15f);
        map.moveCamera(update1);
        map.animateCamera(update2);

        // 권한 확인 작업
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try{
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    return;
                }
            }
            catch (Exception e){
                return;
            }

        }

        // 현재 위치 표시
        map.setMyLocationEnabled(true);

        NetworkThread thread = new NetworkThread(location.getLatitude(), location.getLongitude());
        thread.start();
    }

    // 현재 위치 측정이 성공하면 반응하는 리스너
    class GetMyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            //setMyLocation(location);
            locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }

    // 구글 서버에서 주변 정보를 받아오기 위한 스레드
    class NetworkThread extends Thread {
        double lat, lng;

        NetworkThread(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public void run() {
            super.run();

            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder();

            String site = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                    + "?key=AIzaSyDJ0mbhei8kzTawEdyKLL1uZLE4QPMCwUs"
                    + "&location=" + lat + "," + lng
                    + "&radius=800"
                    + "&language=ko"
                    + "&type=cafe";
            //Log.d("test123", "주소 : " + site);
            builder = builder.url(site);
            Request request = builder.build();

            Callback callback1 = new Callback1();
            Call call = client.newCall(request);
            call.enqueue(callback1);

        }
    }

    class Callback1 implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                String result = response.body().string();
                //("test123", result);

                JSONObject obj = new JSONObject(result);

                String status = obj.getString("status");
                if (status.equals("OK")) {
                    JSONArray results = obj.getJSONArray("results");
                    lat_lst.clear();
                    lng_lst.clear();
                    name_lst.clear();
                    vicinity_lst.clear();

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject obj2 = results.getJSONObject(i);
                        JSONObject geometry = obj2.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        double lat2 = location.getDouble("lat");
                        double lng2 = location.getDouble("lng");

                        String name = obj2.getString("name");
                        String vicinity = obj2.getString("vicinity");

                        lat_lst.add(lat2);
                        lng_lst.add(lng2);
                        name_lst.add(name);
                        vicinity_lst.add(vicinity);

                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            float distance = 0;
                            MapItem item;
                            isRunning = true;
                            cafeLocation = new Location("cafe");
                            // 지도에 표시되어있는 마커 제거
                            for (Marker marker : marker_lst) {
                                marker.remove();
                            }
                            marker_lst.clear();

                            for (int i = 0; i < lat_lst.size(); i++) {
                                double lat3 = lat_lst.get(i);
                                double lng3 = lng_lst.get(i);
                                String name3 = name_lst.get(i);
                                String vicinity3 = vicinity_lst.get(i);

                                LatLng position = new LatLng(lat3, lng3);
                                MarkerOptions option = new MarkerOptions();
                                option.position(position);

                                option.title(name3);
                                option.snippet(vicinity3);

                                // 마커 이미지 변경
//                                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker);
//                                option.icon(bitmap);

                                Marker marker = map.addMarker(option);
                                marker_lst.add(marker);

                                // 리스트뷰에 아이템 추가
                                cafeLocation = new Location("cafe");
                                cafeLocation.setLatitude(lat3);
                                cafeLocation.setLongitude(lng3);
                                distance = currentLocation.distanceTo(cafeLocation);

                                // 잘못된 정보 거르기
                                if (0 < name3.length() && name3.length() < 20) {
                                    // 주변 정보 담아서 listview에 보이기
                                    item = new MapItem(name3, String.format("%.2f", distance) + " m");
                                    setImgThread = new SetImgThread(item, vicinity3);
                                    //Log.d("Test", vicinity3);
                                    setImgThread.start();
                                    adapter.addItem(item);
                                    listView.setAdapter(adapter);

                                }
                            }

                            listView.setAdapter(adapter);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MyAdapter extends BaseAdapter implements View.OnClickListener {
        ArrayList<MapItem> items = new ArrayList<MapItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(MapItem item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MapItemView mapItemView = null;
            if (convertView == null) {
                mapItemView = new MapItemView(getActivity().getApplicationContext());
            } else {
                mapItemView = (MapItemView) convertView;
            }

            MapItem item = items.get(position);
            mapItemView.setCafeName(item.getCafe_name());
            mapItemView.setDistance(item.getDistance());
            mapItemView.setImg(item.getResId());

            // 리스트뷰 아이템 클릭 처리
            mapItemView.setTag(position);
            mapItemView.setOnClickListener(this);

            return mapItemView;
        }

        @Override
        public void onClick(View v) {
            int pos = (Integer) v.getTag();
            MapItem item = (MapItem) getItem(pos);

            // 아이템 목록 클릭 시
            dialog = new RatingDialog(item.getCafe_name(), item.getDistance(), item.getAssign(), item.getRating());
            dialog.show(getActivity().getSupportFragmentManager(), "tag");


        }
    }

    // 식품안전나라에서 open api 정보 가져오기
    class OpenAPI extends AsyncTask<Void, Void, String> {
        String url;
        String vicinity;

        OpenAPI(String url, String vicinity) {
            this.url = url;
            this.vicinity = vicinity;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = null;

            try {
                documentBuilder = dbFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            Document doc = null;

            try {
                doc = documentBuilder.parse(url);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }

            try {
                doc.getDocumentElement().normalize();
                //   Log.d("Test_url" , url);
                NodeList nodeList = doc.getElementsByTagName(doc.getDocumentElement().getNodeName());
                //Log.d("len: ", ""+ nodeList.getLength());
                System.out.println("Test Root element: " + doc.getDocumentElement().getNodeName());
//                Node node = nodeList.item(1);
//                Log.d("Test_element: ", node.getNodeValue());

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
//                    Log.d("Test", "for문 들어왔다");
//                    Log.d("Test_element: ", node.getNodeValue());
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        //      Log.d("Test", "if문 들어왔다");
                        Element element = (Element) node;

                        //    Log.d("Test", getTagValue("CODE", element));
                        // 위생 등급과 지정기관 result에 저장
                        if (getTagValue("CODE", element).equals("INFO-000")) {
//                            Log.d("OPEN_API", "이름: " + getTagValue("BSSH_NM", element));
//                            Log.d("OPEN_API", "위생등급: " + getTagValue("HG_ASGN_LV", element));

                            // 주소가 같은지 비교
                            if(getTagValue("ADDR", element).contains(vicinity)){
                                result = getTagValue("HG_ASGN_LV", element);
                                result += ","+ getTagValue("HG_ASGN_NM", element);
                            }
                        }

                    }
//                    if(result.length() > 1) {
//                        break;
//                    }
                }
            }
            // 위생등급 데이터가 없는 경우
            catch (Exception e) {
                //Log.d("ds", "null");
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    String getTagValue(String tag, Element element) {
        NodeList nList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nList.item(0);
        if (nValue == null) {
            return null;
        }
        return nValue.getNodeValue();
    }

    // open api 에서 위생 등급과 지정 기관 받아오는 스레드
    class SetImgThread extends Thread{
        MapItem item;
        String vicinity;

        SetImgThread(MapItem item, String vicinity){
            this.item = item;
            this.vicinity = vicinity;
        }

        @Override
        public void run() {
            super.run();
            String result = "";
            OpenAPI data = null;
            String temp_cafe_name = "";

            if(item.getCafe_name().contains(" ")){
                temp_cafe_name = item.getCafe_name().split(" ")[0];
            }
            else{
                temp_cafe_name = item.getCafe_name();
            }

            for(int k = 1; k < 13000; k += 1000){
                url = "http://openapi.foodsafetykorea.go.kr/api/e740909b02604cfcb677/C004/xml/";
                if(k == 12001){
                    url += Integer.toString(k) + "/" + Integer.toString(12400) + "/" +
                            "UPSO_NM=" + temp_cafe_name;
                }else{
                    url += Integer.toString(k) + "/" + Integer.toString(k+999) + "/" +
                            "UPSO_NM=" + temp_cafe_name;
                }
                data = new OpenAPI(url, vicinity);

                try {
                    result = data.execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(result.length() != 0){
                    rating = result.split(",")[0];
                    assign = result.split(",")[1];
                }
                else{
                    rating = "";
                    assign = "";
                }

                if (result.length() == 0) {
                    item.setResId(R.drawable.null_space);
                    item.setAssign("데이터가 없습니다.");
                    item.setRating("데이터가 없습니다.");
                }
                else if (result.contains("우수") || result.contains("좋음")){
                    item.setResId(R.drawable.green_place);
                    item.setAssign(assign);
                    item.setRating(rating);
                }
                else{
                    item.setResId(R.drawable.red_place);
                    item.setAssign(assign);
                    item.setRating(rating);
                }
                //Log.d("Test", item.getCafe_name() + " , "  + item.getAssign());

                Message msg1 = handler.obtainMessage();
                msg1.what = 0;
                msg1.obj = item;
                handler.sendMessage(msg1);

                Message msg2 = handler.obtainMessage();
                msg2.what = 1;
                msg2.obj = result;
                handler.sendMessage(msg2);

//                    if(result.length() > 1) break;
            }

            //Log.d("Test", "Thread_result: " + result);


        }
    }

    class DisplayImgHandler extends Handler{
        MapItem item;
        String rating;
        String assign;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 0:
                    item = (MapItem) msg.obj;
                    break;
                case 1:
                    String result = (String) msg.obj;
                    if(result.length() != 0){
                        rating = result.split(",")[0];
                        assign = result.split(",")[1];
                    }
                    else{
                        rating = "";
                        assign = "";
                    }

//                     Log.d("Test", "Handler_rating: " + rating);
//                     Log.d("Test", "Handler_assign: " + assign);
                    //Log.d("Test", "Item.name" + item.getCafe_name());
                    if (result.length() == 0) {
                        item.setResId(R.drawable.null_space);
                        item.setAssign("데이터가 없습니다.");
                        item.setRating("데이터가 없습니다.");
                    }
                    else if (result.contains("우수") || result.contains("좋음")){
                        item.setResId(R.drawable.green_place);
                        item.setAssign(assign);
                        item.setRating(rating);
                    }
                    else{
                        item.setResId(R.drawable.red_place);
                        item.setAssign(assign);
                        item.setRating(rating);
                    }

                    break;
            }
            adapter.notifyDataSetChanged();
            //listView.setAdapter(adapter);
        }
    }


    // setImgThread 종료
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

}
