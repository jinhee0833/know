package com.helloants.mm.helloants1.activity.feedback;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.adapters.viewholder.SMSViewHolder;
import com.helloants.mm.helloants1.data.SMS.SMSReader;
import com.helloants.mm.helloants1.fragment.sms.SlidingSmsFragment;


/**
 * Created by kingherb on 2016-04-13.
 */
public class SMSActivity extends AppCompatActivity {
    private ListView mListView;
    private SMSAdapter mAdapter;
    private int mSize;
    final int REQUEST_READ_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView) findViewById(R.id.txv_title_sms);
        txvTitle.setText("미인식 문자 신고");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_sms);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SMSActivity.this.onBackPressed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 버전이 마시멜로 이상일때
            if (ContextCompat.checkSelfPermission(SMSActivity.this,
                    Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) { // 사용 권한 체크

                if (ActivityCompat.shouldShowRequestPermissionRationale(SMSActivity.this,
                        Manifest.permission.READ_SMS)) { // 사용자가 임의로 권한을 취소시킨 경우 권한 재요청
                    ActivityCompat.requestPermissions(SMSActivity.this,
                            new String[]{Manifest.permission.READ_SMS},
                            REQUEST_READ_SMS);
                } else { // 최초로 권한을 요청하는 경우
                    ActivityCompat.requestPermissions(SMSActivity.this,
                            new String[]{Manifest.permission.READ_SMS},
                            REQUEST_READ_SMS);
                }
            } else { // 사용 권한이 있는 경우
                initSMS();
            }
        } else { // 버전이 마시멜로 이전일떄
            initSMS();
        }
    }

    private void initSMS() {
        SMSReader.INSTANCE.settingSMSMap();
        if (SMSReader.INSTANCE.mSmsMap == null) {
            SMSReader.INSTANCE.init(SMSActivity.this);
//            Intent intent1 = new Intent(SMSActivity.this, SMSActivity.class);
//            startActivity(intent1);
//            SMSActivity.this.finish();
            initSMS();
        } else {
            mSize = SMSReader.INSTANCE.mSmsMap.size();

            mListView = (ListView) findViewById(R.id.listview_sms_fragment);
            mAdapter = new SMSAdapter();
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initSMS();
                } else {
                    Toast.makeText(SMSActivity.this, "권한에 동의해주셔야 문자 인식이 가능합니다", Toast.LENGTH_LONG).show();
                }
                return;
            default:
                Toast.makeText(SMSActivity.this, "예외적인 경우입니다\n앱을 다시 실행해주세요", Toast.LENGTH_LONG).show();
                return;
        }
    }

    @Override
    public void onBackPressed() {
        if (SlidingSmsFragment.mIsVisible) {
            SlidingSmsFragment.mBack.performClick();
            SlidingSmsFragment.mIsVisible = false;
        } else {
            super.onBackPressed();
        }
    }

    private class SMSAdapter extends BaseAdapter {
        private SMSViewHolder viewHolder;
        private Typeface fontFamily;

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_sms_list, parent, false);

                viewHolder = new SMSViewHolder();
                viewHolder.mPhoneNum = (TextView) convertView.findViewById(R.id.txv_phone_number_sms);
                viewHolder.mBody = (TextView) convertView.findViewById(R.id.txv_body_sms);

                convertView.setTag(viewHolder);
            } else viewHolder = (SMSViewHolder) convertView.getTag();

            fontFamily = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
            TextView txv = (TextView) convertView.findViewById(R.id.txv_phone_icon_sms);
            txv.setTypeface(fontFamily);
            txv.setText("\uF003");

            String str = "";
            try {
                str = SMSReader.INSTANCE.mOrderList.get(position);
            } catch (IndexOutOfBoundsException e) {
            }

            if (str.equals("")) {
            } else {
                viewHolder.mPhoneNum.setText(str);
                viewHolder.mBody.setText(SMSReader.INSTANCE.mSmsMap.get(str));

                final int POS = position;
                final String STR = str;
                final View CV = convertView;
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SMSReader.INSTANCE.settingSMSList(STR);
                        android.app.Fragment f = getFragmentManager().findFragmentByTag("sms_letter_fragment");
                        if (f != null) {
                            getFragmentManager().popBackStack();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                                SlidingSmsFragment.mBack = CV;

                                getFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.animator.slide_up,
                                                R.animator.slide_down,
                                                R.animator.slide_up,
                                                R.animator.slide_down)
                                        .add(R.id.list_fragment_container, SlidingSmsFragment
                                                        .instantiate(SMSActivity.this, SlidingSmsFragment.class.getName()),
                                                "sms_letter_fragment"
                                        ).addToBackStack(null).commit();
                            }
                        }
                    }
                });
            }
            return convertView;
        }
    }
}