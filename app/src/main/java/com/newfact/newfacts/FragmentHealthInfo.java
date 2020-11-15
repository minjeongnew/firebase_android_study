package com.newfact.newfacts;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

public class FragmentHealthInfo extends Fragment {
    String [] sex = {" ","남성", "여성"};
    UserInfo userInfo;
    public FragmentHealthInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_health_info, container, false);

        // *** 기본 건강 정보 시작
        // 성별 -> {' ', 남성', '여성'} 스피너
        View view = inflater.inflate(R.layout.fragment_health_info, container, false);
        Spinner spinner = (Spinner)layout.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, sex);
        adapter_spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter_spinner);


        // *** 기본 건강 정보 끝

        // *** 알레르기 정보 시작
        ListView listview;

        CustomChoiceListViewAdatper adapter = new CustomChoiceListViewAdatper();

        listview = (ListView)layout.findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        adapter.addItem("우유 알레르기 ");
        adapter.addItem("대두 알레르기 ");
        adapter.addItem("복숭아 알레르기 ");
        adapter.addItem("토마토 알레르기 ");
        adapter.addItem("오징어 알레르기 ");
        // *** 알레르기 정보 끝

        return layout;
    }
}