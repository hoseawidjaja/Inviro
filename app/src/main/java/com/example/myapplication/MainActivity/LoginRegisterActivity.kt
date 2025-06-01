package com.example.myapplication.MainActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.Misc.ViewPagerAdapter
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class LoginRegisterActivity : AppCompatActivity() {
    private var tabLayout: TabLayout? = null
    private var viewPager2: ViewPager2? = null
    private var adapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_login_register)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager2 = findViewById(R.id.view_pager)

        tabLayout?.addTab(tabLayout!!.newTab().setText("Login"))
        tabLayout?.addTab(tabLayout!!.newTab().setText("Signup"))

        val fragmentManager = supportFragmentManager
        adapter = ViewPagerAdapter(fragmentManager, lifecycle)
        viewPager2?.adapter = adapter

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager2?.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewPager2?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout?.getTabAt(position)?.select()
            }
        })
    }
}
