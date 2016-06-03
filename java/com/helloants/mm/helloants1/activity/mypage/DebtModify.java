package com.helloants.mm.helloants1.activity.mypage;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.adapters.AssetModifyAdapter;
import com.helloants.mm.helloants1.data.type.BSType;
import com.helloants.mm.helloants1.db.bs.BsDB;
import com.helloants.mm.helloants1.db.bs.BsItem;

import java.util.ArrayList;


public class DebtModify extends AppCompatActivity {
    private Button mAdddebtBtn;
    private ArrayList<BSType> mList;
    BackPressCloseHandler backPressCloseHandler;
    AssetModifyAdapter assetModifyAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_debt_modify);
            //툴바 타이틀 텍스트뷰
            TextView txvTitle = (TextView) findViewById(R.id.txv_title_dif);
            txvTitle.setText("부채 초기값 입력");

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_dif);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DebtModify.this.onBackPressed();
                }
            });
            final ListView list = (ListView) findViewById(R.id.list_debt_debtinsertfrag);
            assetModifyAdapter = new AssetModifyAdapter();

            //DB에 있는 부채항목 가져와서 뿌리기
            mList = new ArrayList<BSType>();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    BsDB.INSTANCE.firstDebtFind(mList);
                }
            };

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assetModifyAdapter.setList(mList);
            assetModifyAdapter.setTag("debt");
            list.setAdapter(assetModifyAdapter);

            //부채항목 추가 버튼 눌렀을때 얼럿다이얼로그
            mAdddebtBtn = (Button) findViewById(R.id.btn_adddebt_debtinsertfrag);
            mAdddebtBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog = new Dialog(DebtModify.this);
                    dialog.setContentView(R.layout.alert_firstvalue_modify);
                    dialog.setTitle("부채항목 추가");

                    final EditText AssetName = (EditText) dialog.findViewById(R.id.alert_firstvalue_title);
                    final EditText AssetPrice = (EditText) dialog.findViewById(R.id.alert_firstvalue_content);
                    Button btnInsert = (Button) dialog.findViewById(R.id.alert_firstvaluet_insert);
                    Button btnCalcel = (Button) dialog.findViewById(R.id.alert_firstvalue_cancel);

                    final Dialog DIALOG = dialog;

                    btnCalcel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DIALOG.dismiss();
                        }
                    });
                    btnInsert.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = AssetName.getText().toString();
                            String price = AssetPrice.getText().toString();

                            if (name.equals("") || price.equals("")) {
                                Snackbar.make(v, "내용을 입력해 주세요.", Snackbar.LENGTH_SHORT).show();
                            } else if (BsItem.INSTANCE.isExisted(name, BsItem.DEBT)) {
                                Snackbar.make(v, "중복된 이름입니다", Snackbar.LENGTH_SHORT).show();
                            } else {
                                String type = "loan";
                                String firstPlot = "debt";
                                mList.add(new BSType(name + "+", Long.parseLong(price)));
                                //디비에 추가
                                BsDB.INSTANCE.newFirstAssetDebt(price, type, name + "+", firstPlot);
                                assetModifyAdapter.setList(mList);
                                list.setAdapter(assetModifyAdapter);
                                DIALOG.dismiss();
                            }
                        }
                    });
                    dialog.show();
                }
            });

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    private class BackPressCloseHandler {
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            assetModifyAdapter.refresh();
            activity.finish();
        }
    }

}