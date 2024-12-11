package com.example.sensorslogin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sensorslogin.databinding.ActivityLoginSuccessBinding;
import com.example.sensorslogin.databinding.ActivityMainBinding;






public class LoginSuccessActivity extends AppCompatActivity {

    private ActivityLoginSuccessBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}