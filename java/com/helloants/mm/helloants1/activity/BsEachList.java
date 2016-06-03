package com.helloants.mm.helloants1.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.adapters.BsEachAdapter;
import com.helloants.mm.helloants1.data.type.ISType;
import com.helloants.mm.helloants1.db.bs.BsDB;

import java.util.ArrayList;

public class BsEachList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bs_each_list);
        Intent intent = getIntent();
        String temp = intent.getStringExtra("name");
        String itemName = temp.substring(0,temp.length()-1);


        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView) findViewById(R.id.txv_title_bseachlist);
        txvTitle.setText(itemName + " 관련 경제활동");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_bseachlist);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BsEachList.this.onBackPressed();
            }
        });

        ArrayList<ISType> bsEachList =  BsDB.INSTANCE.bsItemRelatedFind(itemName);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.bseach_listview);
        BsEachAdapter bsEachAdpter = new BsEachAdapter(this,bsEachList,recyclerView);
        bsEachAdpter.mList = bsEachList;
        bsEachAdpter.size = bsEachList.size();
        bsEachAdpter.notifyDataSetChanged();
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(bsEachAdpter);
    }
}