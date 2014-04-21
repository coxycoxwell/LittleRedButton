package tr.edu.boun.swe599.littleredbutton.photographer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class PhotoHandler implements PictureCallback {
	private final Context context;
	private String pictureFileName;

	private Camera camera;

	public PhotoHandler(Context context) {
		this.context = context;

		File pictureFileDir = getDir();
		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
			Toast.makeText(context, "Can't create directory to save image.",
					Toast.LENGTH_LONG).show();
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		String date = dateFormat.format(new Date());
		String photoFile = "Picture_" + date + ".jpg";

		pictureFileName = pictureFileDir.getPath() + File.separator + photoFile;
	}
	
	public String getPictureFileName()
	{
		return this.pictureFileName;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		File pictureFile = new File(pictureFileName);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Toast.makeText(context, "New Image saved", Toast.LENGTH_LONG)
					.show();
		} catch (Exception error) {
			Toast.makeText(context, "Image could not be saved.",
					Toast.LENGTH_LONG).show();
		}
	}

	private File getDir() {
		File sdDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "LittleRedButton");
	}
}