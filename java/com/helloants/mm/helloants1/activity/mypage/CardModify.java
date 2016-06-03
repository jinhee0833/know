package com.helloants.mm.helloants1.activity.mypage;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.data.DeviceSize;
import com.helloants.mm.helloants1.db.bs.BsItem;
import com.helloants.mm.helloants1.db.member.MemberDB;

import java.util.Set;
import java.util.TreeSet;

public class CardModify extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_modify);

        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView) findViewById(R.id.txv_title_cardmodify);
        txvTitle.setText("카드관리");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_cardmodify);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardModify.this.onBackPressed();
            }
        });

        //카드 데이터 가지고 오기
        Set set = MemberDB.INSTANCE.myCardFind();
        if (set.isEmpty()) {
            ((TextView) findViewById(R.id.txv_checkcard_cardmodify)).setText("불러올 카드가 없습니다.");
            ((TextView) findViewById(R.id.txv_creditcard_cardmodify)).setVisibility(View.GONE);
        } else {
            Set myCard = new TreeSet(set);
            LinearLayout checkRoot = (LinearLayout) findViewById(R.id.checkcard_linear);
            LinearLayout creditRoot = (LinearLayout) findViewById(R.id.creditcard_linear);

            //BsItem에 들어있는 자산 부채 목록
            String[] AssetArray = BsItem.INSTANCE.getAssetArrayy();
            final ArrayAdapter<String> checkAdapter = new ArrayAdapter<String>(CardModify.this, android.R.layout.select_dialog_singlechoice);

            for (int i = 0; i < AssetArray.length; i++) {
                String temp = AssetArray[i];
                String name = temp.substring(0, temp.length() - 1);
                checkAdapter.add(name);
            }

            final ArrayAdapter<String> creditAdapter = new ArrayAdapter<String>(CardModify.this, android.R.layout.select_dialog_singlechoice);
            String[] DebtArray = BsItem.INSTANCE.getDebtArray();
            for (int i = 0; i < DebtArray.length; i++) {
                String temp = DebtArray[i];
                String name = temp.substring(0, temp.length() - 1);
                creditAdapter.add(name);
            }

            try {
                for (Object card : myCard) {
                    final String tempCardName = String.valueOf(card);
                    final String cardN[] = tempCardName.split("~");
                    if (cardN[1].equals("credit")) {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout linearLayout = new LinearLayout(this);
                        View v = inflater.inflate(R.layout.item_credit_modify, linearLayout);

                        TextView lockedItem = (TextView) v.findViewById(R.id.locked_item);
                        lockedItem.setText("연동 부채");
                        TextView cardName = (TextView) v.findViewById(R.id.txv_cardname_cardmodify);
                        cardName.setText(cardN[0]);

                        final Button cardButton = (Button) v.findViewById(R.id.btn_card_cardmodify);
                        cardButton.setMinWidth(DeviceSize.mWidth/3);
                        cardButton.setMaxWidth(DeviceSize.mWidth / 3);
                        boolean isEqual = false;
                        for (int i = 0; i < DebtArray.length; ++i) {
                            if (DebtArray[i].equals(cardN[2] + "+")) isEqual = true;
                        }
                        if (isEqual) cardButton.setText(cardN[2]);
                        else cardButton.setText("없음");

                        cardButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CardModify.this);
                                alertBuilder.setIcon(R.drawable.ic_card);
                                alertBuilder.setTitle("연동될 부채를 선택하세요.");

                                alertBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                alertBuilder.setAdapter(creditAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String name = creditAdapter.getItem(which);
                                        AlertDialog.Builder innBuilder = new AlertDialog.Builder(CardModify.this);
                                        innBuilder.setTitle(name + "로(으로) 설정하시겠습니까?");
                                        innBuilder.setPositiveButton(
                                                "확인",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        MemberDB.INSTANCE.modifyOneCard(tempCardName, cardN[0] + "~credit~" + name);
                                                        cardButton.setText(name);
                                                        dialog.dismiss();
                                                    }
                                                });
                                        innBuilder.show();
                                    }
                                });
                                alertBuilder.show();
                            }
                        });

                        final Button accountBtn = (Button) v.findViewById(R.id.btn_card_accountmodify);
                        accountBtn.setMinWidth(DeviceSize.mWidth/3);
                        accountBtn.setMaxWidth(DeviceSize.mWidth / 3);
                        try {
                            isEqual = false;
                            for (int i = 0; i < AssetArray.length; ++i) {
                                if (AssetArray[i].equals(cardN[3] + "+")) isEqual = true;
                            }
                            if (isEqual) {
                                if(cardN[3].length() > 10){
                                    accountBtn.setText(cardN[3].substring(0,9)+"…");
                                }else{
                                    accountBtn.setText(cardN[3]);
                                }
                            }
                            else accountBtn.setText("없음");
                        } catch (Exception e) {
                            accountBtn.setText("없음");
                        }
                        accountBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CardModify.this);
                                alertBuilder.setIcon(R.drawable.ic_card);
                                alertBuilder.setTitle("연동될 계좌를 선택하세요.");

                                alertBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                alertBuilder.setAdapter(checkAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String name = checkAdapter.getItem(which);
                                        AlertDialog.Builder innBuilder = new AlertDialog.Builder(CardModify.this);
                                        innBuilder.setTitle(name + "로(으로) 설정하시겠습니까?");
                                        innBuilder.setPositiveButton(
                                                "확인",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        MemberDB.INSTANCE.modifyOneCard(tempCardName, cardN[0] + "~credit~" + cardButton.getText() + "~" + name);
                                                        accountBtn.setText(name);
                                                        dialog.dismiss();
                                                    }
                                                });
                                        innBuilder.show();
                                    }
                                });
                                alertBuilder.show();
                            }
                        });

                        creditRoot.addView(linearLayout);
                    } else {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout linearLayout = new LinearLayout(this);
                        View v = inflater.inflate(R.layout.item_cardmodify, linearLayout);

                        TextView lockedItem = (TextView) v.findViewById(R.id.locked_item);
                        lockedItem.setText("연동 계좌");
                        TextView cardName = (TextView) v.findViewById(R.id.txv_cardname_cardmodify);
                        cardName.setText(cardN[0]);
                        final Button cardButton = (Button) v.findViewById(R.id.btn_card_cardmodify);
                        boolean isEqual = false;
                        cardButton.setMinWidth(DeviceSize.mWidth/3);
                        cardButton.setMaxWidth(DeviceSize.mWidth/3);
                        for (int i = 0; i < AssetArray.length; ++i) {
                            if (AssetArray[i].equals(cardN[2] + "+")) isEqual = true;
                        }
                        if (isEqual) {
                            if(cardN[2].length() > 10){
                                cardButton.setText(cardN[2].substring(0,9)+"…");
                            }else{
                                cardButton.setText(cardN[2]);
                            }
                        }
                        else cardButton.setText("없음");

                        cardButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CardModify.this);
                                alertBuilder.setIcon(R.drawable.ic_card);
                                alertBuilder.setTitle("연동될 계좌를 선택하세요.");

                                alertBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                alertBuilder.setAdapter(checkAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String name = checkAdapter.getItem(which);
                                        AlertDialog.Builder innBuilder = new AlertDialog.Builder(CardModify.this);
                                        innBuilder.setTitle(name + "로(으로) 설정하시겠습니까?");
                                        innBuilder
                                                .setPositiveButton(
                                                        "확인",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                MemberDB.INSTANCE.modifyOneCard(tempCardName, cardN[0] + "~check~" + name);
                                                                cardButton.setText(name);
                                                                dialog.dismiss();
                                                            }
                                                        });
                                        innBuilder.show();
                                    }
                                });
                                alertBuilder.show();
                            }
                        });
                        checkRoot.addView(linearLayout);
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}