package pl.komunikator.komunikator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import pl.komunikator.komunikator.R;
import pl.komunikator.komunikator.RealmUtilities;
import pl.komunikator.komunikator.adapter.ChatAdapter;
import pl.komunikator.komunikator.entity.Conversation;
import pl.komunikator.komunikator.entity.Message;
import pl.komunikator.komunikator.entity.User;

public class ConversationActivity extends AppCompatActivity {

    public static final int BACK_PRESS_CODE = 1;
    private static final int SUCCESS_CODE = 124;
    private static final int CONVERSATION_ACTIVITY_ID = 234;
    private static int ERROR_CODE = 562;
    private EditText messageET;
    private ListView messagesContainer;
    private ChatAdapter adapter;
    private Realm realm;
    private Conversation mConversation;

    private Number loggedUserId = null;
    private Number conversationId = null;
    private Number lastMessageID = null;

    public static void show(Activity startActivity, long conversationId) {
        Intent intent = new Intent(startActivity, ConversationActivity.class);
        intent.putExtra("lastMessageID", conversationId);
        startActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        realm = Realm.getDefaultInstance();
        loggedUserId = User.getLoggedUser().getId();
        initConversation();
        initControls();


    }

    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        Button sendBtn = (Button) findViewById(R.id.chatSendButton);

        loadDummyHistory();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        String messageText = messageET.getText().toString().trim();
                        if (TextUtils.isEmpty(messageText)) {
                            return;
                        }
                        User user = realm.where(User.class).equalTo("id", loggedUserId.intValue()).findFirst();
                        Conversation con = realm.where(Conversation.class).equalTo("id", conversationId.intValue()).findFirst();
                        lastMessageID = realm.where(Message.class).max("id");

                        if (lastMessageID == null) {
                            lastMessageID = new Long(0);
                        }
                        lastMessageID = lastMessageID.longValue() + 1;
                        Message message = realm.createObject(Message.class, lastMessageID.longValue());
                        message.setContent(messageText);
                        message.setFromUser(user);
                        message.setCreateDate(new Date());
                        con.getMessages().add(message);
                        lastMessageID = message.getId();
                        displayMessage(message);
                    }
                });


                messageET.setText("");
            }
        });
    }

    public void displayMessage(Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory() {

        List<Message> chatHistory = realm.copyFromRealm(mConversation.getMessages());

        adapter = new ChatAdapter(getApplicationContext(), new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);
        for (Message message : chatHistory) {
            displayMessage(message);
        }
    }

    private void initConversation() {

        long id;
        if (conversationId != null && conversationId.longValue() < 0) {
            id = conversationId.longValue();
        } else id = getIntent().getLongExtra("lastMessageID", getIntent().getLongExtra("id", 0));
        RealmUtilities realm = new RealmUtilities();
        mConversation = realm.getConversation(id);
        conversationId = id;
        mConversation.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel element) {
                initControls();
            }
        });
        setTitle(mConversation.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_conversation_menu_item) {
            long conversationId = mConversation.getId();
            Intent intent = new Intent(this, CreateConversationActivity.class);
            intent.putExtra("editId", conversationId);
            startActivityForResult(intent, CONVERSATION_ACTIVITY_ID);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CONVERSATION_ACTIVITY_ID) {
            conversationId = data.getLongExtra("lastMessageID", 0);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ContainerActivity.class);
        startActivityForResult(intent, BACK_PRESS_CODE);
    }
}
