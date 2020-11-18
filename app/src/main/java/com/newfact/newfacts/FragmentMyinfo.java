package com.newfact.newfacts;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class FragmentMyinfo extends Fragment {

    // TODO: Rename and change types of parameters
    String [] list_menu = { "건강 정보", "영양 성분 수치 제한하기","로그아웃"};
    public FragmentMyinfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.fragment_myinfo, container, false);
        ListView listView = (ListView)layout.findViewById(R.id.listView_Myinfo);

        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                list_menu
        );


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    ((MainActivity)getActivity()).onFragmentChange(position);
                }
                else if(position ==1 ){
                    ((MainActivity)getActivity()).onFragmentChange(position);
                }
                else if(position ==2){
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getActivity(),"로그아웃",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }

            }
        });
        listView.setAdapter(listViewAdapter);

        return layout;
    }
}