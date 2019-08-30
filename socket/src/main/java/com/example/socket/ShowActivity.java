package com.example.socket;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends Activity {
    private RecyclerView rec;
    List<UserBean> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
//        rec= (RecyclerView) findViewById(R.id.rec);
//        list=new ArrayList<>();
//        for (int i = 0; i <16; i++) {
//            list.add(new UserBean("10.55.200.66","周立国","1","老山-四惠"));
//        }
//        rec.setLayoutManager(new LinearLayoutManager(this));
//        RecAdapter recAdapter=new RecAdapter(this,list);
//        rec.setAdapter(recAdapter);
//        recAdapter.setOnItemClickListener(new RecAdapter.ItemListener() {
//            @Override
//            public void onItemclick(int i,String ip) {
//                Toast.makeText(ShowActivity.this,"位置"+i+"，ip"+ip,Toast.LENGTH_LONG).show();
//            }
//        });

    }
}
