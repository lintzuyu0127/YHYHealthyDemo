package com.example.yhyhealthy;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AppPage extends AppCompatActivity {

    private static final String TAG = AppPage.class.getSimpleName();

    private ProgressDialog progressDialog;

    @Override
    protected void onResume() {
        super.onResume();

        initialBackButton();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    protected void disableBackButton() {
        View view = findViewById(R.id.btnBack);

        if(view != null) view.setVisibility(View.INVISIBLE);
    }

    protected void initialBackButton() {
        View view = findViewById(R.id.btnBack);

        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(onBackListener);
        }
    }

    private View.OnClickListener onBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: " );
            onBackPressed();
        }
    };

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        TextView textTitle = findViewById(R.id.text_title);

        if(textTitle == null){
            return;
        }

        textTitle.setText(title);
    }

    protected void setActionButton(int resId, View.OnClickListener listener){
        TextView btnMore = findViewById(R.id.btnMore);
        if (btnMore != null){
            btnMore.setVisibility(View.VISIBLE);
//            btnMore.setBackgroundResource(resId);
            btnMore.setText(resId);
            btnMore.setOnClickListener(listener);
        }
    }

    protected void disableActionButton(){
        View btnMore = findViewById(R.id.btnMore);
        if (btnMore != null){
            btnMore.setVisibility(View.GONE);
        }
    }

    public void replaceFragment(int resId, Fragment f){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(resId, f);
        fragmentTransaction.commit();
    }

    // region  dialog
    public void buildProgress(@StringRes int titleId, @StringRes int messageId) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, getString(titleId), getString(messageId));
        }

        if (!progressDialog.isShowing()) progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) progressDialog.dismiss();
    }
}