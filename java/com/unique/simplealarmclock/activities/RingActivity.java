package com.unique.simplealarmclock.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.unique.simplealarmclock.R;
import com.unique.simplealarmclock.databinding.ActivityRingBinding;
import com.unique.simplealarmclock.model.Alarm;
import com.unique.simplealarmclock.service.AlarmService;
import com.unique.simplealarmclock.viewmodel.AlarmListViewModel;

import java.util.Calendar;
import java.util.Random;

public class RingActivity extends AppCompatActivity {
    Alarm alarm;
    private AlarmListViewModel alarmsListViewModel;
    private ActivityRingBinding ringActivityViewBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ringActivityViewBinding= ActivityRingBinding.inflate(getLayoutInflater());
        setContentView(ringActivityViewBinding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
/*        KeyguardManager keyguardManager=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE) ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(this, null);
            }*/

        alarmsListViewModel = ViewModelProviders.of(this).get(AlarmListViewModel.class);
        Bundle bundle=getIntent().getBundleExtra(getString(R.string.bundle_alarm_obj));
        if (bundle!=null)
            alarm =(Alarm)bundle.getSerializable(getString(R.string.arg_alarm_obj));

        ringActivityViewBinding.activityRingDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInput();
            }
        });

        ringActivityViewBinding.activityRingConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndDismiss();
            }
        });


        animateClock();
    }
    private String getRandomSentence() {
        // List of example 10-word sentences
        String[] sentences = {
                "Good morning all of you boys welcome to reality o",
                "Wake up it's a brand new beautiful sunny morning today",
                "Every morning brings new opportunities and adventures ahead on you",
                "Hope today will be a better and exciting day for you",
                "Rise and shine it's time to make today amazing for you",
                "Good day ahead stay positive and make things happen o",
                "Let's start the day with a smile and positive energy",
                "A beautiful morning is waiting for you get ready o",
                "Today is full of possibilities make the most of it",
                "Good morning don't forget to chase your dreams today o"
        };

        // Randomly select a sentence
        Random random = new Random();
        int index = random.nextInt(sentences.length);

        return sentences[index];
    }

    private void showTextInput() {
        String sentence = getRandomSentence();

        // Show the random sentence above the input
        ringActivityViewBinding.activityRingLabel.setText(sentence);
        ringActivityViewBinding.activityRingLabel.setVisibility(View.VISIBLE);
        ringActivityViewBinding.activityRingEditText.setVisibility(View.VISIBLE);
        ringActivityViewBinding.activityRingConfirm.setVisibility(View.VISIBLE);
        ringActivityViewBinding.activityRingDismiss.setVisibility(View.GONE);
    }

    private void validateAndDismiss() {
        String input = ringActivityViewBinding.activityRingEditText.getText().toString().trim();
        String displayedSentence = ringActivityViewBinding.activityRingLabel.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter some text.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (input.split("\\s+").length < 10) {
            Toast.makeText(this, "Please type at least 10 words.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (input.equals(displayedSentence)) {
            dismissAlarm();
        } else {
            Toast.makeText(this, "The typed text does not match the sentence.", Toast.LENGTH_SHORT).show();
        }
    }


    private void animateClock() {
        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(ringActivityViewBinding.activityRingClock, "rotation", 0f, 30f, 0f, -30f, 0f);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setDuration(800);
        rotateAnimation.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false);
            setTurnScreenOn(false);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
    }
    private void clearInputFields() {
        ringActivityViewBinding.activityRingEditText.setText("");
        ringActivityViewBinding.activityRingLabel.setVisibility(View.GONE);
        ringActivityViewBinding.activityRingEditText.setVisibility(View.GONE);
        ringActivityViewBinding.activityRingConfirm.setVisibility(View.GONE);
        ringActivityViewBinding.activityRingDismiss.setVisibility(View.VISIBLE);
    }


    private void dismissAlarm(){
        if(alarm!=null) {
            alarm.setStarted(false);
            alarm.cancelAlarm(getBaseContext());
            alarmsListViewModel.update(alarm);
        }
        Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
        getApplicationContext().stopService(intentService);
        finish();
    }
}