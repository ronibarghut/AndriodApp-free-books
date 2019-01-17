package android.course.books_312316433_313601130.Fragments.DiscoverFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.course.books_312316433_313601130.Activities.ChaptersListActivity;
import android.course.books_312316433_313601130.Activities.NewStoryActivity;
import android.course.books_312316433_313601130.Adapters.GridSpacingItemDecoration;
import android.course.books_312316433_313601130.Adapters.StoriesAdapter;
import android.course.books_312316433_313601130.DatabaseHandkers.DataHandler;
import android.course.books_312316433_313601130.Fragments.Listeners.Refreshable;
import android.course.books_312316433_313601130.Fragments.TabbedFragment;
import android.course.books_312316433_313601130.Network.NetworkConnector;
import android.course.books_312316433_313601130.Network.NetworkResListener;
import android.course.books_312316433_313601130.Network.ResStatus;
import android.course.books_312316433_313601130.Objects.Story;
import android.course.books_312316433_313601130.Objects.User;
import android.course.books_312316433_313601130.R;
import android.course.books_312316433_313601130.Utils.Constants;
import android.course.books_312316433_313601130.Fragments.Listeners.DataLoadingListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class StoriesFragment extends Fragment implements View.OnClickListener, NetworkResListener, Refreshable {
    /**
     * fragment type tags
     */
    public final static String NEWEST_TAG = "newest";
    public final static String RECOMMENDED_TAG = "recommended";
    public static final String MY_STORIES_TAG = "myStories";
    public static final String DELETED_TAG = "deleted";
    public static final String PROFILE_STORIES = "profile";
    public static final String CURRENT_READ = "reading";
    public static final String FAVOURITES = "favourite";

    private static final String TAG = "TAG";
    private static final int ADDSTORY = 1;

    private String tag;
    private StoriesAdapter adapter;
    private List<Story> stories;
    private DataLoadingListener listener;
    private String username;

    public StoriesFragment(){}
    /**
     * factory method to initialize fragments
     * @param tag fragment type tag
     * @param user
     * @return a new instance of StoriesFragment
     */
    public static StoriesFragment newInstance(@NonNull String tag, String user){
        StoriesFragment fragment = new StoriesFragment();
        Bundle args = new Bundle();
        args.putString(TAG, tag);
        args.putString(Constants.USERNAME, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tag = getArguments().getString(TAG);
            username = getArguments().getString(Constants.USERNAME);
            // initialize the list dependant on fragment type
            if (tag.equals(RECOMMENDED_TAG)) {
                NetworkConnector.getInstance().update(this, NetworkConnector.GET_ALL_ITEMS_JSON_REQ);
            } else if (tag.equals(NEWEST_TAG)) {
                NetworkConnector.getInstance().update(this, NetworkConnector.GET_NEWEST);
            } else if (tag.equals(MY_STORIES_TAG))
                NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_ITEM_USER_REQ,
                        new User(username), this);
            else if (tag.equals(PROFILE_STORIES))
                NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_ITEM_USER_REQ,
                    new User(username), this);
            else if (tag.equals(FAVOURITES))
                NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_FAVE_ITEM_USER_REQ,
                        new User(username), this);
            else if (tag.equals(CURRENT_READ))
                NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_CUURENT_READING,
                        new User(username), this);
        }
        stories = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stories, container, false);

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.include);
        // set the adapter and and the recycler layout type
        adapter = new StoriesAdapter(getContext(), stories, tag.equals(MY_STORIES_TAG));
        adapter.setDelete(tag.equals(DELETED_TAG));
        recyclerView.setAdapter(adapter);
        if (tag.equals(MY_STORIES_TAG))
            adapter.setListener((TabbedFragment)getParentFragment());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(8), true));

        FloatingActionButton fab = view.findViewById(R.id.fab);
        if (tag.equals(DELETED_TAG) || tag.equals(CURRENT_READ)
                || tag.equals(PROFILE_STORIES) || tag.equals(FAVOURITES))
            fab.hide();
        // add click listener for the FloatingActionButton that launches new story activity
        fab.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DataLoadingListener)context;
        }catch (Exception e){}
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(getContext(), NewStoryActivity.class);
        ((Activity)getContext()).startActivityForResult(intent, ADDSTORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle request in case of a chapter was added,  or a chapter was edited
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == ADDSTORY){
                String s = data.getStringExtra(Constants.STORY_TITLE);
                if (tag.equals(RECOMMENDED_TAG) || tag.equals(NEWEST_TAG)){
                    stories.add(0,DataHandler.getInstance().getStoryByTitle(s));
                    adapter.notifyItemInserted(0);
                }else if (tag.equals(MY_STORIES_TAG)){
                    stories.add(DataHandler.getInstance().getStoryByTitle(s));
                    adapter.notifyItemInserted(stories.size()-1);
                }
            }
            else if(requestCode == Constants.EDIT_STORY || requestCode == Constants.ADDCHAPTER){
                String s = data.getStringExtra(Constants.STORY_TITLE);
                int i = DataHandler.getInstance().replaceStory(stories, s);
                if (i >= 0)
                    adapter.notifyItemChanged(i);
            }
        }else if (resultCode == ChaptersListActivity.CHAPTER_ADDED) {

        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onPreUpdate() {
        if (listener != null)
            listener.onLoadingStart();
    }

    @Override
    public void onPostUpdate(JSONObject res, String table, ResStatus status) {
        // if there's data from the internet parse it and display it in the recyclerView
        // else load the data from the device
        if (status == ResStatus.SUCCESS) {
            DataHandler.getInstance().parseStories(adapter, stories, tag.equals(CURRENT_READ), tag.equals(FAVOURITES), res);
        }else {
            if (tag.equals(NEWEST_TAG))
                stories = DataHandler.getInstance().getNewsetStories();
            else if (tag.equals(RECOMMENDED_TAG))
                stories = DataHandler.getInstance().getStories();
            else if(tag.equals(CURRENT_READ))
                stories = DataHandler.getInstance().getCurrentReading(username);
            else if (tag.equals(MY_STORIES_TAG) || tag.equals(PROFILE_STORIES))
                stories = DataHandler.getInstance().getUserStories(username);
            else if(tag.equals(FAVOURITES))
                stories = DataHandler.getInstance().getFaveStories(username);

            adapter.setStories(stories);
        }
        if (listener != null)
            listener.onnLoadingDone();
    }

    @Override
    public void onPostUpdate(Bitmap res, ResStatus status) {

    }

    public void addToDeletedList(Story s){
        stories.add(s);
        adapter.notifyItemInserted(stories.size()-1);
    }

    public void addTofaves(String stringExtra) {
        Story s = DataHandler.getInstance().getStoryByTitle(stringExtra);
        stories.add(s);
        adapter.notifyItemInserted(stories.size()-1);
    }

    public void removeFromfaves(String stringExtra) {
        int index = stories.indexOf(new Story(stringExtra));
        stories.remove(index);
        adapter.notifyItemRemoved(index);
    }

    @Override
    public void refresh() {
        stories.clear();
        if (tag.equals(RECOMMENDED_TAG)) {
            NetworkConnector.getInstance().update(this, NetworkConnector.GET_ALL_ITEMS_JSON_REQ);
        } else if (tag.equals(NEWEST_TAG)) {
            NetworkConnector.getInstance().update(this, NetworkConnector.GET_NEWEST);
        } else if (tag.equals(MY_STORIES_TAG))
            NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_ITEM_USER_REQ,
                    new User(username), this);
        else if (tag.equals(PROFILE_STORIES))
            NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_ITEM_USER_REQ,
                    new User(username), this);
        else if (tag.equals(FAVOURITES))
            NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_FAVE_ITEM_USER_REQ,
                    new User(username), this);
        else if (tag.equals(CURRENT_READ))
            NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.GET_CUURENT_READING,
                    new User(username), this);
    }
}
