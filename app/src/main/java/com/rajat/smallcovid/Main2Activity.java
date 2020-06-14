package com.rajat.smallcovid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.google.gson.Gson;
import com.rajat.smallcovid.Adapter.StateAdapter;
import com.rajat.smallcovid.Models.History;
import com.rajat.smallcovid.Models.HistoryModel;
import com.rajat.smallcovid.Models.NewApiModels.ModelAPI;
import com.rajat.smallcovid.Models.NewApiModels.States;
import com.rajat.smallcovid.Models.Statewise;
import com.rajat.smallcovid.Utils.Helper;
import com.rajat.smallcovid.Utils.URLS;
import com.rajat.smallcovid.Utils.VolleySingleton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Main2Activity extends AppCompatActivity implements StateAdapter.setOnClickListener {

    private static String TAG = "Main2Activity";
    private ArrayList<Statewise> dataArrayList;
    private ArrayList<States> statesArrayList;
    private ModelAPI modelAPI;
    private RecyclerView recyclerView;
    private StateAdapter stateAdapter;
    private HistoryModel historyModel;
    private Context context;
    private TextView country_confirmed_current, country_active_current, country_recovered_current, country_dead_current;
    private TextView country_confirmed_increased, country_recovered_increased, country_dead_increased, updated_time;
    private ArrayList<History> history;
    private ArrayList<Statewise> currentDataList;
    private RelativeLayout  loading_layout;
    private LinearLayout main_anim;
    private NestedScrollView scrollView;
    private PullRefreshLayout pullRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        statesArrayList = new ArrayList<>();
        context = this;
        initViews();
        getResponse();

        refreshRecycler();

    }

    private void initViews() {
        recyclerView = findViewById(R.id.main_recycler);
        country_confirmed_current = findViewById(R.id.country_confirmed_current);
        country_active_current = findViewById(R.id.country_active_current);
        country_recovered_current = findViewById(R.id.country_recovered_current);
        country_dead_current = findViewById(R.id.country_dead_current);
        country_confirmed_increased = findViewById(R.id.country_confirmed_increased);
        country_recovered_increased = findViewById(R.id.country_recovered_increased);
        country_dead_increased = findViewById(R.id.country_dead_increased);
        main_anim = findViewById(R.id.main_anim);
        scrollView = findViewById(R.id.nested_scroll);
        pullRefreshLayout = findViewById(R.id.v_refresh);
        updated_time = findViewById(R.id.time);
        loading_layout = findViewById(R.id.loading_layout);
    }

    private void setRecyclerView() {
        main_anim.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
//        main_anim.setAnimation(AnimationUtils.loadAnimation(context, R.anim.up_bottom_transition_animation_d));
        scrollView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.up_bottom_transition_animation_d));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setHasFixedSize(true);
        ArrayList<States> statesList = statesArrayList;
        statesList.remove(0);
        stateAdapter = new StateAdapter(statesList, getApplicationContext(), this);
        recyclerView.setAdapter(stateAdapter);
        pullRefreshLayout.setRefreshing(false);

    }


    private void refreshRecycler() {

        int[] color = {getResources().getColor(R.color.red_active),
                getResources().getColor(R.color.green_active),
                getResources().getColor(R.color.blue),
                getResources().getColor(R.color.black)};
        pullRefreshLayout.setColorSchemeColors(color);

        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefreshLayout.setRefreshing(true);
                Toast.makeText(context, "REFRESHING", Toast.LENGTH_SHORT).show();
                getRefreshedResponse();

            }
        });
    }

    private void getResponse() {
        StringRequest request = new StringRequest(Request.Method.GET, URLS.newApiData, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    loading_layout.setVisibility(View.GONE);
                    Gson gson = new Gson();
                    modelAPI = gson.fromJson(response, ModelAPI.class);
                    statesArrayList = modelAPI.getState();
                    setTotalCount();
                    setRecyclerView();

                } catch (Exception e) {
                    Log.e(TAG, "onResponse: ", e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error);
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void getRefreshedResponse() {
        scrollView.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.GET, URLS.newApiData, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    loading_layout.setVisibility(View.GONE);
                    Gson gson = new Gson();
                    modelAPI = gson.fromJson(response, ModelAPI.class);
                    statesArrayList = modelAPI.getState();
                    setTotalCount();
                    refreshedRecycler();
                } catch (Exception e) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error);
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void refreshedRecycler() {
        Toast.makeText(context, "REFRESHED", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        scrollView.setVisibility(View.VISIBLE);
        scrollView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.up_bottom_transition_animation));

        ArrayList<States> statesList = statesArrayList;
        statesList.remove(0);
        stateAdapter = new StateAdapter(statesList, getApplicationContext(), this);

        stateAdapter.notifyDataSetChanged();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(stateAdapter);
            }
        }, 200);
        new Thread(new Runnable() {
            @Override
            public void run() {
                pullRefreshLayout.setRefreshing(false);
            }
        }).start();

    }

    private void getHistoryResponse() {
        history = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, URLS.UnOfficialHistoryApi, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Gson gson = new Gson();
                historyModel = gson.fromJson(response, HistoryModel.class);
                history = historyModel.getData().getHistory();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error);
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void sortConfirmedWise() {

        Collections.sort(statesArrayList, new Comparator<States>() {
            @Override
            public int compare(States o1, States o2) {
                long l1, l2;
                l1 = Long.parseLong(o1.getConfirmed());
                l2 = Long.parseLong(o2.getConfirmed());
                if (l1 > l2) {
                    return -1;
                } else if (l1 < l2) {
                    return 1;
                }
                return 0;
            }
        });

    }



    private void setTotalCount() {

        setTextFont();
        States state = statesArrayList.get(0);

        long confirm = Long.parseLong(state.getDeltaconfirmed());
        long recover = Long.parseLong(state.getDeltarecovered());
        long death = Long.parseLong(state.getDeltadeaths());
        String updatedTime = "LAST UPDATED " + timeAgo(state.getLastupdatedtime());

        if (confirm > 0) {
            String s = "+" + confirm;
            country_confirmed_increased.setText(s);
        } else {
            country_confirmed_increased.setText(" +0 ");
        }

        if (recover > 0) {
            String s = "+" + recover;
            country_recovered_increased.setText(s);
        } else {
            country_recovered_increased.setText(" +0 ");
        }

        if (death > 0) {
            String s = "+" + death;
            country_dead_increased.setText(s);
        } else {
            country_dead_increased.setText(" +0 ");
        }


        country_confirmed_current.setText(String.valueOf(state.getConfirmed()));
        country_active_current.setText(String.valueOf(state.getActive()));
        country_recovered_current.setText(String.valueOf(state.getRecovered()));
        country_dead_current.setText(String.valueOf(state.getDeaths()));
        updated_time.setText(updatedTime);

    }

    /*    private void setTotalCount() {
            setTextFont();
            Total totalCount = history.get(history.size() - 1).getTotal();
            Total diffCount = history.get(history.size() - 2).getTotal();
            long confirm = totalCount.getConfirmed() - diffCount.getConfirmed();
            long recover = totalCount.getRecovered() - diffCount.getRecovered();
            long death = totalCount.getDeaths() - diffCount.getDeaths();


            if (confirm > 0) {
                String s = "+ " + confirm;
                country_confirmed_increased.setText(s);
            } else if (confirm < 0) {
                String s = "- " + Math.abs(confirm);
                country_confirmed_increased.setText(s);
            }

            if (recover > 0) {
                String s = "+ " + recover;
                country_recovered_increased.setText(s);
            } else if (recover < 0) {
                String s = "- " + Math.abs(recover);
                country_recovered_increased.setText(s);
            }

            if (death > 0) {
                String s = "+ " + death;
                country_dead_increased.setText(s);
            } else if (death < 0) {
                String s = "- " + Math.abs(death);
                country_dead_increased.setText(s);
            }


            country_confirmed_current.setText(String.valueOf(totalCount.getConfirmed()));
            country_active_current.setText(String.valueOf(totalCount.getActive()));
            country_recovered_current.setText(String.valueOf(totalCount.getRecovered()));
            country_dead_current.setText(String.valueOf(totalCount.getDeaths()));


        }
    */

    private void setTextFont() {
        TextView confirmed = findViewById(R.id.country_confirmed_status);
        TextView active = findViewById(R.id.country_active_status);
        TextView recovered = findViewById(R.id.country_recovered_status);
        TextView dead = findViewById(R.id.country_dead_status);

        confirmed.setTypeface(Helper.getFontSb(context));
        active.setTypeface(Helper.getFontSb(context));
        recovered.setTypeface(Helper.getFontSb(context));
        dead.setTypeface(Helper.getFontSb(context));
        country_confirmed_current.setTypeface(Helper.getFontSb(context));
        country_active_current.setTypeface(Helper.getFontSb(context));
        country_recovered_current.setTypeface(Helper.getFontSb(context));
        country_dead_current.setTypeface(Helper.getFontSb(context));

    }

    private void search(SearchView searchView) {
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (stateAdapter != null) {
//                    moto.setVisibility(View.GONE);
                    stateAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {

            super.onBackPressed();

    }

    @Override
    public void onClick(int position, States statewise, View view) {
        statewise.setExpanded(!statewise.isExpanded());
        stateAdapter.notifyItemChanged(position);
        recyclerView.smoothScrollToPosition(position);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) Objects.requireNonNull(context).getSystemService(
                Context.INPUT_METHOD_SERVICE);
        try {
            View focusedView = Objects.requireNonNull(getCurrentFocus());
            if (focusedView != null) {
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "hideKeyboard: ", e);
        }

    }

    private String timeAgo(String date) {
        int day = 0;
        int hh = 0;
        int mm = 0;

        try {

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date oldDate = dateFormat.parse(date);
            Date cDate = new Date();
            assert oldDate != null;
            long timeDiff = cDate.getTime() - oldDate.getTime();
            day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
            hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
            mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));


        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mm <= 60 && hh != 0) {
            if (hh <= 60 && day != 0) {
                if (day == 1)
                    return day + " DAY AGO";
                return day + " DAYS AGO";
            } else {
                if (hh == 1)
                    return hh + " HOUR AGO";
                return hh + " HOURS AGO";
            }
        } else {
            if (mm == 1)
                return mm + " MINUTE AGO";
            return mm + " MINUTES AGO";
        }

    }

    public static class BackgroundProcessing extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

}
