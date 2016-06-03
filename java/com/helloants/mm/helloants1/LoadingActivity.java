package com.helloants.mm.helloants1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.helloants.mm.helloants1.activity.MainActivity;
import com.helloants.mm.helloants1.activity.login.CardOffsetDayInsert;
import com.helloants.mm.helloants1.activity.login.LoginActivity;
import com.helloants.mm.helloants1.activity.login.SalaryInsert;
import com.helloants.mm.helloants1.data.DeviceSize;
import com.helloants.mm.helloants1.data.SMS.SMSReader;
import com.helloants.mm.helloants1.data.network.GetNetState;
import com.helloants.mm.helloants1.db.ConnectDB;
import com.helloants.mm.helloants1.db.bs.BsItem;
import com.helloants.mm.helloants1.db.content.ContentDB;
import com.helloants.mm.helloants1.db.content.NoticeDB;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.db.mypage.RequestDB;
import com.helloants.mm.helloants1.db.mypage.ScrapDB;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;
import com.nhn.android.naverlogin.OAuthLogin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadingActivity extends AppCompatActivity {
    GoogleApiClient client;
    String store_version;
    String device_version;
    final int REQUEST_READ_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            versionCheck();
            init();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        ConnectDB.INSTANCE.connect();
                        ContentDB.INSTANCE.onlyCall();
                        NoticeDB.INSTANCE.settingImg();
                        ScrapDB.INSTANCE.onlyCall();
                        RequestDB.INSTANCE.onlyCall();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 버전이 마시멜로 이상일때
                            if (ContextCompat.checkSelfPermission(LoadingActivity.this,
                                    Manifest.permission.READ_SMS)
                                    != PackageManager.PERMISSION_GRANTED) { // 사용 권한 체크

                                if (ActivityCompat.shouldShowRequestPermissionRationale(LoadingActivity.this,
                                        Manifest.permission.READ_SMS)) { // 사용자가 임의로 권한을 취소시킨 경우 권한 재요청
                                    ActivityCompat.requestPermissions(LoadingActivity.this,
                                            new String[]{Manifest.permission.READ_SMS},
                                            REQUEST_READ_SMS);
                                } else { // 최초로 권한을 요청하는 경우
                                    ActivityCompat.requestPermissions(LoadingActivity.this,
                                            new String[]{Manifest.permission.READ_SMS},
                                            REQUEST_READ_SMS);
                                }
                            } else { // 사용 권한이 있는 경우
                                SMSReader.INSTANCE.init(LoadingActivity.this);
                            }
                        } else { // 버전이 마시멜로 이전일떄
                            SMSReader.INSTANCE.init(LoadingActivity.this);
                        }
                    } catch (ExceptionInInitializerError e) {
                    }
                }
            };
            thread.start();

            try {
                GetNetState.INSTANCE.checkNetwork(LoadingActivity.this);
            } catch (NullPointerException e) {
                new AlertDialog.Builder(LoadingActivity.this)
                        .setTitle("인터넷 연결 오류")
                        .setMessage("인터넷 연결 중 문제가 발생했습니다.\n" +
                                "연결을 확인하고 다시 실행해주세요")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoadingActivity.this.finish();
                            }
                        })
                        .show();
            }

            if (GetNetState.INSTANCE.mWifi) {
                Handler hd = new Handler();
                hd.postDelayed(new splashhandler(), 1);
            } else if (GetNetState.INSTANCE.mMobile) {
                Handler hd = new Handler();
                hd.postDelayed(new splashhandler(), 1);
            } else {
                new AlertDialog.Builder(LoadingActivity.this)
                        .setTitle("인터넷 연결 오류")
                        .setMessage("인터넷이 연결되어 있지 않습니다.\n" +
                                "연결을 확인하고 다시 실행해주세요")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoadingActivity.this.finish();
                            }
                        }).show();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case REQUEST_READ_SMS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SMSReader.INSTANCE.init(LoadingActivity.this);
                } else {
                    Toast.makeText(LoadingActivity.this, "권한에 동의해주셔야 문자 인식이 가능합니다", Toast.LENGTH_LONG).show();
                }
                return;
            default:
                Toast.makeText(LoadingActivity.this, "예외적인 경우입니다\n앱을 다시 실행해주세요", Toast.LENGTH_LONG).show();
                return;
        }
    }

    private void init() {
        setContentView(R.layout.activity_loading);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        DeviceSize.init(LoadingActivity.this);
        OAuthLogin.getInstance().init(
                LoadingActivity.this,
                "_obagyJoXIu0wGtf9HeV",
                "fIGURdt5La",
                "헬로앤츠");
        MemberDB.INSTANCE.init(LoadingActivity.this);
    }

    private void versionCheck() {
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        new Thread(new Runnable() {
            public void run() {
                try {
                    store_version = MarketVersionChecker.getMarketVersionFast(getPackageName());
                    try {
                        device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                    }

                    if (store_version.compareTo(device_version) > 0) {
                        Looper.prepare();

                        AlertDialog alert = new AlertDialog.Builder(LoadingActivity.this)
                                .setTitle("알림")
                                .setMessage("새로운 버전이 있습니다. \n새로운 버전으로 업데이트 해주세요!")
                                .setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String url = "https://play.google.com/store/apps/details?id=com.helloants.mm.helloants1";
                                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.helloants.mm.helloants1"));

                                        startActivity(i);
                                        finish();
                                    }
                                })
                                .show();

                        alert.setCanceledOnTouchOutside(false);
                        Looper.loop();
                    }
                } catch (NullPointerException e) {
                }
            }

        }).start();
    }

    private class splashhandler implements Runnable {
        public void run() {
            String email = "";

            try {
                email = Cryptogram.Decrypt(LoginData.mEmail);
            } catch (Exception e) {
            }

            final String EMAIL = email;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    boolean bsItemExisted = BsItem.INSTANCE.isItemExisted();
                    if (EMAIL.equals("")) {
                        startActivity(new Intent(getApplication(), LoginActivity.class));
                        LoadingActivity.this.finish();
                    } else if (MemberDB.INSTANCE.find(new BasicDBObject("email", EMAIL)).next().get("salaryDate") == null) {
                        startActivity(new Intent(getApplication(), SalaryInsert.class));
                        LoadingActivity.this.finish();
                    } else if (MemberDB.INSTANCE.isCardOff()
                            && !bsItemExisted) {
                        startActivity(new Intent(getApplication(), CardOffsetDayInsert.class));
                        LoadingActivity.this.finish();
                    } else {
                       startActivity(new Intent(getApplication(), MainActivity.class));
                        LoadingActivity.this.finish();
                    }
                }
            };

            thread.start();
        }
    }

    private static class MarketVersionChecker {
        public static String getMarketVersionFast(String packageName) {
            String mData = "", mVer = null;

            try {
                URL mUrl = new URL(" https://play.google.com/store/apps/details?id=" + packageName);
                HttpURLConnection mConnection = (HttpURLConnection) mUrl
                        .openConnection();

                if (mConnection == null)
                    return null;

                mConnection.setConnectTimeout(5000);
                mConnection.setUseCaches(false);
                mConnection.setDoOutput(true);

                if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader mReader = new BufferedReader(
                            new InputStreamReader(mConnection.getInputStream()));

                    while (true) {
                        String line = mReader.readLine();
                        if (line == null)
                            break;
                        mData += line;
                    }
                    mReader.close();
                }
                mConnection.disconnect();
            } catch (Exception ex) {
                return null;
            }

            String startToken = "softwareVersion\">";
            String endToken = "<";
            int index = mData.indexOf(startToken);

            if (index == -1) {
                mVer = null;
            } else {
                mVer = mData.substring(index + startToken.length(), index
                        + startToken.length() + 100);
                mVer = mVer.substring(0, mVer.indexOf(endToken)).trim();
            }
            return mVer;
        }
    }
}