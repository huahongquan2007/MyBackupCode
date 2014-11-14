package robotbase.abilities.gallery;

import java.util.ArrayList;

import robotbase.vision.FakeNLP;
import robotbase.vision.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class FullScreenImageAdapter extends PagerAdapter {
	private Activity _activity;
	private ArrayList<String> _imagePaths;
	private LayoutInflater inflater;
	
	private FakeNLP fakeNLP;
	// constructor
	public FullScreenImageAdapter(Activity activity,
			ArrayList<String> imagePaths) {
		this._activity = activity;
		this._imagePaths = imagePaths;
	}

	@Override
	public int getCount() {
		return this._imagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		TouchImageView imgDisplay;
		Button btnClose;

		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.activity_gallery_full_screen_image,
				container, false);

		imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
		btnClose = (Button) viewLayout.findViewById(R.id.btnClose);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position),
				options);
		imgDisplay.setImageBitmap(bitmap);

		// close button click event
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_activity.finish();
			}
		});

		((ViewPager) container).addView(viewLayout);

		
		
		// FAKE NLP
		fakeNLP = new FakeNLP(_activity);
		Button btnShareFB = (Button) viewLayout.findViewById(R.id.btnShareFB);
		btnShareFB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("share this photo on facebook");
            }
        });
		Button btnShareTW = (Button) viewLayout.findViewById(R.id.btnShareTW);
		btnShareTW.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("share this photo on twitter");
            }
        });
		
		Button btnPrevious = (Button) viewLayout.findViewById(R.id.btnPrevious);
		btnPrevious.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("previous photo");
            }
        });
		Button btnNext = (Button) viewLayout.findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("next photo");
            }
        });
		
		
		return viewLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);

	}
}
