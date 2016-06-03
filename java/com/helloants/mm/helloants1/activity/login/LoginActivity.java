package com.helloants.mm.helloants1.activity.login;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.TempActivity;
import com.helloants.mm.helloants1.data.SMS.SMSReader;
import com.helloants.mm.helloants1.data.constant.Icon;
import com.helloants.mm.helloants1.data.network.GetNetState;
import com.helloants.mm.helloants1.data.notification.QuickstartPreferences;
import com.helloants.mm.helloants1.data.notification.RegistrationIntentService;
import com.helloants.mm.helloants1.data.type.FBType;
import com.helloants.mm.helloants1.data.type.NaverType;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.loading.WaitDlg;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailEdit;
    private EditText mPwEdit;
    private RelativeLayout mLogin;
    private RelativeLayout mJoin;
    private TextView mFindEmailTxv;
    private TextView mFindPwTxv;
    private Toast mLoginfailToast;
    private CallbackManager callbackManager;
    private RelativeLayout mFacebook;
    private RelativeLayout mNaver;
    private WaitDlg mWaitDlg;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";
    private String token;
    private BackPressCloseHandler backPressCloseHandler;
    LinearLayout layout;
    private OAuthLogin mOAuthLoginInstance;
    private static Context mContext;
    final int REQUEST_READ_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                registBroadcastReceiver();
                getInstanceIdToken();

                // 페이스북 네이버 로그인 토큰을 join 말고(if 부분)
                // 로그인에서도(else 부분) 넣어줘야됨


                MemberDB.INSTANCE.init(LoginActivity.this);
                initLayout();
                initFacebook();
                initNaver();

                backPressCloseHandler = new BackPressCloseHandler(this);

                layout = (LinearLayout) findViewById(R.id.llay_login_login);
            } else {
                Toast.makeText(LoginActivity.this, "네트워크 연결에 문제가 있습니다.\n다시 시도해주십시오", Toast.LENGTH_LONG).show();
                LoginActivity.this.finish();
            }
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this,e.toString(),Toast.LENGTH_LONG);
        }
    }

    private void initNaver() {
        mContext = getApplicationContext();
        mOAuthLoginInstance = OAuthLogin.getInstance();

        final OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                if (success) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            String url = "https://openapi.naver.com/v1/nid/me";
                            String at = mOAuthLoginInstance.getAccessToken(mContext);
                            try {
                                JSONObject jsonObj = new JSONObject(mOAuthLoginInstance.requestApi(mContext, at, url));
                                JSONObject json = new JSONObject(jsonObj.getString("response"));

                                NaverType user = new NaverType();
                                user.mID = json.has("id") ? json.getString("id") : "";
                                user.mEmail = json.has("email") ? json.getString("email") : "";
                                user.mName = json.has("name") ? json.getString("name") : "";
                                user.mBirth = json.has("birthday") ? json.getString("birthday") : "";
                                user.mGender = json.has("gender") ? json.getString("gender") : "";

                                if (MemberDB.INSTANCE.isNaverJoin(user.mID + "@naver")) {
                                    startActivity(new Intent(getApplication(), TempActivity.class));
                                    LoginActivity.this.finish();
                                } else {
                                    Join.mLoginActivity = LoginActivity.this;
                                    Intent loginActivity = new Intent(LoginActivity.this, Join.class);
                                    loginActivity.putExtra("naverEmail", user.mEmail);
                                    loginActivity.putExtra("naverName", user.mName);
                                    loginActivity.putExtra("naverBirthday", user.mBirth.replaceAll("-", ""));
                                    loginActivity.putExtra("naverGender", user.mGender);
                                    loginActivity.putExtra("joinPath", user.mID + "@naver");
                                    loginActivity.putExtra("token", token);
                                    startActivity(loginActivity);
                                    LoginActivity.this.finish();
                                }
                            } catch (JSONException e) {
                            }
                        }
                    };
                    thread.start();
                } else {
                }
            }

            ;
        };

        mNaver = (RelativeLayout) findViewById(R.id.rlay_naverlogin_login);
        mNaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginInstance.startOauthLoginActivity(LoginActivity.this, mOAuthLoginHandler);
            }
        });
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                FBType user = new FBType();
                                try {
                                    user.mID = object.has("id") ? object.getString("id") : "";
                                    user.mEmail = object.has("email") ? object.getString("email") : "";
                                    user.mName = object.has("name") ? object.getString("name") : "";
                                    user.mGender = object.has("gender") ? object.getString("gender") : "";
                                    user.mBirth = object.has("birthday") ? object.getString("birthday") : "";

                                } catch (JSONException e) {
                                }

                                if (MemberDB.INSTANCE.isFbJoin(user.mID + "@facebook")) {
                                    startActivity(new Intent(getApplication(), TempActivity.class));
                                    LoginActivity.this.finish();
                                } else {
                                    Join.mLoginActivity = LoginActivity.this;
                                    Intent loginActivity = new Intent(LoginActivity.this, Join.class);
                                    loginActivity.putExtra("fbEmail", user.mEmail);
                                    loginActivity.putExtra("fbName", user.mName);
                                    loginActivity.putExtra("fbBirthday", user.mBirth);
                                    loginActivity.putExtra("fbGender", user.mGender);
                                    loginActivity.putExtra("joinPath", user.mID + "@facebook");
                                    loginActivity.putExtra("token", token);
                                    startActivity(loginActivity);
                                    LoginActivity.this.finish();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });
    }

    private void initLayout() {
        mEmailEdit = (EditText) findViewById(R.id.edit_id_login);
        mPwEdit = (EditText) findViewById(R.id.edit_pw_login);
        mLoginfailToast = Toast.makeText(this, "이메일 혹은 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT);

        mLogin = (RelativeLayout) findViewById(R.id.rlay_login_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    new CheckLogin(mEmailEdit.getText().toString(), mPwEdit.getText().toString()).execute();
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mJoin = (RelativeLayout) findViewById(R.id.rlay_join_login);
        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Join.mLoginActivity = LoginActivity.this;
                Intent JoinActivity = new Intent(LoginActivity.this, Join.class);
                JoinActivity.putExtra("token", token);
                startActivity(JoinActivity);
            }
        });
        mFindEmailTxv = (TextView) findViewById(R.id.txv_findEmai_loginl);
        mFindEmailTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    Intent EmailSearch = new Intent(LoginActivity.this, EmailSearch.class);
                    startActivity(EmailSearch);
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFindPwTxv = (TextView) findViewById(R.id.txv_FindPw_login);
        mFindPwTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("비밀번호 찾기")
                            .setMessage("비밀번호 찾기는 웹사이트에서 진행해주세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.helloants.com/login"));
                                    intent.setPackage("com.android.chrome");
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFacebook = (RelativeLayout) findViewById(R.id.rlay_fblogin_login);
        mFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Typeface fontFamily = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
        TextView loginIcon = (TextView) findViewById(R.id.txv_login_icon);
        loginIcon.setTypeface(fontFamily);
        loginIcon.setText(Icon.LOGIN);

        TextView joinIcon = (TextView) findViewById(R.id.txv_join_icon);
        joinIcon.setTypeface(fontFamily);
        joinIcon.setText(Icon.JOIN);

        TextView fbIcon = (TextView) findViewById(R.id.txv_fblogin_icon);
        fbIcon.setTypeface(fontFamily);
        fbIcon.setText(Icon.FACEBOOK);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class CheckLogin extends AsyncTask<Void, Void, Void> {
        private String mEmail;
        private String mPw;

        public CheckLogin(String email, String pw) {
            mEmail = email;
            mPw = pw;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mWaitDlg = new WaitDlg(LoginActivity.this, null, "Loading...");
            mWaitDlg.start();

            BasicDBObject user = new BasicDBObject("email", mEmail).append("pw", mPw);
            if (MemberDB.INSTANCE.checkLogin(user, true)) {
                String email = "";
                try {
                    email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                } catch (Exception e) {}

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 버전이 마시멜로 이상일때
                    if (ContextCompat.checkSelfPermission(LoginActivity.this,
                            Manifest.permission.READ_SMS)
                            != PackageManager.PERMISSION_GRANTED) { // 사용 권한 체크

                        if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                                Manifest.permission.READ_SMS)) { // 사용자가 임의로 권한을 취소시킨 경우 권한 재요청
                            ActivityCompat.requestPermissions(LoginActivity.this,
                                    new String[]{Manifest.permission.READ_SMS},
                                    REQUEST_READ_SMS);
                        } else { // 최초로 권한을 요청하는 경우
                            ActivityCompat.requestPermissions(LoginActivity.this,
                                    new String[]{Manifest.permission.READ_SMS},
                                    REQUEST_READ_SMS);
                        }
                    } else { // 사용 권한이 있는 경우
                        SMSReader.INSTANCE.SMSList(LoginActivity.this);
                    }
                } else { // 버전이 마시멜로 이전일떄
                    SMSReader.INSTANCE.SMSList(LoginActivity.this);
                }

                BasicDBObject device = new BasicDBObject("DeviceToken", token).append("DeviceType", "Android");
                MemberDB.INSTANCE.deviceToken(new BasicDBObject("email", email), device);

                startActivity(new Intent(getApplication(), TempActivity.class));
                LoginActivity.this.finish();
            } else {
                mLoginfailToast.show();
            }

            WaitDlg.stop(mWaitDlg);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
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

    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void registBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                token = intent.getStringExtra("token");
            }
        };
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case REQUEST_READ_SMS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SMSReader.INSTANCE.SMSList(LoginActivity.this);
                } else {
                    Toast.makeText(LoginActivity.this, "권한에 동의해주셔야 문자 인식이 가능합니다", Toast.LENGTH_LONG).show();
                }
                return;
            default:
                Toast.makeText(LoginActivity.this, "예외적인 경우입니다\n앱을 다시 실행해주세요", Toast.LENGTH_LONG).show();
                return;
        }
    }
}