package com.helloants.mm.helloants1.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.feedback.QandA;
import com.helloants.mm.helloants1.activity.feedback.Request;
import com.helloants.mm.helloants1.activity.feedback.SMSActivity;
import com.helloants.mm.helloants1.activity.mypage.AssetModify;
import com.helloants.mm.helloants1.activity.mypage.CardModify;
import com.helloants.mm.helloants1.activity.mypage.DebtModify;
import com.helloants.mm.helloants1.activity.mypage.EditSalary;
import com.helloants.mm.helloants1.activity.mypage.Profile;
import com.helloants.mm.helloants1.activity.mypage.Scrap;
import com.helloants.mm.helloants1.data.notification.cardOffsetAlarm;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.db.mypage.QandADB;
import com.helloants.mm.helloants1.db.mypage.RequestDB;
import com.helloants.mm.helloants1.db.mypage.ScrapDB;
import com.helloants.mm.helloants1.fragment.financeInfo.FinanceInfo;
import com.helloants.mm.helloants1.fragment.financeInfo.News;
import com.helloants.mm.helloants1.fragment.financeInfo.Pcmt;
import com.helloants.mm.helloants1.fragment.financeInfo.Tip;
import com.helloants.mm.helloants1.fragment.wm.BSFragment;
import com.helloants.mm.helloants1.fragment.wm.ISFragment;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoTimeoutException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawer;
    TabLayout tab;
    FragmentManager fragmentManager;
    DrawerLayout drawerLayout;
    BackPressCloseHandler backPressCloseHandler;
    private String[] cardND;
    public static ViewPager vpPager;
    public static Toolbar toolbar;

    public static Toolbar getToolbar() {
        return toolbar;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ModifyDelete.mMainActivity = this;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        backPressCloseHandler = new BackPressCloseHandler(this);

        String email = "";
        try {
            email = Cryptogram.Decrypt(LoginData.mEmail);
        } catch (Exception e) {
        }

//        try {
            final String finalEmail = email;
            final String[] date = new String[1];
            final Set set = new HashSet();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DBCursor cursor = MemberDB.INSTANCE.find(new BasicDBObject("email", finalEmail));
                        BasicDBObject obj = (BasicDBObject) cursor.next();
                        date[0] = obj.get("salaryDate").toString();
                        List list = (List) obj.get("cardOffsetDay");
                        Iterator iter = list.iterator();
                        while (iter.hasNext()) {
                            set.add(iter.next());
                        }
                    } catch (NullPointerException e) {
                        date[0] = "0.0";
                    } catch (MongoTimeoutException e) {
                        date[0] = "0.0";
                    } catch (NoSuchElementException e) {
                        date[0] = "0.0";
                    }
                }
            });

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
            }

            int salaryDate = (int) Double.parseDouble(date[0]);
            if (salaryDate != 0) {
                List<PendingIntent> broadcastList = new ArrayList();
                List<Calendar> calendarList = new ArrayList();
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                notificationIntent.addCategory("android.intent.category.DEFAULT");
                for (int i = 0; i < 12; i++) {
                    PendingIntent broadcast = PendingIntent.getBroadcast(this, i + 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    broadcastList.add(broadcast);
                    Calendar cal = Calendar.getInstance();

                    int today = cal.getTime().getDate();
                    if (today < salaryDate) {
                        cal.add(Calendar.MONTH, i);
                    } else {
                        cal.add(Calendar.MONTH, i + 1);
                    }
                    cal.set(Calendar.DAY_OF_MONTH, salaryDate);
                    cal.set(Calendar.HOUR_OF_DAY, 12);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    calendarList.add(cal);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendarList.get(i).getTimeInMillis(), broadcastList.get(i));
                }
            }
            //카드마다 for문 돌면서 날짜 체크
            for (Object card : set) {
                String a = String.valueOf(card);
                cardND = a.split("~");
                int cardDate = Integer.parseInt(cardND[1]);

                List<PendingIntent> broadcastList2 = new ArrayList();
                List<Calendar> calculationList = new ArrayList();
                AlarmManager calculation = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent coding = new Intent(MainActivity.this, cardOffsetAlarm.class);
                coding.setData(Uri.parse(cardND[0]));

                for (int i = 0; i < 12; i++) {
                    PendingIntent broadcast2 = PendingIntent.getBroadcast(this, i + 1, coding, PendingIntent.FLAG_UPDATE_CURRENT);
                    broadcastList2.add(broadcast2);
                    Calendar calendar = Calendar.getInstance();

                    int today = calendar.getTime().getDate();
                    if (today < cardDate) {
                        calendar.add(Calendar.MONTH, i);
                    } else {
                        calendar.add(Calendar.MONTH, i + 1);
                    }

                    calendar.set(Calendar.DAY_OF_MONTH, cardDate);
                    calendar.set(Calendar.HOUR_OF_DAY, 12);
                    calendar.set(Calendar.MINUTE, 40);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    calculationList.add(calendar);
                    calculation.set(AlarmManager.RTC, calculationList.get(i).getTimeInMillis(), broadcastList2.get(i));
                }
            }

            //툴바셋업
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBar ab = getSupportActionBar();
            if (null != ab) {
                ab.setHomeAsUpIndicator(R.drawable.ic_menuhello);
                ab.setDisplayHomeAsUpEnabled(true);
            }

            //드로워 셋업
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            NavigationView nv = (NavigationView) findViewById(R.id.navigation_view);
            nv.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                            if (menuItem.isCheckable()) {
                                menuItem.setChecked(true);
                            }
                            switch (menuItem.getItemId()) {
                                case R.id.nav_scrap:
                                    Intent intent0 = new Intent(MainActivity.this, Scrap.class);
                                    startActivity(intent0);
                                    break;
                                case R.id.nav_sms:
                                    Intent intent1 = new Intent(MainActivity.this, SMSActivity.class);
                                    startActivity(intent1);
                                    break;
                                case R.id.nav_request:
                                    Intent intent2 = new Intent(MainActivity.this, Request.class);
                                    startActivity(intent2);
                                    break;
                                case R.id.nav_qna:
                                    Intent intent3 = new Intent(MainActivity.this, QandA.class);
                                    startActivity(intent3);
                                    break;
                                case R.id.nav_profile:
                                    Intent intent4 = new Intent(MainActivity.this, Profile.class);
                                    startActivity(intent4);
                                    Profile.mMainActivity = MainActivity.this;
                                    break;
                                case R.id.nav_edit_asset:
                                    Intent intent5 = new Intent(MainActivity.this, AssetModify.class);
                                    startActivity(intent5);
                                    break;
                                case R.id.nav_edit_debt:
                                    Intent intent6 = new Intent(MainActivity.this, DebtModify.class);
                                    startActivity(intent6);
                                    break;
                                case R.id.nav_edit_salary:
                                    Intent intent7 = new Intent(MainActivity.this, EditSalary.class);
                                    startActivity(intent7);
                                    break;
                                case R.id.nav_edit_card:
                                    Intent intent8 = new Intent(MainActivity.this, CardModify.class);
                                    startActivity(intent8);
                                    break;

                            }
                            drawer.closeDrawers();
                            return true;
                        }
                    });
            String name = "";
            try {
                name = Cryptogram.Decrypt(LoginData.mName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            View v = nv.inflateHeaderView(R.layout.nav_header);
            TextView txvEmailHeader = (TextView) v.findViewById(R.id.email_navheader);
            txvEmailHeader.setText(email);
        Log.v("이메일",email);

            //탭레이아웃, 뷰페이저 셋업
            vpPager = (ViewPager) findViewById(R.id.viewpager);
            vpPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
            vpPager.setOffscreenPageLimit(3);
            tab = (TabLayout) findViewById(R.id.tabs);
            tab.setupWithViewPager(vpPager);

            //플로팅 액션바 셋업
            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, NewIS.class);
                    startActivity(intent);
                    NewIS.mMainActivity = MainActivity.this;
                }
            });
            fab.setVisibility(View.GONE);
            initData();
            vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    fab.setVisibility(View.GONE);
                    if (position == 1) {
                        fab.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            backPressCloseHandler = new BackPressCloseHandler(this);
//        } catch (Exception e) {
//            Log.v("메인 액티비티", e.toString());
//        }
    }

    @Override
    public void onBackPressed() {
        fragmentManager = getSupportFragmentManager();
        if (News.isGetVisible() || Pcmt.isGetVisible() || Tip.isGetVisible()) {
            News.setmIsVisible(false);
            Pcmt.setmIsVisible(false);
            Tip.setmIsVisible(false);
            fragmentManager.popBackStack();
            vpPager.setVisibility(View.VISIBLE);
            tab.setVisibility(View.VISIBLE);
        } else {
            backPressCloseHandler.onBackPressed();
        }
    }

    private void initData() {
        ScrapDB.INSTANCE.setScrapList();
        RequestDB.INSTANCE.list(1);
        QandADB.INSTANCE.list(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.option_setting:
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    //탭메뉴 생성(뷰페이저 아답타)
    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof BSFragment) {
                return POSITION_NONE;
            } else if (object instanceof ISFragment) {
                return POSITION_NONE;
            } else {
                return super.getItemPosition(object);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "재무상태";
                case 1:
                    return "경제활동";
                case 2:
                    return "금융정보";
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new BSFragment();
                case 1:
                    return new ISFragment();
                case 2:
                    return new FinanceInfo();
            }
            throw new IndexOutOfBoundsException();
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
                clearApplicationCache(null);
                activity.finish();
            }
        }

        private void showGuide() {
            Snackbar.make(drawerLayout, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT).show();
        }

        private void clearApplicationCache(java.io.File dir) {
            if (dir == null)
                dir = getCacheDir();
            if (dir == null)
                return;
            java.io.File[] children = dir.listFiles();
            try {
                for (int i = 0; i < children.length; i++)
                    if (children[i].isDirectory())
                        clearApplicationCache(children[i]);
                    else children[i].delete();
            } catch (Exception e) {
            }
        }
    }
}
