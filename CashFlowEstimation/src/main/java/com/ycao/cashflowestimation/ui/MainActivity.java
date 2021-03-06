package com.ycao.cashflowestimation.ui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.ui.fragment.CashFlowsFragment;
import com.ycao.cashflowestimation.ui.fragment.EstimationFragment;
import com.ycao.cashflowestimation.ui.fragment.InvoiceSummaryFragment;
import com.ycao.cashflowestimation.utils.Constants;

import java.util.Locale;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboFragmentActivity implements ActionBar.TabListener {

    private static final String CLASS_NAME = MainActivity.class.getName();

    @Inject
    private SQLiteConnector sqlConn;

    // Create the adapter that will return a fragment for each of the three
    // primary sections of the app.
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // Set up the ViewPager with the sections adapter.
    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        sqlConn.open();
        bootstrap();
    }

    private void bootstrap() {
        SharedPreferences settings = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        if (!settings.getBoolean(Constants.INITIALIZED, false)) {
            SharedPreferences.Editor editor = settings.edit();
            sqlConn.bootstrapData();
            editor.putBoolean(Constants.INITIALIZED, true);
            editor.putFloat(Constants.WEEKDAY_INCOME, Constants.WEEKDAY_INCOME_DEFAULT);
            editor.putFloat(Constants.WEEKEND_INCOME, Constants.WEEKEND_INCOME_DEFAULT);
            Log.i(CLASS_NAME, "Cashflow Estimation initailized");
            editor.commit();
        } else {
            Log.i(CLASS_NAME, "Cashflow Estimation already initailized, skip bootstrapping");
        }
    }

    private void cleanup() {
        this.deleteDatabase(sqlConn.getDatabaseName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment cashEstimationFragment;
        Fragment cashflowFragment;
        Fragment invoiceSummaryFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (cashEstimationFragment == null) {
                        cashEstimationFragment = new EstimationFragment();
                    }
                    return cashEstimationFragment;

                case 1:
                    if (invoiceSummaryFragment == null) {
                        invoiceSummaryFragment = new InvoiceSummaryFragment();
                    }
                    return invoiceSummaryFragment;

                case 2:
                    if (cashflowFragment == null) {
                        cashflowFragment = new CashFlowsFragment();
                    }
                    return cashflowFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

}
