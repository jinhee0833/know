package com.helloants.mm.helloants1.activity.mypage;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.adapters.AssetModifyAdapter;
import com.helloants.mm.helloants1.data.type.BSType;
import com.helloants.mm.helloants1.db.bs.BsDB;
import com.helloants.mm.helloants1.db.bs.BsItem;

import java.util.ArrayList;


public class AssetModify extends AppCompatActivity {
    private Button mAddassetBtn;
    private ArrayList<BSType> mList;
    private boolean mIsBs;
    private Button mAssetInsertBtn;
    BackPressCloseHandler backPressCloseHandler;
    AssetModifyAdapter assetModifyAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_modify);

            setContentView(R.layout.activity_asset_modify);

            //툴바 타이틀 텍스트뷰
            TextView txvTitle = (TextView) findViewById(R.id.txv_title_aif);
            txvTitle.setText("자산 초기값 입력");

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_aif);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AssetModify.this.onBackPressed();
                }
            });

            final ListView list = (ListView) findViewById(R.id.list_asset_assetinsertfrag);
            assetModifyAdapter = new AssetModifyAdapter();

            //디비에 있는 자산항목 가져와서 뿌리기
            mList = new ArrayList<BSType>();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    BsDB.INSTANCE.firstAssetFind(mList);
                }
            };

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assetModifyAdapter.setList(mList);
            assetModifyAdapter.setTag("asset");
            list.setAdapter(assetModifyAdapter);

            //자산항목 추가 버튼 눌렀을때 얼럿다이얼로그
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.asset_modify_dialog, null);
            mAddassetBtn = (Button) findViewById(R.id.btn_addasset_assetinsertfrag);
            mAddassetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AssetModify.this)
                            .setTitle("자산항목 추가")
                            .setView(dialogView)
                            .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //추가 버튼 눌렀을때
                                    EditText editText = (EditText) dialogView.findViewById(R.id.dialog_edit);
                                    String name = editText.getText().toString();

                                    EditText editTextPrice = (EditText) dialogView.findViewById(R.id.dialog_edit_price);
                                    String price = editTextPrice.getText().toString();

                                    RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.dialog_rg);
                                    int checkedId = radioGroup.getCheckedRadioButtonId();
                                    String type = "";
                                    switch (checkedId) {
                                        case R.id.dialog_rb_save:
                                            type = "save";
                                            break;
                                        case R.id.dialog_rb_house:
                                            type = "house";
                                            break;
                                        case R.id.dialog_rb_income:
                                            type = "income";
                                            break;
                                        case R.id.dialog_rb_car:
                                            type = "car";
                                            break;
                                        case R.id.dialog_rb_etc:
                                            type = "etc";
                                            break;
                                    }

                                    String firstPlot = "asset";

                                    if (editText.getText().toString().equals("") || editTextPrice.getText().toString().equals("") || checkedId == -1) {
                                        Toast.makeText(AssetModify.this, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show();
                                        editText.setText("");
                                        editTextPrice.setText("");
                                        radioGroup.clearCheck();
                                        ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                                    } else if (BsItem.INSTANCE.isExisted(editText.getText().toString(), BsItem.ASSET)) {
                                        Toast.makeText(AssetModify.this, "중복된 이름입니다", Toast.LENGTH_SHORT).show();
                                        editText.setText("");
                                        editTextPrice.setText("");
                                        radioGroup.clearCheck();
                                        ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                                    } else {
                                        mList.add(new BSType(name + "+", Long.parseLong(price)));
                                        //디비에 추가
                                        BsDB.INSTANCE.newFirstAssetDebt(price, type, name + "+", firstPlot);
                                        assetModifyAdapter.setList(mList);
                                        list.setAdapter(assetModifyAdapter);
                                        editText.setText("");
                                        editTextPrice.setText("");
                                        radioGroup.clearCheck();
                                        ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                                    }
                                }
                            });
                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                        }
                    });
                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                        }
                    });

                    try {
                        AlertDialog dialog = alertDialog.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    } catch (Exception e) {
                    }
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