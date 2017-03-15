package lisboa.joao.project_add;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //remove dropbox token to test
        SharedPreferences preferences = getSharedPreferences("services", MODE_PRIVATE);
        preferences.edit().remove("dropbox_access_token").apply();

    }

    public void goToAccounts(View view){
        startActivity(new Intent(this, ServiceChooserActivity.class));
    }
}
