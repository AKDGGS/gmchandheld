package gov.alaska.gmchandheld;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import java.util.concurrent.ExecutionException;

public class UpdateAppActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_app);

		Context context = this;
		new UpdateCheckLastModifiedDate(context).execute();
//		try {
//			new UpdateCheckLastModifiedDate(context).execute();
////			new UpdateCheckLastModifiedDate(context).execute().get();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

//		UpdateDialog exampleDialog = new UpdateDialog();
//		exampleDialog.show(getSupportFragmentManager(), "update dialog");
	}
}