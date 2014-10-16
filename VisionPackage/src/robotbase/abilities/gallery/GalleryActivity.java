package robotbase.abilities.gallery;

import java.util.ArrayList;

import robotbase.vision.R;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.GridView;

// SOURCE: http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/

public class GalleryActivity extends Activity {
	private GalleryUtils utils;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private GridViewImageAdapter adapter;
	private GridView gridView;
	private int columnWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		gridView = (GridView) findViewById(R.id.grid_view);

		utils = new GalleryUtils(this);

		// Initilizing Grid View
		InitilizeGridLayout();

		// loading all image paths from SD card
		imagePaths = utils.getFilePaths();

		// Gridview adapter
		adapter = new GridViewImageAdapter(GalleryActivity.this, imagePaths,
				columnWidth);

		// setting grid view adapter
		gridView.setAdapter(adapter);
	}

	private void InitilizeGridLayout() {
		Resources r = getResources();
		float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				GalleryConfig.GRID_PADDING, r.getDisplayMetrics());

		columnWidth = (int) ((utils.getScreenWidth() - ((GalleryConfig.NUM_OF_COLUMNS + 1) * padding)) / GalleryConfig.NUM_OF_COLUMNS);

		gridView.setNumColumns(GalleryConfig.NUM_OF_COLUMNS);
		gridView.setColumnWidth(columnWidth);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setPadding((int) padding, (int) padding, (int) padding,
				(int) padding);
		gridView.setHorizontalSpacing((int) padding);
		gridView.setVerticalSpacing((int) padding);
	}

}
