package zazxxx.mapad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String locName = intent.getStringExtra("locName");
        String locAdd = intent.getStringExtra("locAdd");

        TextView name = new TextView(this);
        name.setText(locName);

        TextView addr = new TextView(this);
        addr.setText(locAdd);

        ViewGroup layout = (ViewGroup) findViewById(R.id.txtDetail);
        layout.addView(name);
        layout.addView(addr);
    }
}
