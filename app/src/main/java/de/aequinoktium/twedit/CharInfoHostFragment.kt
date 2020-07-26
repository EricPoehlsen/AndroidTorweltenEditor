package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * A simple [Fragment] subclass.
 * Use the [CharInfoHostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharInfoHostFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_info_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager2>(R.id.charinfo_viewpager)
        val tabLayout = view.findViewById<TabLayout>(R.id.charinfo_tab)
        TabLayoutMediator(tabLayout, viewPager) {tab, pos ->
            
        }

    }



    class CharInfoFragmentAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        val fragments: Array<Fragment> = arrayOf(

        )

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}