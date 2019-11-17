package com.fuelspot;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.InboxAdapter;
import com.fuelspot.model.MessageItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.FragmentProfile.conversationIDs;
import static com.fuelspot.FragmentProfile.lastMessages;
import static com.fuelspot.FragmentProfile.openMessage;
import static com.fuelspot.FragmentProfile.userInbox;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.username;

public class MessagingActivity extends AppCompatActivity {

    static List<MessageItem> conversation = new ArrayList<>();
    PercentRelativeLayout writeMessageLayout;
    TextView textViewSessiecClosed;
    EditText messageHolder;
    ImageView sendButton;
    String messageText;
    private Window window;
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private RequestQueue requestQueue;
    private int conversationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        conversationID = getIntent().getIntExtra("CONVERSATION_ID", 0);
        String topic = getIntent().getStringExtra("TOPIC");

        window = this.getWindow();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(topic);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        //Comments
        requestQueue = Volley.newRequestQueue(MessagingActivity.this);
        mRecyclerView = findViewById(R.id.messageView);
        writeMessageLayout = findViewById(R.id.writeMessageLayout);
        textViewSessiecClosed = findViewById(R.id.sessionClosed);

        messageHolder = findViewById(R.id.editText2);
        messageHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    messageText = s.toString();
                }
            }
        });

        sendButton = findViewById(R.id.imageView3);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageText != null && messageText.length() > 0) {
                    sendMessage();
                } else {
                    Toast.makeText(MessagingActivity.this, getString(R.string.enter_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        conversation.clear();

        for (int i = userInbox.size() - 1; i >= 0; i--) {
            if (userInbox.get(i).getConversationID() == conversationID) {
                conversation.add(userInbox.get(i));
            }
        }

        RecyclerView.Adapter mAdapter = new InboxAdapter(MessagingActivity.this, conversation, "MESSENGER");
        GridLayoutManager mLayoutManager = new GridLayoutManager(MessagingActivity.this, 1);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (conversation.get(0).getIsOpen() == 1) {
            writeMessageLayout.setVisibility(View.VISIBLE);
            textViewSessiecClosed.setVisibility(View.GONE);
        } else {
            writeMessageLayout.setVisibility(View.GONE);
            textViewSessiecClosed.setVisibility(View.VISIBLE);
        }
    }

    // Depends on user, it changes with user comments or station comments
    private void sendMessage() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(MessagingActivity.this, getString(R.string.sending_message), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SEND_MESSAGE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        if (s != null && s.length() > 0) {
                            if (s.equals("Success")) {
                                messageHolder.setText("");
                                fetchInbox();
                            } else {
                                Toast.makeText(MessagingActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MessagingActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(MessagingActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("senderPhoto", photo);
                params.put("conversationID", String.valueOf(conversationID));
                params.put("receiver", "fuelspot");
                params.put("receiverPhoto", "https://fuelspot.com.tr/default_icons/fuelspot.png");
                params.put("topic", conversation.get(0).getTopic());
                params.put("message", messageText);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchInbox() {
        openMessage = 0;
        userInbox.clear();
        lastMessages.clear();
        conversationIDs.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_INBOX) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    System.out.println(obj);

                                    MessageItem item = new MessageItem();
                                    item.setID(obj.getInt("id"));
                                    item.setConversationID(obj.getInt("conversationID"));
                                    item.setSender(obj.getString("sender"));
                                    item.setSenderPhoto(obj.getString("senderPhoto"));
                                    item.setReceiver(obj.getString("receiver"));
                                    item.setReceiverPhoto(obj.getString("receiverPhoto"));
                                    item.setTopic(obj.getString("topic"));
                                    item.setMessage(obj.getString("message"));
                                    item.setIsOpen(obj.getInt("isOpen"));
                                    item.setTime(obj.getString("time"));
                                    userInbox.add(item);

                                    if (conversationIDs != null && !conversationIDs.contains(item.getConversationID())) {
                                        conversationIDs.add(item.getConversationID());
                                        lastMessages.add(item);
                                        if (item.getIsOpen() == 1) {
                                            openMessage++;
                                        }
                                    }
                                }

                                loadMessages();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color1);
            toolbar.setBackgroundColor(color2);
        } else {
            toolbar.setBackgroundColor(color2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
