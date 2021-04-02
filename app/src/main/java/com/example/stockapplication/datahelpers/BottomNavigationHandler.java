package com.example.stockapplication.datahelpers;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.stockapplication.R;
import com.example.stockapplication.fragments.ChartFragment;
import com.example.stockapplication.fragments.MainFragment;
import com.example.stockapplication.fragments.OptionsFragment;
import com.example.stockapplication.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class BottomNavigationHandler {
    final Context context;
    private BottomNavigationView bottomNavigationView;
    boolean selectedByCode = false;
    public BottomNavigationHandler(Context ctx) {
        this.context = ctx;

    }

    /**
     * Sets listener for BottomNavigation clicks
     * This method needs to be called, navigation won't work otherwise
     * @param viewId Id for BottomNavigation xml
     * @param navId Id of item to select on init
     */
    public void initNavigation(int viewId,int navId){
        if (!(context instanceof AppCompatActivity)) {
            return;
        }

        AppCompatActivity activity = (AppCompatActivity) context;
        Objects.requireNonNull(activity.getSupportActionBar()).hide();
        bottomNavigationView = activity.findViewById(viewId);
        // navId is -1 on init if user paused app on chart activity
        // and relaunched -> MainActivity will call initNavigation with id -1
        // to prevent navigationListener from redirecting to MainFragment
        if(navId !=-1){
            bottomNavigationView.setSelectedItemId(navId);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            // SelectedByCode prevents listener doing actions
            // This.setSelectedItem method utilizes this
            if(!selectedByCode){
                // Home icon will be selected in ChartFragment so clicking home there wouldn't work normally
                // because first statement will check if user is in current selected view
                // enable home redirecting by checking if ChartFragment is visible
                if(id == bottomNavigationView.getSelectedItemId()   && !(activity.getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) instanceof ChartFragment)) {
                    return true;
                }else if (id == R.id.home){
                    navigateToFragment(MainFragment.newInstance(),AppData.MAIN_FRAGMENT,activity);
                    return true;
                }else if(id ==  R.id.search){
                    navigateToFragment(SearchFragment.newInstance(),AppData.SEARCH_FRAGMENT,activity);
                    return true;
                }else if (id == R.id.settings ){
                    navigateToFragment(OptionsFragment.newInstance(),AppData.OPTIONS_FRAGMENT,activity);
                    return true;
                }
            }else{
                selectedByCode = false;
                return true;
            }


            return false;
        });

    }

    /**
     * Handle navigation to new fragment,
     * called from navigationSelectedListener
     * @param fragment Instance of fragment to navigate into
     * @param name Name of fragment to use in backstack
     * @param activity instance of AppCompatActivity
     */
    private void navigateToFragment(Fragment fragment,String name, AppCompatActivity activity){
        FragmentTransaction fragmentTransaction=activity.getSupportFragmentManager().beginTransaction();
        // fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragmentContainer,fragment);
        fragmentTransaction.addToBackStack(name);
        fragmentTransaction.commit();

    }

    /**
     * Sets navigation item calling navigationItemListener
     * @param navId Id of item to select
     */
    public void setSelectedItem(int navId){
        if(bottomNavigationView != null && bottomNavigationView.getSelectedItemId() != navId){
            selectedByCode = true;
            bottomNavigationView.setSelectedItemId(navId);
        }

    }


}
