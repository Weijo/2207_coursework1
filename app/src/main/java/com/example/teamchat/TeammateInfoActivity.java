package com.example.teamchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TeammateInfoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = TeammateInfoActivity.class.getSimpleName();
    private TextView teammateName;
    private TextView teammateAge;
    private TextView teammateHobby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teammate_info);

        teammateName = findViewById(R.id.teammateName);
        teammateAge = findViewById(R.id.teammateAge);
        teammateHobby = findViewById(R.id.teammateHobby);

        String name = getIntent().getStringExtra("teammate_name");
        teammateName.setText(name);

        String age = getIntent().getStringExtra("teammate_age");
        String hobby = getIntent().getStringExtra("teammate_hobby");
        teammateAge.setText(age);
        teammateHobby.setText(hobby);

        String member1Age = getResources().getString(R.string.member1_age);
        String member1Hobby = getResources().getString(R.string.member1_hobby);
        String member2Age = getResources().getString(R.string.member2_age);
        String member2Hobby = getResources().getString(R.string.member2_hobby);
        String member3Age = getResources().getString(R.string.member3_age);
        String member3Hobby = getResources().getString(R.string.member3_hobby);
        String member4Age = getResources().getString(R.string.member4_age);
        String member4Hobby = getResources().getString(R.string.member4_hobby);
        String member5Age = getResources().getString(R.string.member5_age);
        String member5Hobby = getResources().getString(R.string.member5_hobby);
        String member6Age = getResources().getString(R.string.member6_age);
        String member6Hobby = getResources().getString(R.string.member6_hobby);

        switch (name) {
            case "Member 1":
                teammateAge.setText(member1Age);
                teammateHobby.setText(member1Hobby);
                break;
            case "Member 2":
                teammateAge.setText(member2Age);
                teammateHobby.setText(member2Hobby);
                break;
            case "Member 3":
                teammateAge.setText(member3Age);
                teammateHobby.setText(member3Hobby);
                break;
            case "Member 4":
                teammateAge.setText(member4Age);
                teammateHobby.setText(member4Hobby);
                break;
            case "Member 5":
                teammateAge.setText(member5Age);
                teammateHobby.setText(member5Hobby);
                break;
            case "Member 6":
                teammateAge.setText(member6Age);
                teammateHobby.setText(member6Hobby);
                break;
            default:
                Log.e(TAG, "No teammate information found");
                break;
        }

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
    }

    public static void bindButton(Context context, int id, final String name, final String age, final String hobby) {
        Button button = ((MainActivity) context).findViewById(id);
        button.setOnClickListener(v -> {
            Log.v(TAG, "Button clicked for " + name);
            Intent intent = new Intent(v.getContext(), TeammateInfoActivity.class);
            intent.putExtra("teammate_name", name);
            intent.putExtra("teammate_age", age);
            intent.putExtra("teammate_hobby", hobby);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backButton) {
            finish();
        }
    }
}