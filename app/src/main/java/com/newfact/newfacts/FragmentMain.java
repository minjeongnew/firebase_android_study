package com.newfact.newfacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.newfact.newfacts.menu.Product;
import com.newfact.newfacts.menu.UserInfo;
import com.newfact.newfacts.productDetail.Myadapter;
import com.newfact.newfacts.productDetail.SearchListActivity;

import java.util.ArrayList;

public class FragmentMain extends Fragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    EditText srch_content;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, null);


        srch_content = (EditText)view.findViewById(R.id.srch_content);


        Button SearchBtn = (Button)view.findViewById(R.id.button);
        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override // 버튼을 누르면 조회창으로 넘어가기
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchListActivity.class);
                intent.putExtra("content", srch_content.getText().toString());  // 검색어 전달
                startActivity(intent);


            }
        });



        // seekbar 설정
        Integer[] Rid_Seek_bar = {
                R.id.KcalBar, R.id.FatBar, R.id.ProteinBar, R.id.SodiumBar, R.id.SugarBar,
                R.id.CaffeineBar};
        final SeekBar seekbar[] = new SeekBar[6];

        Integer[] Rid_Seek_Text = {
                R.id.KcalAmount, R.id.FatAmount, R.id.ProteinAmount, R.id.SodiumAmount, R.id.SugarAmount,
                R.id.CaffeineAmount};
        final TextView seek_text[] = new TextView[6];
        final String[] unit = {"kcal", "g", "g", "mg", "g", "mg"}; // 단위


        for(int i=0;i<=5; i++){

            seek_text[i] = (TextView)view.findViewById(Rid_Seek_Text[i]);
            seekbar[i] = (SeekBar)view.findViewById(Rid_Seek_bar[i]);
            final int finalI = i;
            seekbar[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seek_text[finalI].setText(progress + unit[finalI]);
                }
            });


        }

        // 성분검색 버튼
        Button SearchBtn_nut = (Button)view.findViewById(R.id.button2);
        SearchBtn_nut.setOnClickListener(new View.OnClickListener() {
            @Override // 버튼을 누르면 조회창으로 넘어가기
            public void onClick(View view) {
                String nutrition_content = "";
                for(int i=0;i<=5; i++){
                    nutrition_content += seekbar[i].getProgress()+"/";
                }
                nutrition_content.replaceAll("[^0-9/]","");
                Intent intent = new Intent(getActivity(), SearchListActivity.class);
                intent.putExtra("content", nutrition_content);  // 검색어 전달
                startActivity(intent);


            }
        });


        // 추천음료 리사이클러뷰
        try {
            final RecyclerView mRecyclerView = view.findViewById(R.id.recycler_view_recommend);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            final ArrayList<Product> data = new ArrayList<>();
            Myadapter adapter = new Myadapter(data);
            mRecyclerView.setAdapter(adapter);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbRef = database.getReference().child("data");
            ValueEventListener mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    data.clear();
                    for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                        Product product = datasnapshot.getValue(Product.class);

                        // 고객맞춤정보
                        String[] srch_nutrition = new String[6];
                        UserInfo userInfo = UserInfo.getInstance();
                        if(userInfo.getNutrition()!= null){
                            srch_nutrition = userInfo.getNutrition().split("/"); }
                        String[] nutrition = product.getNutrition().split("/");
                        int flag = 0;
                        for(int i = 0;i<6; i++){
                            if(Integer.parseInt(nutrition[i]) > Integer.parseInt(srch_nutrition[i])){
                                flag = 1;
                                break; }
                        }
                        if(flag == 0){ data.add(product);
                        }
                    }


                    final Myadapter adapter = new Myadapter(data);
                    mRecyclerView.setAdapter(adapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println("error");
                }

            };
            dbRef.addValueEventListener(mValueEventListener);


        }
        catch (Exception e){
            e.printStackTrace();
        }



        return view;
    }
}
