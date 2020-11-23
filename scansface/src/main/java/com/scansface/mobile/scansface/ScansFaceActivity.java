package com.scansface.mobile.scansface;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ScansFaceActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imgBack;
    TextView tvTitle;
    private String imgFilePath;
    private String refreshToken;
    private String systemCode;
    private String mode=FACE_ADD_MODE;
    public static final String FACE_ADD_MODE="add";
    public static final String FACE_SCANS_MODE="scans";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_scans);
        imgFilePath=getIntent().getStringExtra("imgPath");
        refreshToken=getIntent().getStringExtra("refreshToken");
        systemCode=getIntent().getStringExtra("systemCode");
        if (getIntent().getStringExtra("mode")!=null){
            mode=getIntent().getStringExtra("mode");
        }
        imgBack=findViewById(R.id.img_back);
        imgBack.setOnClickListener(this::onClick);
        tvTitle=findViewById(R.id.tv_title);

        ScansFragment scansFragment=new ScansFragment();
        Bundle bundle=new Bundle();
        bundle.putString("imgPath",imgFilePath);
        bundle.putString("mode",mode);
        scansFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fg_content,scansFragment).addToBackStack("scans").commit();

    }

    @Override
    public void onClick(View v) {
        getSupportFragmentManager().popBackStack();
        finish();
    }
    public void jumpResult(boolean isSuccess){
        ScansResultFragment scansResultFragment=new ScansResultFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fg_content,scansResultFragment).addToBackStack("result").commit();

    }
}