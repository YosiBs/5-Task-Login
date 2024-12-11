package com.example.sensorslogin;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.res.Configuration;
import android.database.ContentObserver;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.sensorslogin.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    //task 1
    private int batteryPercentage = -1; // To hold the current battery percentage
    private BroadcastReceiver batteryReceiver;

    //task 2
    //task 3
    private AudioManager audioManager;
    private VolumeObserver volumeObserver;
    private boolean volumeMaxReached = false;
    private boolean volumeMinReached = false;
    //task 4


    //task 5
    private SensorManager sensorManager1;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;

    private static final float SHAKE_THRESHOLD = 15.0f; // Threshold for shake detection
    private static final int SHAKE_COUNT_REQUIRED = 5;  // Number of shakes to pass task
    private long lastShakeTime = 0;
    private int shakeCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initTask1();
        initTask2();
        initTask3();
        initTask4();
        initTask5();
        initLoginButton();


    }

    private void initLoginButton() {
        binding.loginButton.setOnClickListener(view -> {
            if (checkTasksCompleted()) {
                Toast.makeText(MainActivity.this, "All tasks complete! Login successful!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, LoginSuccessActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please complete all tasks before logging in.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkTasksCompleted() {
        // Check if all 5 circles are green
        Drawable greenDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, null);

        return compareDrawable(binding.circle1.getBackground(), greenDrawable) &&
                compareDrawable(binding.circle2.getBackground(), greenDrawable) &&
                compareDrawable(binding.circle3.getBackground(), greenDrawable) &&
                compareDrawable(binding.circle4.getBackground(), greenDrawable) &&
                compareDrawable(binding.circle5.getBackground(), greenDrawable);
    }

    private boolean compareDrawable(Drawable drawable1, Drawable drawable2) {
        if (drawable1 instanceof ColorDrawable && drawable2 instanceof ColorDrawable) {
            return ((ColorDrawable) drawable1).getColor() == ((ColorDrawable) drawable2).getColor();
        }
        return drawable1.getConstantState() != null && drawable1.getConstantState().equals(drawable2.getConstantState());
    }

    private void initTask1(){
        setupBatteryReceiver();

        // Add a text change listener to check input dynamically
        binding.editTextBatteryPercentage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkBatteryPercentageMatch();
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });
    }

    private void initTask2() {
        checkOrientation();
    }

    private void initTask3() {
        // Initialize AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Set up volume observer
        volumeObserver = new VolumeObserver(new Handler());
        getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, volumeObserver
        );
    }

    private void initTask4() {
        // Check brightness when the user clicks the Task 4 circle
        binding.circle4.setOnClickListener(v -> checkScreenBrightness());
    }

    private void checkScreenBrightness() {
        try {
            // Get the current screen brightness (range: 0-255)
            ContentResolver contentResolver = getContentResolver();
            int currentBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);

            // Check if brightness is at maximum (255)
            if (currentBrightness == 255) {
                binding.circle4.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, null));
                Toast.makeText(this, "Task 4 Completed: Brightness is at maximum!", Toast.LENGTH_SHORT).show();
            } else {
                binding.circle4.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_red, null));
                Toast.makeText(this, "Please set brightness to maximum.", Toast.LENGTH_SHORT).show();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to access screen brightness settings.", Toast.LENGTH_SHORT).show();
        }
    }
    private void initTask5(){
        // Initialize SensorManager and Accelerometer
        sensorManager1 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // Calculate acceleration magnitude
                double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

                if (acceleration > SHAKE_THRESHOLD) {
                    long currentTime = System.currentTimeMillis();

                    // Avoid counting shakes too close together
                    if ((currentTime - lastShakeTime) > 500) {
                        lastShakeTime = currentTime;
                        shakeCount++;

                        // Check if required shakes are met
                        if (shakeCount >= SHAKE_COUNT_REQUIRED) {
                            onTask5Completed();
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    private void checkOrientation() {
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape mode
            binding.circle2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, null));
            Toast.makeText(this, "Landscape Mode", Toast.LENGTH_SHORT).show();

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait mode
            binding.circle2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_red, null));
            Toast.makeText(this, "Portrait Mode", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBatteryReceiver() {
        // BroadcastReceiver to monitor battery changes
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPercentage = (int) ((level / (float) scale) * 100);
                checkBatteryPercentageMatch();
            }
        };
    }

    private void checkBatteryPercentageMatch() {
        String input = binding.editTextBatteryPercentage.getText().toString();
        if (!input.isEmpty()) {
            int inputPercentage = Integer.parseInt(input);
            if (inputPercentage == batteryPercentage) {
                // Match: Turn the circle green
                binding.circle1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, null));
            } else {
                // Mismatch: Turn the circle red
                binding.circle1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_red, null));

            }
        } else {
            // Empty input: Turn the circle red
            binding.circle1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_red, null));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //task 1
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //task 4

        //task 5
        sensorManager1.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //task 1
        unregisterReceiver(batteryReceiver);
        //task 4

        //task 5
        sensorManager1.unregisterListener(shakeListener);
    }

    private class VolumeObserver extends ContentObserver {

        public VolumeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // Get current volume level and max volume
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            if (currentVolume == maxVolume) {
                // Volume max reached
                volumeMaxReached = true;
            }

            if (currentVolume == 0 && volumeMaxReached) {
                // Volume min reached AFTER reaching max
                volumeMinReached = true;
                onTask3Completed();
            }
        }
    }

    private void onTask3Completed() {
        if (volumeMaxReached && volumeMinReached) {
            // Task completed: Turn the Task 3 circle green
            binding.circle3.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, null));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(volumeObserver);
    }

    private void onTask5Completed() {
        // Task 5 passes: Turn the Task 5 circle green
        binding.circle5.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, null));
        sensorManager1.unregisterListener(shakeListener); // Stop listening for shakes
    }

}