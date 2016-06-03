package com.helloants.mm.helloants1.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.data.network.GetNetState;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.loading.WaitDlg;
import com.mongodb.BasicDBObject;

public class Join extends AppCompatActivity {
    private Button mJoinBtn;
    private Button mCancleBtn;
    private EditText mIdEdit;
    private EditText mPwEdit;
    private EditText mPwcheckEdit;
    private EditText mNameEdit;
    private EditText mBirthEdit;
    private RadioButton mFemaleRadio;
    private RadioButton mMaleRadio;
    private Toast mIdToast;
    private Toast mPwToast;
    private Toast mNameToast;
    private Toast mBirthToast;
    private Toast mPwcheckfailToast;
    private String mJoinPath;
    private BackPressCloseHandler backPressCloseHandler;
    public static Activity mLoginActivity;
    private WaitDlg mWaitDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_join);

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_aif);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Join.this.onBackPressed();
                }
            });

            mJoinBtn = (Button) findViewById(R.id.btn_join_join);
            mIdEdit = (EditText) findViewById(R.id.edit_id_join);
            mPwEdit = (EditText) findViewById(R.id.edit_pw_join);
            mPwcheckEdit = (EditText) findViewById(R.id.edit_pwcheck_join);
            mNameEdit = (EditText) findViewById(R.id.edit_name_join);
            mBirthEdit = (EditText) findViewById(R.id.edit_birth_join);
            mFemaleRadio = (RadioButton) findViewById(R.id.radio_fmale_join);
            mMaleRadio = (RadioButton) findViewById(R.id.radio_male_join);
            mIdToast = Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT);
            mPwToast = Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT);
            mNameToast = Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT);
            mBirthToast = Toast.makeText(this, "생년월일은 6자리로 입력해주세요", Toast.LENGTH_SHORT);
            mPwcheckfailToast = Toast.makeText(this, "비밀번호와 비밀번호확인이 서로 다릅니다.", Toast.LENGTH_SHORT);

            Intent intent = getIntent();
            if (intent.getStringExtra("fbEmail") != null) {
                mIdEdit.setText(intent.getStringExtra("fbEmail"));
                mNameEdit.setText(intent.getStringExtra("fbName"));
                mBirthEdit.setText(intent.getStringExtra("fbBirthday"));
                if (intent.getStringExtra("fbGender").equals("male")) mMaleRadio.performClick();
                else mFemaleRadio.performClick();
                mJoinPath = intent.getStringExtra("joinPath");
            } else if (intent.getStringExtra("naverEmail") != null) {
                mIdEdit.setText(intent.getStringExtra("naverEmail"));
                mNameEdit.setText(intent.getStringExtra("naverName"));
                mBirthEdit.setText(intent.getStringExtra("naverBirthday"));
                if (intent.getStringExtra("naverGender").equals("M")) mMaleRadio.performClick();
                else mFemaleRadio.performClick();
                mJoinPath = intent.getStringExtra("joinPath");
            } else {
                mJoinPath = "general";
            }

            mJoinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                        if (mIdEdit.getText().toString().equals("")) {
                            mIdToast.show();
                        } else if (mPwEdit.getText().toString().equals("")) {
                            mPwToast.show();
                        } else if (mPwEdit.getText().toString().length() < 8) {
                            Toast.makeText(Join.this, "비밀번호는 최소 8자 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                        } else if (mPwcheckEdit.getText().toString().equals("")
                                || mPwcheckEdit.getText().toString().length() < 8) {
                            Toast.makeText(Join.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                        } else if (mNameEdit.getText().toString().equals("")) {
                            mNameToast.show();
                        } else if (mBirthEdit.getText().toString().equals("")) {
                            mBirthToast.show();
                        } else if (MemberDB.INSTANCE.isDuplicate(mIdEdit.getText().toString())) {
                            Toast.makeText(Join.this, "중복된 이메일입니다!", Toast.LENGTH_SHORT).show();
                        } else if (mBirthEdit.getText().toString().length() != 6) {
                            mBirthToast.show();
                        } else if (!mIdEdit.getText().toString().contains("@")) {
                            mIdToast.show();
                        } else {
                            if (mPwEdit.getText().toString().equals(mPwcheckEdit.getText().toString())) {
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        mWaitDlg = new WaitDlg(Join.this, null, "Loading...");
                                        mWaitDlg.start();

                                        Intent intent = getIntent();
                                        String token = intent.getStringExtra("token");
                                        BasicDBObject user = new BasicDBObject("email", mIdEdit.getText().toString())
                                                .append("pw", mPwEdit.getText().toString())
                                                .append("name", mNameEdit.getText().toString())
                                                .append("birth", mBirthEdit.getText().toString())
                                                .append("gender", mMaleRadio.isChecked() ? "Male" : "Female")
                                                .append("joinPath", mJoinPath)
                                                .append("DeviceToken", token)
                                                .append("DeviceType", "Android");
                                        MemberDB.INSTANCE.join(user);
                                        if(mLoginActivity != null) mLoginActivity.finish();
                                        WaitDlg.stop(mWaitDlg);
                                    }
                                };

                                thread.start();
                                try {
                                    thread.join();
                                } catch (InterruptedException e) {
                                }

                                Intent SalaryInsert = new Intent(Join.this, SalaryInsert.class);
                                startActivity(SalaryInsert);
                                Join.this.finish();
                            } else {
                                mPwcheckfailToast.show();
                            }
                        }
                    } else {
                        Toast.makeText(Join.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            backPressCloseHandler = new BackPressCloseHandler();
            TextView agree = (TextView) findViewById(R.id.txv_agreement_join);
            agree.setLinkTextColor(Color.RED);
            agree.setMovementMethod(LinkMovementMethod.getInstance());

            mCancleBtn = (Button) findViewById(R.id.btn_cancle_join);
            mCancleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        try {
            backPressCloseHandler.onBackPressed();
        } catch (NullPointerException e) {
            Intent intent = new Intent(Join.this, LoginActivity.class);
            startActivity(intent);
            Join.this.finish();
        }
    }

    private class BackPressCloseHandler {
        public void onBackPressed() {
            Intent intent = new Intent(Join.this, LoginActivity.class);
            startActivity(intent);
            Join.this.finish();
        }
    }
}