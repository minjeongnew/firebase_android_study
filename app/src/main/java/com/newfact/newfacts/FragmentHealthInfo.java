package com.newfact.newfacts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.newfact.newfacts.menu.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

// 사용자의 건강 데이터를 입력하는 프래그먼트
public class FragmentHealthInfo extends Fragment {
    String [] sex_ = {"남성", "여성"};
    String [] allergy = {"우유 알레르기",
                        "대두 알레르기",
                        "복숭아 알레르기",
                        "토마토 알레르기",
                        "오징어 알레르기"};
    UserInfo userInfo = UserInfo.getInstance();

    DatabaseReference mDBReference = null;
    HashMap<String, Object> childUpdates = null;
    Map<String, Object> userValue = null;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    String user_id = user.getUid();
    String user_sex ="";
    String user_age = "";
    String user_height="";
    String user_weight="";
    Boolean user_milk_allergy=false;
    Boolean user_soybean_allergy=false;
    Boolean user_peach_allergy=false;
    Boolean user_tomato_allergy=false;
    Boolean user_squid_allergy=false;
    String user_allergy = "";
    String user_nutrition = "";

    Button buttonSaveUserInfo;
    EditText editTextAge;
    EditText editTextHeight;
    EditText editTextWeight;

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
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_health_info, container, false);

        mDBReference = FirebaseDatabase.getInstance().getReference();// 파이어베이스 참조
        buttonSaveUserInfo = (Button)layout.findViewById(R.id.buttonSaveUserInfo); // 사용자의 데이터를 파이어베이스에 저장하는 버튼
        editTextAge = (EditText)layout.findViewById(R.id.editTextAge); // 나이를 입력하는 editText
        editTextHeight = (EditText)layout.findViewById(R.id.editTextHeight); // 키를 입력하는 editText
        editTextWeight = (EditText)layout.findViewById(R.id.editTextWeight); // 몸무게를 입력하는 editText

        final Spinner spinner = (Spinner)layout.findViewById(R.id.spinner); // 성별을 선택하는 spinner
        // spinner: 성별 -> {' ', 남성', '여성'}
        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, sex_);
        adapter_spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user_sex = sex_[position];
            }

            @Override // 아무것도 선택 안 되었을때
            public void onNothingSelected(AdapterView<?> parent) {
                user_sex = "";
            }
        });
        // 스피너 끝

        // *** 리스트뷰 항목에 알레르기 추가
        final ListView listview;
        final CustomChoiceListViewAdapter adapter = new CustomChoiceListViewAdapter();
        listview = (ListView)layout.findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        for(int i=0;i<allergy.length;i++){
            adapter.addItem(allergy[i]);
        }

        // 메인 액티비티 시작 시 파이어베이스에 사용자 데이터가 있을 경우 싱글톤인 UserInfo에 사용자 데이터를 입력한다
        // FragmentHealthInfo 는 UserInfo에 사용자 데이터가 있을 경우 데이터들을 반영해 놓는다
        // 만약 데이터가 없을 경우에는 각 칸들은 비워져 있다
        if(userInfo.getAge()!=null){
            editTextAge.setText(userInfo.getAge());
        }
        if (userInfo.getSex()!=null){
            if(userInfo.getSex() == "남성"){ spinner.setSelection(0);}
            else if(userInfo.getSex() =="여성"){spinner.setSelection(1);}
        }
        if (userInfo.getHeight() !=null){
            editTextHeight.setText(userInfo.getHeight());
        }
        if (userInfo.getWeight() != null ){
            editTextWeight.setText(userInfo.getWeight());
        }
        if (userInfo.getAllergy() != null){
            String[] user_allergy_data = userInfo.getAllergy().split("/");
            for(int i=0;i<5;i++){
                if(user_allergy_data[i].equals("1")){
                    listview.setItemChecked(i, true);
                }
            }
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        user_milk_allergy = true;
                        break;
                    case 1:
                        user_soybean_allergy = true;
                        break;
                    case 2:
                        user_peach_allergy = true;
                        break;
                    case 3:
                        user_tomato_allergy = true;
                        break;
                    case 4:
                        user_squid_allergy =true;
                        break;
                }
            }
        });
        // '저장' 버튼 클릭 시 파이어베이스에 데이터를 저장한다
        buttonSaveUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 나이
                if(editTextAge.getText().toString().length()==0){
                    user_age = "";
                } else{
                    user_age = editTextAge.getText().toString();
                }
                // 나이 끝

                // 키
                if(editTextHeight.getText().toString().length()==0){
                    user_height = "";
                } else{
                    user_height = editTextHeight.getText().toString();

                }
                // 키 끝

                // 몸무게
                if(editTextWeight.getText().toString().length()==0){
                    user_weight = "";
                } else{
                    user_weight = editTextWeight.getText().toString();
                }
                // 몸무게 끝

                childUpdates = new HashMap<>();
                user_allergy = allergyToString(user_milk_allergy)+"/"+
                allergyToString(user_soybean_allergy)+"/"+
                allergyToString(user_peach_allergy)+"/"+
                allergyToString(user_tomato_allergy)+"/"+
                allergyToString(user_squid_allergy)+"/0";

                mDBReference.child("/UserInfo/"+user_id).child("id").setValue(user_id);
                mDBReference.child("/UserInfo/"+user_id).child("sex").setValue(user_sex);
                mDBReference.child("/UserInfo/"+user_id).child("age").setValue(user_age);
                mDBReference.child("/UserInfo/"+user_id).child("height").setValue(user_height);
                mDBReference.child("/UserInfo/"+user_id).child("weight").setValue(user_weight);
                mDBReference.child("/UserInfo/"+user_id).child("allergy").setValue(user_allergy);
                Toast.makeText(getActivity(), "저장에 성공했습니다", Toast.LENGTH_SHORT).show();
            }
        });
        // 파이어베이스 코드 부분 끝
        return layout;
    }
    public String allergyToString(boolean allergy){
        if(allergy) return "1";
        else return "0";
    }
}