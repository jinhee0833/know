package com.helloants.mm.helloants1.fragment.wm;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.MainActivity;
import com.helloants.mm.helloants1.adapters.bsAdapter;
import com.helloants.mm.helloants1.adapters.debtAdapter;
import com.helloants.mm.helloants1.data.DeviceSize;
import com.helloants.mm.helloants1.data.constant.Icon;
import com.helloants.mm.helloants1.data.type.BSType;
import com.helloants.mm.helloants1.data.type.ISType;
import com.helloants.mm.helloants1.db.bs.BsDB;
import com.helloants.mm.helloants1.db.bs.BsItem;
import com.helloants.mm.helloants1.db.member.MemberDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class BSFragment extends Fragment {
    View v;
    public static ArrayList<BSType> mAssetList = new ArrayList<>();
    public static ArrayList<BSType> mDebtList = new ArrayList<>();
    private ArrayList<ISType> mBSList;
    private Date mFirstDate;
    private String[] mAssetArray;
    private String[] mDebtArray;
    private long mAssetLeft;
    private long mAssetRight;
    private long mDebtLeft;
    private long mDebtRight;
    private long mDebtSum = 0;
    private long mAssetSum = 0;
    private TextView mTotalEquity;
    private TextView mTotalEquity2;
    private TextView mTotalEquity1;
    private TextView mTotalWon;
    private TextView mTotalWon1;
    private TextView mTotalWon2;
    private RelativeLayout rel;
    private RelativeLayout rel1;
    private RelativeLayout rel2;
    private PieChart mChart;
    private String[] xData = {"", ""};

    private float weightDebtNum;
    private float weightEquityNum;
    private TextView mAssetText;
    private TextView mEquityText;
    private TextView mDebtText;
    private TextView mEquityText2;
    private TextView mDebtText2;
    private long TotalAsset;
    private long TotalEquity;
    private long TotalDebt;
    public static Map<String, Long> mPriceMap;
    public static bsAdapter mAssetAdapter;
    public static debtAdapter mDebtAdapter;
    private BarChart mBarChart;
    private ArrayList<ISType> monthSumList = new ArrayList<>();
    Date sixMonthArray[] = new Date[7];
    private ArrayList<ISType> mBudgetPreList = new ArrayList<>();
    private ArrayList<ISType> mBudgetNowList = new ArrayList<>();
    public static LineChart mLineChart;
    private String mBudget;
    private long sum;
    private long sum2;
    private long sum4;
    private CardView cardView;
    private CardView cardView1;
    private CardView cardView2;
    public static TextView budget;
    public static ArrayList<Entry> yVals3;
    public static LineDataSet set1;
    public static LineDataSet set2;
    public static LineDataSet set3;
    public static LineDataSet set4;
    public static LineData data;
    public static ArrayList<String> xVals;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_bs, container, false);

//        try {
        //DB에서 이 이용자가 가지고있는 자산항목과 부채항목 가지고 오기
        Thread t = new Thread() {
            @Override
            public void run() {
                BsItem.INSTANCE.setArray();
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
        }

        //디비에 있는 자산부채항목 가져와서 뿌리기(누적)
        initStatic();

        //모든 거래내역 가져오기
        mBSList = BsDB.INSTANCE.initData();
        //초기값 입력한 날짜
        mFirstDate = BsDB.INSTANCE.firstDate();
        //BsItem에 들어있는 자산 부채 목록
        mAssetArray = BsItem.INSTANCE.getAssetArrayy();
        mDebtArray = BsItem.INSTANCE.getDebtArray();
        //예산 가져오기
        mBudget = MemberDB.INSTANCE.budgetFind();

        //최근 6개월의 1일을 배열에 담기
        Calendar cal = Calendar.getInstance();//오늘 날짜를 기준으루..
        cal.add(Calendar.MONTH, -5);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < 7; i++) {
            sixMonthArray[i] = new Date(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, +1);
        }
        sixMonthArray[6] = new Date(Calendar.getInstance().getTimeInMillis());

        //앱을 설치하고 초기값 입력한 날짜 이후의 거래내역들이 반영되어야함. 걸러내서 리스트에 집어넣음
        ArrayList<ISType> compareList = new ArrayList();
        for (ISType i : mBSList) {
            if (i.mDate.equals(mFirstDate) || i.mDate.after(mFirstDate)) {
                compareList.add(i);
            }
            if (i.mDate.equals(sixMonthArray[0]) || i.mDate.after(sixMonthArray[0])) {//최근 6개월의 경제활동만 구분하기
                monthSumList.add(i);
            }
        }
        //예산그래프위해 지난달과 이번달 거래내역 리스트에 담음
        for (ISType i : monthSumList) {
            Date date = i.mDate;
            if (date.after(sixMonthArray[5]) && date.before(sixMonthArray[6])) {
                if (i.mType.equals("check") || i.mType.equals("credit") || i.mType.equals("cashExpend")) {
                    mBudgetNowList.add(i);
                }

            }
            if (date.after(sixMonthArray[4]) && date.before(sixMonthArray[5])) {
                if (i.mType.equals("check") || i.mType.equals("credit") || i.mType.equals("cashExpend")) {
                    mBudgetPreList.add(i);
                }
            }
        }

        //자산리스트에 들어갈 데이터 세팅
        for (String j : mAssetArray) {
            for (ISType i : compareList) {
                String aR = i.mRight;
                String aL = i.mLeft;
                String afterStrRight = aR.substring(0, aR.length() - 1);
                String afterStrLeft = aL.substring(0, aL.length() - 1);
                String afterJ = "";
                try {
                    afterJ = j.substring(0, j.length() - 1);
                } catch (NullPointerException e) {
                }
                if (afterStrLeft.equals(afterJ)) {
                    try {
                        mAssetLeft += Long.parseLong(i.mPrice);
                    } catch (NumberFormatException e) {
                    }
                } else if (afterStrRight.equals(afterJ)) {
                    try {
                        mAssetRight += Long.parseLong(i.mPrice);
                    } catch (NumberFormatException e) {
                    }
                }
            }
            BSType bsType = new BSType();
            bsType.setName(j);
            Long asset = mAssetLeft - mAssetRight;
            mPriceMap.put(j, asset);
            bsType.setValue(asset);
            mAssetSum += asset;
            mAssetList.add(bsType);
            mAssetRight = 0;
            mAssetLeft = 0;
        }

        //부채리스트에 들어갈 데이터 세팅
        for (String h : mDebtArray) {
            for (ISType i : compareList) {
                String aR = i.mRight;
                String aL = i.mLeft;
                String afterStrRight = aR.substring(0, aR.length() - 1);
                String afterStrLeft = aL.substring(0, aL.length() - 1);
                String afterH = "";
                try {
                    afterH = h.substring(0, h.length() - 1);
                } catch (Exception e) {

                }
                if (afterStrLeft.equals(afterH)) {
                    try {
                        mDebtLeft += Long.parseLong(i.mPrice);
                    } catch (NumberFormatException e) {
                    }
                } else if (afterStrRight.equals(afterH)) {
                    try {
                        mDebtRight += Long.parseLong(i.mPrice);
                    } catch (NumberFormatException e) {
                    }
                }

            }
            BSType bsType = new BSType();
            bsType.setName(h);
            Long debt = mDebtRight - mDebtLeft;
            mPriceMap.put(h, debt);
            bsType.setValue(debt);
            mDebtSum += debt;
            mDebtList.add(bsType);
            mDebtLeft = 0;
            mDebtRight = 0;
        }


        //합계넣기
        BSType bsTypeAssetSum = new BSType();
        bsTypeAssetSum.setName("총자산+");
        bsTypeAssetSum.setValue(mAssetSum);
        mAssetSum = 0;
        mAssetList.add(bsTypeAssetSum);

        BSType bsTypeDebtSum = new BSType();
        bsTypeDebtSum.setName("빚+");
        bsTypeDebtSum.setValue(mDebtSum);
        mDebtSum = 0;
        mDebtList.add(bsTypeDebtSum);

        //자산 리스트뷰
        ListView assetListView = (ListView) v.findViewById(R.id.list_asset_bsfrag);
        mAssetAdapter.setList(mAssetList);
        assetListView.setAdapter(mAssetAdapter);

        //부채 리스트뷰
        ListView debtListView = (ListView) v.findViewById(R.id.list_debt_bsfrag);
        mDebtAdapter.setList(mDebtList);
        debtListView.setAdapter(mDebtAdapter);

        //자산리스트뷰 UI
        int numberOfItems = mAssetAdapter.getCount();
        int totalItemsHeight = 0;
        for (int g = 0; g < numberOfItems; g++) {
            View item = mAssetAdapter.getView(g, null, assetListView);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }
        int totalDividersHeight = assetListView.getDividerHeight() * (numberOfItems - 1);
        ViewGroup.LayoutParams params = assetListView.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        assetListView.setLayoutParams(params);
        assetListView.requestLayout();

        //부채리스트뷰 UI
        int numberOfItems2 = mDebtAdapter.getCount();
        int totalItemsHeight2 = 0;
        for (int g = 0; g < numberOfItems2; g++) {
            View item = mDebtAdapter.getView(g, null, debtListView);
            item.measure(0, 0);
            totalItemsHeight2 += item.getMeasuredHeight();
        }
        int totalDividersHeight2 = debtListView.getDividerHeight() * (numberOfItems2 - 1);
        ViewGroup.LayoutParams params2 = debtListView.getLayoutParams();
        params2.height = totalItemsHeight2 + totalDividersHeight2;
        debtListView.setLayoutParams(params2);
        debtListView.requestLayout();

        //맨위에 총자산 대비 부채 내돈 %로 표현하기
        TotalAsset = mAssetList.get(mAssetList.size() - 1).getValue();
        TotalDebt = mDebtList.get(mDebtList.size() - 1).getValue();
        TotalEquity = TotalAsset - TotalDebt;
        weightDebtNum = (float) ((double) TotalDebt / (double) TotalAsset) * 100;
        weightEquityNum = 100 - weightDebtNum;
        mAssetText = (TextView) v.findViewById(R.id.asset_text);
        mEquityText = (TextView) v.findViewById(R.id.equity_text);
        mDebtText = (TextView) v.findViewById(R.id.debt_text);
        mEquityText2 = (TextView) v.findViewById(R.id.equity_text2);
        mDebtText2 = (TextView) v.findViewById(R.id.debt_text2);
        LinearLayout equityLinear = (LinearLayout) v.findViewById(R.id.equity_linear);
        LinearLayout debtLinear = (LinearLayout) v.findViewById(R.id.debt_linear);
        int equityHeight = equityLinear.getHeight();
        int debtHeight = debtLinear.getHeight();

        mAssetText.setText("총자산  ::  " + String.format("%,d", TotalAsset) + "원");
        mAssetText.setTextColor(Color.parseColor("#0066CC"));

        mEquityText.setText("순자산");
        mEquityText.setTextColor(Color.parseColor("#00CC99"));
        mEquityText2.setText(Currency.getInstance(Locale.KOREA).getSymbol() + " " + String.format("%,d", TotalEquity));

        mEquityText2.setTextColor(Color.parseColor("#00CC99"));
        mDebtText.setText("빚");
        mDebtText.setTextColor(Color.parseColor("#FF0066"));
        mDebtText2.setText(Currency.getInstance(Locale.KOREA).getSymbol() + " " + String.format("%,d", TotalDebt));

        mDebtText2.setTextColor(Color.parseColor("#FF0066"));

        //파이차트 그리기
        mChart = (PieChart) v.findViewById(R.id.pie_chart);
        mChart.setMinimumWidth(DeviceSize.mWidth / 3);
        mChart.setMinimumHeight(DeviceSize.mWidth / 3);
        mChart.setTouchEnabled(false);
        mChart.setHoleRadius(40);
        mChart.setDrawSliceText(false);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.setRotationEnabled(false);
        mChart.animateXY(1000, 1000);
        addData();

        //바차트 그리기
        mBarChart = (BarChart) v.findViewById(R.id.bs_barchart);
        mBarChart.setMinimumHeight(DeviceSize.mWidth / 2);
        BarData data = new BarData(getXAxisValues(), getDataSet());
        mBarChart.setData(data);
        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.getLegend().setEnabled(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.animateXY(2000, 2000);
        mBarChart.setDescription("");

        data.setHighlightEnabled(false);
        YAxis yAxisRight = mBarChart.getAxisRight();
        YAxis yAxisLeft = mBarChart.getAxisLeft();
        yAxisLeft.setAxisMinValue(0f);
        yAxisRight.setEnabled(false);
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        data.setValueTextSize(10);
        mBarChart.invalidate();

        //라인차트
        mLineChart = (LineChart) v.findViewById(R.id.bs_linechart);
        mLineChart.setMinimumHeight(DeviceSize.mWidth / 2);

        mLineChart.setDescription("");
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(false);
        mLineChart.setScaleEnabled(false);
        mLineChart.setPinchZoom(false);
        mLineChart.setDoubleTapToZoomEnabled(false);
        mLineChart.setDrawGridBackground(false);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mLineChart.setHardwareAccelerationEnabled(false);
        }
        XAxis x = mLineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setAxisLineColor(Color.parseColor("#40303030"));
        x.setAxisLineWidth(0.5f);
        YAxis yAxisLineLeft = mLineChart.getAxisLeft();
        YAxis yAxisRightLine = mLineChart.getAxisRight();
        yAxisLineLeft.setAxisMinValue(0f);
        yAxisRightLine.setAxisMinValue(0f);
        yAxisRightLine.setEnabled(false);
        setData();

        yAxisLineLeft.setEnabled(false);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.animateXY(2000, 2000);
        mLineChart.invalidate();
        Typeface fontFamily = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome.ttf");

        TextView thisMonth = (TextView) v.findViewById(R.id.bs_thisMonth);
        TextView preMonth = (TextView) v.findViewById(R.id.bs_preMonth);
        budget = (TextView) v.findViewById(R.id.bs_budget);
        TextView thisMonthIcon = (TextView) v.findViewById(R.id.thisMonth_image);
        TextView preMonthIcon = (TextView) v.findViewById(R.id.preMonth_image);
        TextView budgetIcon = (TextView) v.findViewById(R.id.budget_image);
        TextView today = (TextView) v.findViewById(R.id.today);
        thisMonthIcon.setTypeface(fontFamily);
        preMonthIcon.setTypeface(fontFamily);
        budgetIcon.setTypeface(fontFamily);
        thisMonthIcon.setText(Icon.SPOT);
        preMonthIcon.setText(Icon.SPOT);
        budgetIcon.setText(Icon.SPOT);
        budgetIcon.setTextColor(Color.parseColor("#FF0000"));
        preMonthIcon.setTextColor(Color.parseColor("#30FFD237"));
        thisMonthIcon.setTextColor(Color.parseColor("#FFD237"));
        Date date = new Date();
        String simpleDateFormat = new SimpleDateFormat("M월 d일 E").format(date);
        today.setText(simpleDateFormat);
        thisMonth.setText("이번달   " + String.format("%,d", sum2) + "원");
        preMonth.setText("지난달   " + String.format("%,d", sum) + "원");
        try {
            budget.setText("예산   " + String.format("%,d", Integer.parseInt(mBudget)) + "원");
        } catch (NumberFormatException ex) {
            budget.setText("예산   21억원 이상");
        }


        //내돈영역 위치와 크기 계산해 넣기
        mTotalEquity = (TextView) v.findViewById(R.id.txv_totalequity_bsfrag);
        mTotalEquity1 = (TextView) v.findViewById(R.id.txv_totalequity_bsfrag1);
        mTotalEquity2 = (TextView) v.findViewById(R.id.txv_totalequity_bsfrag2);
        mTotalWon = (TextView) v.findViewById(R.id.txv_totalequity_bsfrag_1);
        mTotalWon1 = (TextView) v.findViewById(R.id.txv_totalequity_bsfrag1_1);
        mTotalWon2 = (TextView) v.findViewById(R.id.txv_totalequity_bsfrag2_1);
        mTotalEquity.setText("순자산 ");
        mTotalEquity1.setText("순자산 ");
        mTotalEquity2.setText("순자산 ");
        mTotalWon.setText(Currency.getInstance(Locale.KOREA).getSymbol() + " " + String.format("%,d", TotalEquity));
        mTotalWon1.setText(Currency.getInstance(Locale.KOREA).getSymbol() + " " + String.format("%,d", TotalEquity));
        mTotalWon2.setText(Currency.getInstance(Locale.KOREA).getSymbol() + " " + String.format("%,d", TotalEquity));
        mTotalEquity.setTextColor(Color.parseColor("#00CC99"));
        mTotalEquity1.setTextColor(Color.parseColor("#00CC99"));
        mTotalEquity2.setTextColor(Color.parseColor("#00CC99"));
        mTotalWon.setTextColor(Color.parseColor("#00CC99"));
        mTotalWon1.setTextColor(Color.parseColor("#00CC99"));
        mTotalWon2.setTextColor(Color.parseColor("#00CC99"));
        rel = (RelativeLayout) v.findViewById(R.id.bs_relative);
        rel1 = (RelativeLayout) v.findViewById(R.id.bs_relative1);
        rel2 = (RelativeLayout) v.findViewById(R.id.bs_relative2);
        cardView = (CardView) v.findViewById(R.id.cardview);
        cardView1 = (CardView) v.findViewById(R.id.cardview1);
        cardView2 = (CardView) v.findViewById(R.id.cardview2);
        if (params.height > params2.height) {//자산항목이 많으면
            debtLinear.setMinimumHeight(equityHeight);
//            rel2.setMinimumHeight(params.height - (params2.height));
            rel2.setBackgroundColor(Color.parseColor("#FFFFFF"));
            cardView.setVisibility(View.GONE);
            cardView1.setVisibility(View.GONE);
            mTotalEquity.setVisibility(View.GONE);
            mTotalEquity1.setVisibility(View.GONE);
            mTotalWon.setVisibility(View.GONE);
            mTotalWon1.setVisibility(View.GONE);
        } else if (params.height < params2.height) {//부채항목이 많으면
            equityLinear.setMinimumHeight(debtHeight);
//            rel1.setMinimumHeight(params2.height - (params.height));
            rel1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            cardView2.setVisibility(View.GONE);
            cardView.setVisibility(View.GONE);
            mTotalEquity.setVisibility(View.GONE);
            mTotalEquity2.setVisibility(View.GONE);
            mTotalWon.setVisibility(View.GONE);
            mTotalWon2.setVisibility(View.GONE);
        } else if (params.height == params2.height) {//자산 부채 항목갯수가 같으면
            cardView1.setVisibility(View.GONE);
            cardView2.setVisibility(View.GONE);
            mTotalEquity2.setVisibility(View.GONE);
            mTotalEquity1.setVisibility(View.GONE);
            mTotalWon1.setVisibility(View.GONE);
            mTotalWon2.setVisibility(View.GONE);
        }

        //스크롤 맨위로
        final NestedScrollView scrollView = (NestedScrollView) v.findViewById(R.id.scroll_bsfrag);

        MainActivity activity = (MainActivity) getActivity();
        ViewPager vp = (ViewPager) activity.findViewById(R.id.viewpager);

        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                BSType.setmCheck2(true);
                scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (position == 0) {
                            BSType.setmCheck2(false);
                        }

                        if (position == 1) {
                            if (scrollView.getScrollY() != 0 && BSType.ismCheck2()) {
                                scrollView.scrollTo(0, 0);
                            }
                        }
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
//        } catch (Exception e) {
//        }

        return v;
    }

    private void initStatic() {
        mAssetList = new ArrayList<>();
        mDebtList = new ArrayList<>();
        mPriceMap = new HashMap<>();
        mAssetAdapter = new bsAdapter();
        mDebtAdapter = new debtAdapter();
    }

    private void addData() {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        if (TotalEquity < 0) {
            yVals1.add(new Entry(100, 0));
            yVals1.add(new Entry(0, 1));
        } else if (TotalEquity == 0 && TotalAsset == 0 && TotalDebt == 0) {
            yVals1.add(new Entry(50, 0));
            yVals1.add(new Entry(50, 1));
        } else {
            yVals1.add(new Entry(weightDebtNum, 0));
            yVals1.add(new Entry(weightEquityNum, 1));
        }

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);
        PieDataSet dataSet = new PieDataSet(yVals1, "");
        ArrayList<Integer> colors = new ArrayList<Integer>();
        int[] LIBERTY_COLORS = new int[]{Color.parseColor("#70FF0066"), Color.parseColor("#7000CC99")};
        for (int c : LIBERTY_COLORS)
            colors.add(c);

        dataSet.setColors(colors);
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.GRAY);

        mChart.setData(data);
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAssetList.clear();
        mDebtList.clear();
    }

    private ArrayList<IBarDataSet> getDataSet() {
        ArrayList<IBarDataSet> dataSets = null;

        long[] monthSum = new long[6];
        //월별 지출 합계 계산
        for (int i = 0; i < monthSumList.size(); i++) {
            Date date = monthSumList.get(i).mDate;
            String type = monthSumList.get(i).mType;
            for (int j = 0; j < 6; j++) {
                if (date.after(sixMonthArray[j]) && date.before(sixMonthArray[j + 1])) {
                    if (type.equals("check") || type.equals("credit") || type.equals("cashExpend")) {
                        try {
                            monthSum[j] += Long.parseLong(monthSumList.get(i).mPrice);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        }

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(monthSum[0], 0); // Jan
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry(monthSum[1], 1); // Feb
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry(monthSum[2], 2); // Mar
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry(monthSum[3], 3); // Apr
        valueSet1.add(v1e4);
        BarEntry v1e5 = new BarEntry(monthSum[4], 4); // May
        valueSet1.add(v1e5);
        BarEntry v1e6 = new BarEntry(monthSum[5], 5); // Jun
        valueSet1.add(v1e6);

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, null);
        barDataSet1.setColor(Color.parseColor("#aaFF0066"));

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        int nowMont = Integer.parseInt(new SimpleDateFormat("MM").format(new Date()));
        String[] tabNames = new String[6];
        for (int i = 0; i < 6; i++) {
            String month = String.valueOf(((nowMont - 5) + i) % 12) + "월";
            if (month.equals("0월")) {
                tabNames[i] = "12월";
            } else {
                tabNames[i] = month;
            }
        }
        xAxis.add(tabNames[0]);
        xAxis.add(tabNames[1]);
        xAxis.add(tabNames[2]);
        xAxis.add(tabNames[3]);
        xAxis.add(tabNames[4]);
        xAxis.add(tabNames[5]);
        return xAxis;
    }

    private void setData() {
        ArrayList<Long> preDaySum = new ArrayList();
        ArrayList<Long> nowDaySum = new ArrayList();
        for (int i = 1; i < 32; i++) {
            int iDate = i;
            long sum = 0;
            for (int j = 0; j < mBudgetPreList.size(); j++) {
                int preListDate = Integer.parseInt(new SimpleDateFormat("dd").format(mBudgetPreList.get(j).mDate));
                if (iDate == preListDate) {
                    try {
                        sum += Long.parseLong(mBudgetPreList.get(j).mPrice);
                    } catch (Exception ex) {

                    }
                }
            }
            preDaySum.add(sum);

            long sum2 = 0;
            for (int j = 0; j < mBudgetNowList.size(); j++) {
                int nowListDate = Integer.parseInt(new SimpleDateFormat("dd").format(mBudgetNowList.get(j).mDate));
//                if (iDate < Integer.parseInt(new SimpleDateFormat("dd").format(new Date()))){
                if (iDate == nowListDate) {
                    try {
                        sum2 += Long.parseLong(mBudgetNowList.get(j).mPrice);
                        Log.v("이번달", String.valueOf(sum2));
                    } catch (Exception e) {
                    }
                }
//                }else {
//                    return;
//                }

            }
            nowDaySum.add(sum2);

        }

        xVals = new ArrayList<String>();
        for (int i = 1; i < 32; i++) {
            xVals.add(String.valueOf((i)) + "일");
        }

        //지난달
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        sum = 0;
        for (int i = 0; i < preDaySum.size(); i++) {
            sum += preDaySum.get(i);
            yVals.add(new Entry(sum, i));
        }

        //이번달
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        sum2 = 0;
        for (int i = 0; i < nowDaySum.size(); i++) {
            sum2 += (float) nowDaySum.get(i);
            yVals2.add(new Entry(sum2, i));
        }

        yVals3 = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            try {
                yVals3.add(new Entry(Long.parseLong(mBudget), i));
            } catch (NumberFormatException e) {
                yVals3.add(new Entry(Long.MAX_VALUE, i));
            }
        }

        ArrayList<Entry> yVals4 = new ArrayList<Entry>();
        sum4 = 0;
        for (int i = 0; i < nowDaySum.size(); i++) {
            sum4 += (float) nowDaySum.get(i);
            yVals4.add(new Entry(sum4, i));
        }

        set1 = new LineDataSet(yVals, "");
        set1.setDrawFilled(true);
        set1.setFillColor(Color.parseColor("#FFD237"));
        set1.setDrawCircles(false);
        set1.setLineWidth(0);
        set1.setColor(Color.parseColor("#30FFD237"));
        set1.setFillAlpha(30);

        set2 = new LineDataSet(yVals2, "");
        set2.setLineWidth(3f);
        set2.setColor(Color.parseColor("#FFD237"));
        set2.setDrawCircles(false);

        set3 = new LineDataSet(yVals3, "");
        set3.setLineWidth(3f);
        set3.setColor(Color.parseColor("#FF0000"));
        set3.setDrawCircles(false);

        set4 = new LineDataSet(yVals4, "");
        set4.setLineWidth(0);
        set4.setColor(Color.parseColor("#30FFD237"));
        set4.setDrawCircles(true);
        set4.setDrawCircleHole(true);
        set4.setCircleColor(Color.parseColor("#FFD237"));
        set4.setCircleSize(6f);

        int today = Integer.parseInt(new SimpleDateFormat("dd").format(new Date()));
        for (int i = today + 1; i < 32; i++) {
            set2.removeEntry(i);
            set4.removeEntry(i);
        }
        for (int i = 0; i < today - 1; i++) {
            set4.removeEntry(i);
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set4);
        dataSets.add(set3);
        dataSets.add(set2);
        dataSets.add(set1);

        data = new LineData(xVals, dataSets);
        data.setValueTextSize(9f);
        data.setDrawValues(false);
        data.setHighlightEnabled(false);
        mLineChart.setData(data);
    }
}