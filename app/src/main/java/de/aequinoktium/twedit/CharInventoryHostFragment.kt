package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * A simple [Fragment] subclass.
 * Use the [CharInventoryHostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharInventoryHostFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var adapter: CharItemFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_inventory_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act = activity as MainActivity
        adapter = CharItemFragmentAdapter(act, c)

        val viewPager = view.findViewById<ViewPager2>(R.id.charinventory_viewpager)
        viewPager.adapter = adapter

        val tabLayout = view.findViewById<TabLayout>(R.id.charinventory_tab)

        TabLayoutMediator(tabLayout, viewPager) {tab, pos ->
            tab.text = (pos + 1).toString()
        }.attach()

    }

    class CharItemFragmentAdapter(
        fa: FragmentActivity,
        var c: CharacterViewModel
    ) : FragmentStateAdapter(fa) {
        var fragments: MutableList<Fragment> = initialFragments()

        fun initialFragments(): MutableList<Fragment> {
            var result = mutableListOf<Fragment>()
            result.add(CharInventoryFragment("0"))

            result.add(CharInventoryNewFragment("a"))


            return result
        }

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]


    }
}