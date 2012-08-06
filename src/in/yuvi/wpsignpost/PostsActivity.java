package in.yuvi.wpsignpost;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import in.yuvi.wpsignpost.api.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;


public class PostsActivity extends Activity {

	private SignpostAPI api;
	private Issue issue = null;

	private GridView grid;
	
	private class FetchIssuesTask extends AsyncTask<Issue, Object, Issue> {
		@Override
		protected Issue doInBackground(Issue... params) {
			try {
				if(params.length > 0 && params[0] != null) {
					issue = params[0];
				} else {
					issue = api.getLatestIssue();
				}
				issue.fetchPosts(api);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return issue;
		}

		@Override
		protected void onPostExecute(Issue result) {
			super.onPostExecute(result);
			showIssue(result);
		}
				
	}
	
	private void showIssue(Issue issue) {
		grid.setAdapter(new PostsAdaptor(this, issue));
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Post p = (Post) parent.getItemAtPosition(position);
				Intent i = new Intent(parent.getContext(),
						PostActivity.class);
				i.putExtra("post", p);
				startActivity(i);
			}
		});
	}
	
	private class PostsAdaptor extends BaseAdapter {
		private Context context;
		private Issue issue;
		public PostsAdaptor(Context c, Issue issue) {
			this.context = c;
			this.issue = issue;
		}
		
		@Override
		public int getCount() {
			return issue.posts.size();
		}

		@Override
		public Object getItem(int position) {
			return issue.posts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		private int getRandomGrey() {
			Random r = new Random();
			int val = r.nextInt(128);
			return Color.rgb(val, val, val);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Post p = issue.posts.get(position);
			TextView tv;
			if(convertView == null) {
				tv = new TextView(context);
				tv.setGravity(Gravity.BOTTOM);
				tv.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, 256));
				tv.setTextSize(20);
				tv.setTypeface(Typeface.SERIF);
				tv.setPadding(8, 8, 8, 8);
				tv.setTextColor(0xFFFFFFFF);
				tv.setBackgroundColor(getRandomGrey());
			} else {
				tv = (TextView) convertView;
			}

			tv.setText(p.title);
			return tv;
		}
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = ((SignpostApp)getApplicationContext()).signpostAPI;
        grid = (GridView) findViewById(R.id.articles_grid);
        
        Intent intent = getIntent();
        if(intent.hasExtra("issue")) {
        	issue = (Issue)intent.getSerializableExtra("issue");
        }
        
        FetchIssuesTask t = new FetchIssuesTask();
        t.execute(issue);  
    }
    


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_archives:
				Intent i = new Intent(this, IssueListActivity.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}



	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		issue = (Issue)savedInstanceState.getSerializable("issue");	
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("issue", issue);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
