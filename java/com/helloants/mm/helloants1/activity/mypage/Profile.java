package com.helloants.mm.helloants1.activity.mypage;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.login.LoginActivity;
import com.helloants.mm.helloants1.data.DeviceSize;
import com.helloants.mm.helloants1.data.constant.Icon;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.db.mypage.ProfileDB;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;


public class Profile extends AppCompatActivity {
    private LinearLayout mLogout;
    public static Activity mMainActivity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        try {

            //툴바 타이틀 텍스트뷰
            TextView txvTitle = (TextView) findViewById(R.id.txv_title_pro);
            txvTitle.setText("프로필");

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_pro);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Profile.this.onBackPressed();
                }
            });

            mLogout = (LinearLayout) findViewById(R.id.llay_logout_profile);

            Typeface fontFamily = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/fontawesome.ttf");
            TextView name = (TextView) findViewById(R.id.profile_name);
            TextView email = (TextView) findViewById(R.id.profile_email);
            TextView gender = (TextView) findViewById(R.id.profile_gender);
            TextView birth = (TextView) findViewById(R.id.profile_birth);
            TextView logoutIcon = (TextView) findViewById(R.id.txv_logout_icon_profile);
            com.github.siyamed.shapeimageview.CircularImageView circle = (CircularImageView) findViewById(R.id.circle_image);

            logoutIcon.setTypeface(fontFamily);
            logoutIcon.setText(Icon.LOGOUT);

            ProfileDB.INSTANCE.settingData();
            name.setText(" " + ProfileDB.INSTANCE.getUserDate().mName);
            email.setText(" " + ProfileDB.INSTANCE.getUserDate().mEmail);
            gender.setText(" " + ProfileDB.INSTANCE.getUserDate().mGender);
            birth.setText(" " + ProfileDB.INSTANCE.getUserDate().mBirth);

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ui_sam2);
            Bitmap bm2 = bm.createScaledBitmap(bm, DeviceSize.mWidth / 4, DeviceSize.mWidth / 4, false);
            circle.setImageBitmap(bm2);

            mLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(Profile.this)
                            .setTitle("로그아웃")
                            .setMessage("로그아웃 하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String joinPath = "";
                                    try {
                                        joinPath = Cryptogram.Decrypt(LoginData.mJoinPath);
                                    } catch (Exception e) {
                                    }

                                    final String JOINPATH = joinPath;
                                    if (JOINPATH.equals("naver")) MemberDB.INSTANCE.naverLogout();
                                    else if (JOINPATH.equals("facebook"))
                                        MemberDB.INSTANCE.fbLogout();
                                    else MemberDB.INSTANCE.logout();

                                    startActivity(new Intent(Profile.this, LoginActivity.class));
                                    if (mMainActivity != null) mMainActivity.finish();
                                    Profile.this.finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            });
        } catch (Exception e) {
        }
    }
}