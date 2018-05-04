package com.ai.listrelated.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ai.listrelated.imgchooser.ImgChooser;
import com.ai.listrelated.imgchooser.ImgChooserUtil;
import com.ai.listrelated.sample.defaultimpl.DefaultRefreshActivity;
import com.ai.listrelated.sample.simpleloadmore.SimpleLoadmoreActivity;
import com.ai.listrelated.sample.web.WebrowserActivity;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImgChooser chooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.app_name);
        chooser = new ImgChooser(this);
    }

    public void defaultImpl(View view) {
        startActivity(new Intent(this, DefaultRefreshActivity.class));
    }

    public void goWebrowser(View view) {
        startActivity(new Intent(this, WebrowserActivity.class));
    }

    /**
     * 简单的加载更多的实现
     */
    public void simpleLoadMore(View view) {
        startActivity(new Intent(this, SimpleLoadmoreActivity.class));
    }

    public void imageChoose(View view) {
        chooser.showChooseDialog(false);
    }

    public void imageChooseDocument(View view) {
        chooser.showChooseDialog(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        chooser.onActivityResult(requestCode, resultCode, data);
    }
}
