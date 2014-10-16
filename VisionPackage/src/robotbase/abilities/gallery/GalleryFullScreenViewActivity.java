package robotbase.abilities.gallery;

import java.util.ArrayList;

import robotbase.vision.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class GalleryFullScreenViewActivity extends Activity {
	private ArrayList<String> imagePaths = new ArrayList<String>();

	private GalleryUtils utils;

	private PagerAdapter mPagerAdapter;

	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gallery_full_screen_view);

		// get intent data

		Intent i = getIntent();

		utils = new GalleryUtils(this);

		// loading all image paths from SD card

		imagePaths = utils.getFilePaths();

		// Selected image id

		int position = i.getExtras().getInt("id");

		mPagerAdapter = new FullScreenImageAdapter(this, imagePaths);

		mPager = (ViewPager) findViewById(R.id.pager);

		mPager.setAdapter(mPagerAdapter);

		mPager.setCurrentItem(i.getExtras().getInt("position"));

		
		
		// ImageView imageView = (ImageView) findViewById(R.id.imgDisplay);

		// imageView.setImageResource(imageAdapter.mThumbIds[position]);

	}

}