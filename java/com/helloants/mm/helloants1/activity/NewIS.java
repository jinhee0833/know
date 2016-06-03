package com.helloants.mm.helloants1.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.adapters.SpinerAdapter;
import com.helloants.mm.helloants1.adapters.SpinerAdapter2;
import com.helloants.mm.helloants1.db.bs.BsDB;
import com.helloants.mm.helloants1.db.bs.BsItem;
import com.helloants.mm.helloants1.loading.WaitDlg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NewIS extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private EditText mPrice;
    private EditText mWhere;
    private DatePicker mDatePick;
    private Button mBtnInput;
    private String mRprice;
    private String mRwhere;
    private Date mRdate;
    private String mRtype;
    private ArrayList[] mList;
    private TextView mChoiceTxv;
    private TextView mChoiceTxv2;
    private ArrayList mAssetList;
    private ArrayList mDebtList;
    private Spinner sp;
    private Spinner sp2;
    TextView mNewTxv;
    ArrayList<String> resultList;
    SpinerAdapter fAdapter;
    int dis;
    private String mLeft;
    private String mRight;
    private String mRpart;
    private TimePicker mTimePick;
    private WaitDlg mWaitDlg;
    public static Activity mMainActivity;
    private Date initDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_is);

        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView) findViewById(R.id.txv_title_newis);
        txvTitle.setText("새로운 경제 활동");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_newis);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewIS.this.onBackPressed();
            }
        });

        //첫번째 스피너
        sp = (Spinner) findViewById(R.id.spiner_what_newis);
        sp.setPrompt("유형을 선택하세요");
        populateSpinners();

        //두번째 스피너
        sp2 = (Spinner) findViewById(R.id.spiner_what2_newis);
        populateSubSpinners(0);

        //첫번째 스피너 클릭될때 실행될 메소드
        sp.setOnItemSelectedListener(spinSelectedlistener);

        //버튼 텍스트뷰등등
        mPrice = (EditText) findViewById(R.id.edit_price_newis);
        mWhere = (EditText) findViewById(R.id.edit_where_newis);
        Calendar cal = Calendar.getInstance();
        initDate = BsDB.INSTANCE.firstDate();
        cal.set(initDate.getYear() + 1900, initDate.getMonth(), initDate.getDate(), 0, 0, 0);
        long time = cal.getTimeInMillis();
        mDatePick = (DatePicker) findViewById(R.id.datepick_isdate_newis);
        mDatePick.setMinDate(time);


        mTimePick = (TimePicker) findViewById(R.id.timepick_isdate_newis);

        mBtnInput = (Button) findViewById(R.id.btn_input_newis);
        mChoiceTxv = (TextView) findViewById(R.id.txv_choice_newis);
        mChoiceTxv2 = (TextView) findViewById(R.id.txv_choice_newis2);
        mNewTxv = (TextView) findViewById(R.id.txv_new_newis);

        Button mBtnCancel = (Button) findViewById(R.id.btn_cancel_newis);


        //일단 뉴버튼으로 만들어진 새항목을 보여줄 텍스트뷰 안보이게
        mNewTxv.setVisibility(View.GONE);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateSpinners() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("돈 벌었어요");
        list.add("현금을 썼어요");
        list.add("돈 빌렸어요");
        list.add("돈 갚았어요");
        list.add("돈 빌려줬어요");
        list.add("빌려준 돈 받았어요");
        list.add("집을 샀어요 or 보증금 올렸어요");
        list.add("저축 했어요(적금, 펀드, 주식, 보험 등)");
        list.add("자산을 팔았어요");
        list.add("체크카드를 썼어요");
        list.add("신용카드를 썼어요");

        SpinerAdapter fAdapter = new SpinerAdapter();
        fAdapter.setList(list);
        sp.setAdapter(fAdapter);
    }

    private void populateSubSpinners(int itemNum) {
        //DB에서 자산 부채 항목들 가져오기
        mList = BsItem.INSTANCE.BsItemFind();
        mAssetList = mList[0]; //자산항목리스트
        mDebtList = mList[1];//부채항목리스트


        //resultList = new ArrayList<>();
        switch (itemNum) {
            case 0:
            case 4:
            case 7:
                resultList = mAssetList;
                resultList.add("추가+");
                break;
            case 1:
            case 5:
            case 6:
            case 8:
            case 9:
                resultList = mAssetList;
                break;
            case 2:
                resultList = mDebtList;
                resultList.add("추가+");
                break;
            case 3:
            case 10:
                resultList = mDebtList;
                break;
        }
        fAdapter = new SpinerAdapter2();
        fAdapter.setList(resultList);
        sp2.setAdapter(fAdapter);
        sp2.setOnItemSelectedListener(this);

    }

    //첫번째 스피너 선택하면 일어나는 일
    private AdapterView.OnItemSelectedListener spinSelectedlistener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mNewTxv.setVisibility(View.GONE);
            sp2.setVisibility(View.VISIBLE);
            dis = position;
            switch (position) {
                case 0:
                    mRtype = "income";
                    mRight = "수입+";
                    mChoiceTxv.setText("2. 어떤 자산이 늘어났습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\n'추가' 항목을 선택하세요");
                    populateSubSpinners(0);
                    break;
                case 1:
                    mRtype = "cashExpend";
                    mLeft = "지출+";
                    mChoiceTxv.setText("2. 어떤 자산으로 소비하셨습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy메뉴의 '자산 초깃값 수정'에서 항목을 추가해 주세요");
                    populateSubSpinners(1);
                    break;
                case 2:
                    mRtype = "loan";
                    mLeft = "현금+";
                    mChoiceTxv.setText("2. 어떤 부채가 늘어났습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\n'추가' 항목을 선택하세요");
                    populateSubSpinners(2);
                    break;
                case 3:
                    mRtype = "repay";
                    mRight = "현금-";
                    mChoiceTxv.setText("2. 어떤 부채를 갚았습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy 메뉴의 '부채 초깃값 수정'에서 항목을 추가해 주세요");
                    populateSubSpinners(3);
                    break;
                case 4:
                    mRtype = "lend";
                    mRight = "현금-";
                    mChoiceTxv.setText("2. 누구에게 빌려줬습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\n'추가' 항목을 선택하세요");
                    populateSubSpinners(4);
                    break;
                case 5:
                    mRtype = "receiveLend";
                    mLeft = "현금+";
                    mChoiceTxv.setText("2. 누구에게 돌려 받았습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy 메뉴의 자산 초깃값에서 돌려받기 전의 자산(채권)을 추가해 주세요");
                    populateSubSpinners(5);
                    break;
                case 6:
                    mRtype = "house";
                    mLeft = "집값 혹은 보증금+";
                    mChoiceTxv.setText("2. 어떤 자산으로 구입하셨습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy 메뉴의 자산 초깃값에서 구입전의 자산을 추가해 주세요");
                    populateSubSpinners(6);
                    break;
                case 7:
                    mRtype = "save";
                    mRight = "현금-";
                    mChoiceTxv.setText("2. 어디에 저축(펀드,주식,보험,적금)하셨습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\n'추가' 항목을 선택하세요");
                    populateSubSpinners(7);
                    break;
                case 8:
                    mRtype = "sell";
                    mRight = "현금+";
                    mChoiceTxv.setText("2. 어떤 자산을 파셨습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy 메뉴의 자산 초깃값에서 팔기전의 자산을 추가해 주세요");
                    populateSubSpinners(8);
                    break;
                case 9:
                    mRtype = "check";
                    mLeft = "지출+";
                    mChoiceTxv.setText("2. 어떤 카드로 소비하셨습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy 메뉴의 자산 초깃값에서 카드를 추가해 주세요");
                    populateSubSpinners(9);
                    break;
                case 10:
                    mRtype = "credit";
                    mLeft = "지출+";
                    mChoiceTxv.setText("2. 어떤 카드로 소비하셨습니까?");
                    mChoiceTxv2.setText("리스트에 해당 항목이 없다면,\nMy 메뉴의 부채 초깃값에서 카드를 추가해 주세요");
                    populateSubSpinners(10);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    //투번째 스피너 선택후 일어나는일
    @Override
    public void onItemSelected(final AdapterView<?> parent, View view, final int position, long id) {


        if (parent.getItemAtPosition(position).equals("추가+")) {
            final EditText AssetName = new EditText(NewIS.this);
            new AlertDialog.Builder(NewIS.this)
                    .setTitle("항목 추가")
                    .setMessage("새로운 항목을 추가해 주세요.")
                    .setView(AssetName)
                    .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //추가 버튼 눌렀을때
                            String name = AssetName.getText().toString();
                            switch (dis) {
                                case 0:
                                case 4:
                                case 7:
                                    mLeft = name + "+";
                                    break;
                                case 2:
                                    mRight = name + "+";
                                    break;

                            }
                            sp2.setVisibility(View.GONE);
                            mNewTxv.setVisibility(View.VISIBLE);
                            mNewTxv.setText(name);
                        }
                    }).show();
        } else {
            String tempName = (String) parent.getItemAtPosition(position);
            String name = tempName.substring(0, tempName.length() - 1);
            switch (dis) {
                case 0:
                case 4:
                case 7:
                    mLeft = name + "+";
                    break;
                case 1:
                case 5:
                case 6:
                case 9:
                    mRight = name + "-";
                    break;
                case 3:
                case 8:
                    mLeft = name + "-";
                    break;
                case 2:
                case 10:
                    mRight = name + "+";
                    break;
            }
        }

        mBtnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRprice = mPrice.getText().toString();
                mRwhere = mWhere.getText().toString();

                if (mRprice.equals("") ) {
                    Toast.makeText(NewIS.this, "금액을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if(mRwhere.equals("")){
                    Toast.makeText(NewIS.this, "메모를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else {
                    mRdate = new Date(mDatePick.getYear() - 1900, mDatePick.getMonth(), mDatePick.getDayOfMonth(), mTimePick.getCurrentHour(), mTimePick.getCurrentMinute());

                    mWaitDlg = new WaitDlg(NewIS.this, "Please Wait", "Loading...");
                    mWaitDlg.start();

                    mRpart = "self";

                    if (parent.getItemAtPosition(position).equals("추가+")) {
                        if (dis == 0 || dis == 4 || dis == 7) {
                            //assetList에 넣어라
                            BsItem.INSTANCE.insertOne(mLeft, 0);
                        } else if (dis == 2) {
                            //debtList에 넣어라
                            BsItem.INSTANCE.insertOne(mRight, 1);
                        }

                    }

                    BsDB.INSTANCE.newIsInsert(mRprice, mRwhere, mRdate, mRtype, mRpart, mLeft, mRight);

                    Intent intent = new Intent(NewIS.this, MainActivity.class);
                    intent.putExtra("index", 2);
                    startActivity(intent);
                    NewIS.this.finish();
                    mMainActivity.finish();

                    WaitDlg.stop(mWaitDlg);
                }
            }
        });

    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}