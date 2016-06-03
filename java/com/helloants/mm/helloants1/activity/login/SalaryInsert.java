package com.helloants.mm.helloants1.activity.login;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.MainActivity;
import com.helloants.mm.helloants1.data.SMS.SMSReader;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.loading.WaitDlg;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;

public class SalaryInsert extends AppCompatActivity {
    private Button mNextBtn;
    private NumberPicker mSalarydateEdit;
    private BackPressCloseHandler backPressCloseHandler;
    private LinearLayout layout;
    final int REQUEST_READ_SMS = 1;
    private boolean isSkip;
    private WaitDlg mWaitDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_insert);
        Log.v("진행", "6");
        try {
            isSkip = false;
            //툴바 이미지 백 버튼
            Log.v("진행", "7");
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_aif);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SalaryInsert.this.onBackPressed();
                }
            });

            layout = (LinearLayout) findViewById(R.id.llay_root_view_salary_activity);
            mNextBtn = (Button) findViewById(R.id.btn_next_salaryinsert);
            mSalarydateEdit = (NumberPicker) findViewById(R.id.npik_salarydate_salaryinsert);
            mSalarydateEdit.setMinValue(1);
            mSalarydateEdit.setMaxValue(31);
            mSalarydateEdit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            final EditText budget = (EditText) findViewById(R.id.edit_budget_salaryinsert);
            Log.v("진행", "8");
            //기존문자 넣기
            Thread threadSMS = new Thread() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 버전이 마시멜로 이상일때
                        if (ContextCompat.checkSelfPermission(SalaryInsert.this,
                                Manifest.permission.READ_SMS)
                                != PackageManager.PERMISSION_GRANTED) { // 사용 권한 체크
                            Log.v("진행","1");

                            if (ActivityCompat.shouldShowRequestPermissionRationale(SalaryInsert.this,
                                    Manifest.permission.READ_SMS)) { // 사용자가 임의로 권한을 취소시킨 경우 권한 재요청
                                ActivityCompat.requestPermissions(SalaryInsert.this,
                                        new String[]{Manifest.permission.READ_SMS},
                                        REQUEST_READ_SMS);
                                Log.v("진행", "2");
                            } else { // 최초로 권한을 요청하는 경우
                                ActivityCompat.requestPermissions(SalaryInsert.this,
                                        new String[]{Manifest.permission.READ_SMS},
                                        REQUEST_READ_SMS);
                                Log.v("진행", "3");
                            }
                        } else { // 사용 권한이 있는 경우
                            mWaitDlg = new WaitDlg(SalaryInsert.this, null, "Loading...");
                            mWaitDlg.start();

                            Thread thread = new Thread() {
                                public void run() {
                                    Log.v("진행", "4");
                                    SMSReader.INSTANCE.SMSList(SalaryInsert.this);
                                }
                            };

                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                            }

                            WaitDlg.stop(mWaitDlg);
                        }
                    } else { // 버전이 마시멜로 이전일떄
                        mWaitDlg = new WaitDlg(SalaryInsert.this, null, "Loading...");
                        mWaitDlg.start();
                        Log.v("진행", "5");
                        Thread thread = new Thread() {
                            public void run() {
                                SMSReader.INSTANCE.SMSList(SalaryInsert.this);
                            }
                        };

                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                        }

                        WaitDlg.stop(mWaitDlg);
                    }
                }
            };
            threadSMS.start();
            try {
                threadSMS.join();
            } catch (InterruptedException e) {
            }

            mNextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (budget.getText().toString().equals("") || budget.getText().toString() == null) {
                        Snackbar.make(v, "예산을 입력해 주세요", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (isSkip) {
                            mWaitDlg = new WaitDlg(SalaryInsert.this, null, "Loading...");
                            mWaitDlg.start();

                            CardOffsetDayInsert.AssetInsertThread at = new CardOffsetDayInsert.AssetInsertThread();
                            CardOffsetDayInsert.DebtInsertThread dt = new CardOffsetDayInsert.DebtInsertThread();

                            try {
                                at.start();
                                at.join();
                                dt.start();
                                dt.join();
                            } catch (Exception e) {
                            }

                            MemberDB.INSTANCE.init(SalaryInsert.this);
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    BasicDBObject user = new BasicDBObject("salaryDate", mSalarydateEdit.getValue()).append("budget", budget.getText().toString());

                                    String email = "";
                                    try {
                                        email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                                    } catch (Exception e) {
                                    }

                                    MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                            new BasicDBObject("$set", user));
                                }
                            };

                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                            }

                            WaitDlg.stop(mWaitDlg);

                            Intent intent = new Intent(SalaryInsert.this, MainActivity.class);
                            startActivity(intent);
                            SalaryInsert.this.finish();
                        } else {
                            mWaitDlg = new WaitDlg(SalaryInsert.this, null, "Loading...");
                            mWaitDlg.start();

                            MemberDB.INSTANCE.init(SalaryInsert.this);
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    BasicDBObject user = new BasicDBObject("salaryDate", mSalarydateEdit.getValue()).append("budget", budget.getText().toString());

                                    String email = "";
                                    try {
                                        email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                                    } catch (Exception e) {
                                    }

                                    MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                            new BasicDBObject("$set", user));
                                }
                            };

                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                            }

                            WaitDlg.stop(mWaitDlg);

                            Intent intent = new Intent(SalaryInsert.this, CardOffsetDayInsert.class);
                            startActivity(intent);
                            SalaryInsert.this.finish();
                        }
                    }
                }
            });

            backPressCloseHandler = new BackPressCloseHandler(this);
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        try {
            backPressCloseHandler.onBackPressed();
        } catch (NullPointerException e) {
            super.onBackPressed();
        }
    }

    private class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
            }
        }

        private void showGuide() {
            Snackbar.make(layout, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mWaitDlg = new WaitDlg(SalaryInsert.this, null, "Loading...");
                    mWaitDlg.start();

                    Thread thread = new Thread() {
                        public void run() {
                            SMSReader.INSTANCE.SMSList(SalaryInsert.this);
                        }
                    };

                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                    }

                    WaitDlg.stop(mWaitDlg);
                } else {
                    Toast.makeText(SalaryInsert.this, "권한에 동의해주셔야 문자 인식이 가능합니다", Toast.LENGTH_LONG).show();
                    skip();
                }
                return;
            default:
                Toast.makeText(SalaryInsert.this, "예외적인 경우입니다\n앱을 다시 실행해주세요", Toast.LENGTH_LONG).show();
                return;
        }
    }

    private void skip() {
        isSkip = true;
    }
}