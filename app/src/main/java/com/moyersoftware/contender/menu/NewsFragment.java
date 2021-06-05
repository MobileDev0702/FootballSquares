package com.moyersoftware.contender.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.adapter.FriendsAdapter;
import com.moyersoftware.contender.menu.adapter.NewsAdapter;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.util.Util;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsFragment extends Fragment {

    // Constants
    private static final int REQUEST_INVITE = 0;

    // Views
    @BindView(R.id.newsRecycle)
    RecyclerView mFriendsRecycler;

    // Usual variables
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;

    private ArrayList<String> arrImage = new ArrayList<>();
    private ArrayList<String> arrTitle = new ArrayList<>();
    private ArrayList<String> arrDate = new ArrayList<>();
    private ArrayList<String> arrAuthor = new ArrayList<>();
    private ArrayList<String> arrDesc = new ArrayList<>();
    private ArrayList<String> arrUrl = new ArrayList<>();

    public NewsFragment() {
        // Required empty public constructor
    }

    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, view);

        initAdmobBanner(view);
        setNewsRecyclerView(view);
        loadNews();
        return view;
    }

    void initAdmobBanner(View view) {
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull @NotNull InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = view.findViewById(R.id.av_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setNewsRecyclerView(View view) {
        newsRecyclerView = view.findViewById(R.id.newsRecycle);
        newsAdapter = new NewsAdapter(getContext(), arrImage, arrTitle, arrDate, arrAuthor, arrDesc, arrUrl);
        newsRecyclerView.setAdapter(newsAdapter);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void loadNews() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "http://site.api.espn.com/apis/site/v2/sports/football/nfl/news";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.toString());
                            JSONArray articleArray = jsonObject.getJSONArray("articles");
                            for (int i = 0; i < articleArray.length(); i++) {
                                JSONObject child = articleArray.getJSONObject(i);
                                JSONArray images = child.getJSONArray("images");
                                JSONObject image = images.getJSONObject(0);
                                arrImage.add(image.getString("url"));
                                if (child.has("headline")) {
                                    arrTitle.add(child.getString("headline"));
                                } else {
                                    arrTitle.add("");
                                }

                                if (child.has("published")) {
                                    arrDate.add(child.getString("published"));
                                } else {
                                    arrDate.add("");
                                }

                                if (child.has("byline")) {
                                    arrAuthor.add(child.getString("byline"));
                                } else {
                                    arrAuthor.add("");
                                }

                                if (child.has("description")) {
                                    arrDesc.add(child.getString("description"));
                                } else {
                                    arrDesc.add("");
                                }

                                JSONObject links = child.getJSONObject("links");
                                if (links.has("web")) {
                                    JSONObject webUrl = links.getJSONObject("web");
                                    if (webUrl.has("href")) {
                                        arrUrl.add(webUrl.getString("href"));
                                    } else {
                                        arrUrl.add("");
                                    }
                                }

                                newsAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }
}
