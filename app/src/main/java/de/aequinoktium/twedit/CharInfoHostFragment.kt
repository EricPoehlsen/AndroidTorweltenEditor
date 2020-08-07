package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
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
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var adapter: CharInfoFragmentAdapter

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
        val act = activity as MainActivity
        adapter = CharInfoFragmentAdapter(act, c)

        val viewPager = view.findViewById<ViewPager2>(R.id.charinfo_viewpager)
        viewPager.adapter = adapter

        val tabLayout = view.findViewById<TabLayout>(R.id.charinfo_tab)

        TabLayoutMediator(tabLayout, viewPager) {tab, pos ->
            var names = mutableListOf<String>(
                getString(R.string.ci_core_title),
                getString(R.string.ci_desc_title)
            )

            var icons = mutableListOf<Int>(
                R.drawable.pips_3_0,
                R.drawable.pips_3_0
            )
            for (n in c.info.keys) {
                if (n !in arrayOf("core", "desc")) {
                    names.add(n)
                    icons.add(R.drawable.pips_3_0)
                }
            }

            names.add(getString(R.string.ci_new))
            icons.add(R.drawable.plus)

            tab.setCustomView(R.layout.tab_info)
            tab.text = names[pos]
            tab.setIcon(icons[pos])



        }.attach()

    }

    class CharInfoFragmentAdapter(
        fa: FragmentActivity,
        var c: CharacterViewModel
    ) : FragmentStateAdapter(fa) {
        var fragments: MutableList<Fragment> = initialFragments()

        fun initialFragments(): MutableList<Fragment> {
            var result = mutableListOf<Fragment>()
            var datasets = arrayOf<String>()
            for (key in c.info.keys) {
                if (key !in arrayOf("core", "desc")) datasets += key
            }

            result.add(CharInfoFragment("core"))
            result.add(CharInfoFragment("desc"))

            for (name in datasets) {
                result.add(CharInfoFragment(name))
            }

            result.add(CharInfoNewFragment(this))

            return result
        }

        fun addFragment(f: Fragment) {
            val pos = fragments.size-1
            fragments.add(pos, f)
            notifyItemInserted(pos)
        }

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]


    }
}