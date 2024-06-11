package checkin.zxing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Guilherme on 07-Mar-16.
 */
public class SiteSelect extends ListActivity  {

    private ProgressDialog pDialog2;
    JSONParser jParser = new JSONParser();
    private static String url_get_sites,logout_done, no_sites_found,login_success_admin;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private String username,user_id, password, bid, bid_desc, user_level;
    private Button logout;
    private String url;

    // products JSONArray
    JSONArray sites = null;
    ArrayList<HashMap<String, String>> siteList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url_get_sites = getResources().getString(R.string.get_sites_php);
        no_sites_found = getResources().getString(R.string.no_sites_found);
        logout_done = getResources().getString(R.string.logout_done);
        login_success_admin = getResources().getString(R.string.login_success_admin);

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user_level = extras.getString("Level");
            username = extras.getString("User");
            user_id = extras.getString("user_id");
            password = extras.getString("Pass");
            bid = extras.getString("building");
            bid_desc = extras.getString("building_desc");
            Toast.makeText(getApplicationContext(), extras.getString("message"), Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.select_tour);

        logout = (Button) findViewById(R.id.button_logout);
        logout.setTypeface(font);
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SiteSelect.this);
            builder.setMessage(R.string.logout_confirmation)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.putExtra("message", logout_done);
                        startActivity(intent);
                        finish();
                    }
                });
            builder.show();
            }
        });

        // Loading products in Background Thread
        siteList = new ArrayList<HashMap<String, String>>();
        new LoadSites().execute();
        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                if (!isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_login, Toast.LENGTH_LONG).show();
                }else {
                    Log.d("Create Response", "login sucesso Admin");
                    // Launch login activity
                    bid = ((TextView) view.findViewById(R.id.site_id)).getText().toString();
                    bid_desc = ((TextView) view.findViewById(R.id.name)).getText().toString();
                    Intent intent = new Intent(getApplicationContext(), TagViewer.class);
                    intent.putExtra("Level", user_level);
                    intent.putExtra("User", username);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("Pass", password);
                    intent.putExtra("building",bid);
                    intent.putExtra("building_desc", bid_desc);
                    intent.putExtra("message", "" + login_success_admin);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed (){
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(),Login.class);
        // Closing all previous activities
        i.putExtra("message",R.string.logout_done);
        startActivity(i);
        finish();
    }

    class LoadSites extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog2 = new ProgressDialog(SiteSelect.this);
            pDialog2.setMessage("Loading...");
            pDialog2.setIndeterminate(false);
            pDialog2.setCancelable(false);
            pDialog2.show();
        }


        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", user_id));
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_get_sites, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All sites: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    sites = json.getJSONArray("sites");

                    // looping through All Products
                    for (int i = 0; i < sites.length(); i++) {
                        JSONObject c = sites.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("site_id", c.getString("site_id"));
                        map.put("name", c.getString("name"));
                        siteList.add(map);
                    }

                } else {
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),Login.class);
                    // Closing all previous activities
                    i.putExtra("message",no_sites_found);
                    startActivity(i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

            pDialog2.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(SiteSelect.this, siteList,R.layout.list_item_login, new String[] { "site_id","name"},new int[] { R.id.site_id, R.id.name });
                    setListAdapter(adapter);
                }
            });
            Log.d("All sites: ", "sai de post exec");
        }
    }

}
