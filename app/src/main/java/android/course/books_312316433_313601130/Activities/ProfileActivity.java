package android.course.books_312316433_313601130.Activities;

import android.content.Intent;
import android.course.books_312316433_313601130.Adapters.ProfileFragmentsAdapter;
import android.course.books_312316433_313601130.DatabaseHandkers.DataHandler;
import android.course.books_312316433_313601130.Network.NetworkConnector;
import android.course.books_312316433_313601130.Network.NetworkResListener;
import android.course.books_312316433_313601130.Network.ResStatus;
import android.course.books_312316433_313601130.Objects.User;
import android.course.books_312316433_313601130.R;
import android.course.books_312316433_313601130.Utils.Constants;
import android.graphics.Bitmap;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity implements NetworkResListener {

    public static final int ADD_TO_FAVE = -11;
    public static final int REMOVE_FROM_FAVE = -22;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TextView name, follow;
    private ImageView imageView;
    private TabLayout tabLayout;
    private DataHandler dataHandler;
    private User user;
    private boolean isUser, isFollowing;
    private ProfileFragmentsAdapter mSectionsPagerAdapter;
    private String username;
    private TextView fs;
    private int fCount;
    private final int REQUESTEDITPROF = 30;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bindViews();

        dataHandler = DataHandler.getInstance();
        username = getIntent().getStringExtra(Constants.USERNAME);
        initUser();
        initViews();
        initCollapsingToolbar();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new ProfileFragmentsAdapter(this, getSupportFragmentManager(), username);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // set up tab layout according to viewpager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    private void initUser() {
        User u = dataHandler.getUser();

        if (u.equals(new User(username))) {
            user = u;
            isUser = true;
        }else {
            user = dataHandler.getUser(username);

        }
        NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_USER_FOLLOWING, user, this);
        NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_USER_FOLLOWERS, user, this);
    }

    private void bindViews() {
        name = (TextView) findViewById(R.id.txt_profileName);
        follow = (TextView) findViewById(R.id.followBtn);
        follow.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.backdrop);
         fs = (TextView)findViewById(R.id.followers_txt_prof);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isUser)
            getMenuInflater().inflate(R.menu.menu_user_items, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent prof = new Intent(this, EditProfileActivity.class);
            startActivityForResult(prof, REQUESTEDITPROF);
            return true;
        }
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(user.getFullName());
                    name.setVisibility(View.INVISIBLE);
                    if (!isUser)
                        follow.setVisibility(View.INVISIBLE);
                    isShow = true;

                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    name.setVisibility(View.VISIBLE);
                    if (!isUser)
                        follow.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });
    }

    /**
     * bind data to the views
     */
    private void initViews(){
        name.setText(user.getFullName());
        if(user.getThumb() != null){
            imageView.setImageBitmap(user.getThumb());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == REQUESTEDITPROF ) {
                initViews();
            }
        }else if (resultCode == ADD_TO_FAVE){
            mSectionsPagerAdapter.getFaveFrag().addTofaves(data.getStringExtra(Constants.STORY_TITLE));
        }else if (resultCode == REMOVE_FROM_FAVE){
            mSectionsPagerAdapter.getFaveFrag().removeFromfaves(data.getStringExtra(Constants.STORY_TITLE));
        }
    }

    public void followUser(View view) {

        if (!isFollowing) {
            if (dataHandler.followUser(username)) {
                fCount++;
                follow.setText(getString(R.string.unfollow));
                isFollowing = true;
            }
        }else {
            if (dataHandler.unFollowUser(username)){
                fCount--;
                follow.setText(getString(R.string.follow));
                isFollowing = false;
            }
        }
        fs.setText(String.format(getString(R.string.followers), fCount));
    }

    public void viewFollowers(View view) {
        Intent intent = new Intent(this, FollowActivity.class);
        intent.putExtra(Constants.USERNAME, username);
        startActivity(intent);
    }

    @Override
    public void onPreUpdate() {

    }

    @Override
    public void onPostUpdate(JSONObject res, String table, ResStatus status) {


        if (table.equals(NetworkConnector.GET_USER_FOLLOWERS)){

            int i = 0;
            if (status == ResStatus.SUCCESS){
                dataHandler.parseFollowersList(username, res);
            }
            i = dataHandler.getUserFollowers(username).size();
            fCount = i;
            if (!isUser) {
                follow.setVisibility(View.VISIBLE);
                if (dataHandler.isFollowing(username)) {
                    follow.setText(getString(R.string.unfollow));
                    isFollowing = true;
                }
            }
            fs.setText(String.format(getString(R.string.followers), fCount));

        }else if (table.equals(NetworkConnector.GET_USER_FOLLOWING)){
            if (status == ResStatus.SUCCESS)
                dataHandler.parseFolloweingsist(username, res);
        }

    }

    @Override
    public void onPostUpdate(Bitmap res, ResStatus status) {

    }

}
