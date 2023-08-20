package com.Shady_Selim.Quran

import java.util.Locale

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle

import android.os.Handler
import android.view.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import androidx.viewpager.widget.ViewPager
import android.widget.ImageView
import android.widget.TextView
import com.Shady_Selim.Quran.R.id.*

import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu

class MainActivity : AppCompatActivity() {

    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private var doubleBackToExitPressedOnce: Boolean = false
    private lateinit var info: TextView
    private val hezPart: Array<String> = arrayOf("1/4", "1/2", "3/4")
    private lateinit var mViewPager: ViewPager
    var pagesCount: Int = 0
    private lateinit var prefs: SharedPreferences
    private var setMark: MutableSet<String>? = null
    private lateinit var bookmarkImg: ImageView

    private val clickListener = View.OnClickListener {
        when (it.id) {
            action_surah -> fabFragment(SelectSurah(mViewPager, pagesCount))
            action_juz -> fabFragment(SelectJuz(mViewPager, pagesCount))
            action_hezb -> fabFragment(SelectHezb(mViewPager, pagesCount, hezPart))
            action_bookmark -> fabFragment(SelectBookmark(mViewPager, pagesCount, setMark))
        }
    }

    private fun fabFragment(selectFragment: androidx.fragment.app.DialogFragment) {
        selectFragment.show(supportFragmentManager, "what")
        mFab!!.close(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pagesCount = this.resources.getInteger(R.integer.pagesCount)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = findViewById(pager)
        mViewPager.adapter = mSectionsPagerAdapter
        prefs = applicationContext.getSharedPreferences("Quran", 0)
        if (prefs.getBoolean("PREFERENCE_FIRST_RUN", true)) {
            prefs.edit().clear().apply()
            prefs.edit().putBoolean("PREFERENCE_FIRST_RUN", false).apply()
        }
        mViewPager.currentItem = prefs.getInt("LastPage", pagesCount)
        setMark = prefs.getStringSet("Bookmarks", HashSet())
        fullScreen = prefs.getBoolean("FullScreen", false)
        invertColor = prefs.getBoolean("InvertColor", false)

        info =  findViewById(R.id.info)

        val fabSurah: FloatingActionButton = findViewById(action_surah)
        val fabHezb: FloatingActionButton = findViewById(action_hezb)
        val fabJuz: FloatingActionButton = findViewById(action_juz)
        val fabBookmark: FloatingActionButton = findViewById(action_bookmark)
        fabSurah.setOnClickListener(clickListener)
        fabHezb.setOnClickListener(clickListener)
        fabJuz.setOnClickListener(clickListener)
        fabBookmark.setOnClickListener(clickListener)
        bookmarkImg = findViewById(bookmark)
        bookmarkImg.visibility = View.GONE
        mFab = findViewById(fab)
        getBookmark()
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putInt("LastPage", mViewPager.currentItem).apply()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                mViewPager.currentItem = mViewPager.currentItem - 1
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                mViewPager.currentItem = mViewPager.currentItem + 1
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed()
                }
                this.doubleBackToExitPressedOnce = true
                snackbarFun(getString(R.string.back_exit))
                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                mViewPager.currentItem = mViewPager.currentItem + 1
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
//        menu?.setHeaderTitle("Context Menu")
//        menuInflater.inflate(R.menu.main, menu)
        val bookmark: String = checkBookmark()
        menu?.add(0, action_local, 0, getString(R.string.local))
        menu?.add(0, action_bookmark , 0, bookmark)
        menu?.add(0, action_tafseer, 0, getString(R.string.tafseer))
        menu?.add(0, action_tafseer2, 0, getString(R.string.tafseer2))
        menu?.add(0, change_image_size, 0, getString(R.string.change_image_size))
        menu?.add(0, invert_color, 0, getString(R.string.change_color))
        menu?.add(0, action_about, 0, getString(R.string.about_the_developer))
    }

    private fun checkBookmark(): String {
        var bookmarkTxt: String = getString(R.string.bookmark)
        bookmarkImg.visibility = View.GONE
        for (cPage in setMark!!) {
            val page: String = cPage.substring(cPage.lastIndexOf(" ") + 1, cPage.length)
            if (page == currentPageN) {//((pageNumber[i]).equals(currentPageN)) {
                bookmarkTxt = getString(R.string.bookmarked)
                bookmarkImg.visibility = View.VISIBLE
                break
            }
        }
        return bookmarkTxt
    }

    inner class SectionsPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            val fragment = DummySectionFragment()
            val args = Bundle()
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1)
            fragment.arguments = args
            getBookmark()
            return fragment
        }

        override fun getCount(): Int {
            return pagesCount
        }
    }

    fun getBookmark() {
        val page: Int = resources.getInteger(R.integer.pagesCount) - mViewPager.currentItem
        val surahPages: IntArray = resources.getIntArray(R.array.surahPage)
        val hezbPages: IntArray = resources.getIntArray(R.array.hezb)
        val surahNames: Array<String> = resources.getStringArray(R.array.surahs)
        val hezbAyat: IntArray = resources.getIntArray(R.array.hezbAyah)
        currentPageN = (page - resources.getInteger(R.integer.pagesLeading)).toString()
        if (page == 1) {
            info.text = getString(R.string.app_name)
        } else {
            if (page == pagesCount) {
                currentSurah = surahNames[surahNames.size - 1]
                currentSurahIndex = 114
            } else {
                for (i in surahPages.indices) {
                    if (surahPages[i] == page) {
                        currentSurah = surahNames[i]
                        currentSurahIndex = i + 1
                        break
                    } else if (surahPages[i] > page) {
                        currentSurah = surahNames[i - 1]
                        currentSurahIndex = i
                        break
                    }
                }
            }
            if (hezbPages[hezbPages.size - 1] < page) {
                currentJuz = getString(R.string.juz) + " " + (30).toString()
                currentHezbAyah = hezbAyat[hezbAyat.size -1]
                currentHezb = hezPart[2] + " " + getString(R.string.hezb) + " " + (60).toString()
            } else {
                for (i in hezbPages.indices) {
                    if (hezbPages[i] == page) {
                        currentJuz = getString(R.string.juz) + " " + (Integer.valueOf(i / 8) + 1)
                        currentHezb = getString(R.string.hezb) + " " + (Integer.valueOf(i / 4) + 1)
                        if (i % 4 != 0) {
                            currentHezb = hezPart[i % 4 - 1] + " " + currentHezb
                        }
                        currentHezbAyah = hezbAyat[i]
                        snackbarFun(currentHezb)
                        break
                    } else if (hezbPages[i] > page) {
                        currentJuz = getString(R.string.juz) + " " + (Integer.valueOf(i / 8) + 1) // && hezbPages[1] == 1
                        currentHezb = getString(R.string.hezb) + " " + (Integer.valueOf(i / 4) + 1)
                        if ((i - 1) % 4 != 0) {
                            currentHezb = hezPart[(i - 1) % 4 - 1] + " " + currentHezb
                        }
                        currentHezbAyah = hezbAyat[i]
                        break
                    }
                }
            }
            info.text = infoString()
            checkBookmark()
        }
    }

    private fun snackbarFun(displayText: String) {
        val snackbar = Snackbar.make(mViewPager, displayText, Snackbar.LENGTH_LONG)
        val layout = snackbar.view as Snackbar.SnackbarLayout
        layout.setBackgroundResource(R.drawable.snackbar_bg)
        snackbar.show()
    }

    class DummySectionFragment : androidx.fragment.app.Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false)
            val img: ImageView = rootView.findViewById(imageQuran)
            val currentPage: Int = resources.getInteger(R.integer.pagesCount) - requireArguments().getInt(ARG_SECTION_NUMBER) + 1
            img.setImageResource(resources.getIdentifier("q$currentPage", "drawable", rootView.context.packageName))
            img.setOnClickListener {
                mFab!!.close(true)
            }
            img.scaleType = when {
                fullScreen -> ImageView.ScaleType.FIT_XY
                else -> ImageView.ScaleType.FIT_START
            }

            if (invertColor) {
                val matrix = ColorMatrix()
                matrix.set(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                img.colorFilter = ColorMatrixColorFilter(matrix)
            }
            registerForContextMenu(img)
            return rootView
        }

        companion object {
            const val ARG_SECTION_NUMBER = "section_number"
        }
    }

    class AboutDeveloper : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(activity).setMessage(R.string.dialog_Developer)
                .setPositiveButton(R.string.visit) { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/in/ShadySelim")))}
                .setNegativeButton(R.string.cancel) { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Shady-Selim/Quran-Kareem-Material")))}
                .create()
        }
    }

    class SelectSurah(private val mViewPager: ViewPager, private val pagesCount: Int) : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val surahItems = ArrayList<String>()
            val madinan = arrayOf(2, 3, 4, 5, 8, 9, 13, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 110)
            val surahArray = resources.getStringArray(R.array.surahs)
            for (i in surahArray.indices) {
                val tanjil = if (contains(madinan, i + 1)) getString(R.string.medinan) else getString(R.string.meccan)
                surahItems.add("${i+1} - ${surahArray[i]} $tanjil")
            }
            val surahs = surahItems.toTypedArray<CharSequence>()
            return AlertDialog.Builder(activity).setTitle(R.string.surah)
                .setItems(surahs) { _, which ->
                    val page = resources.getIntArray(R.array.surahPage)
                    mViewPager.currentItem = pagesCount - page[which]
                }
                .create()
        }
    }

    class SelectHezb(private val mViewPager: ViewPager, private val pagesCount: Int, private val hezPart: Array<String>) : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val page: IntArray = resources.getIntArray(R.array.hezb)
            val hezbItems = ArrayList<String>()
            val hezbString: String = getString(R.string.hezb)
            for (i in page.indices) {
                if (i % 4 == 0) {
                    hezbItems.add(hezbString + " " + (Integer.valueOf(i / 4) + 1))
                    continue
                }
                hezbItems.add(hezPart[i % 4 - 1])
            }
            val hez = hezbItems.toTypedArray<CharSequence>()
            return AlertDialog.Builder(activity).setTitle(R.string.hezb)
                .setItems(hez) { _, which -> mViewPager.currentItem = pagesCount - page[which] }
                .create()
        }
    }

    class SelectJuz(private val mViewPager: ViewPager, private val pagesCount: Int) : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val juzItems = ArrayList<String>()
            var j = 1
            val juzString = getString(R.string.juz)
            val hezb = resources.getIntArray(R.array.hezb)
            val page = IntArray(30)
            for (i in hezb.indices) {
                if (i % 8 == 0) {
                    juzItems.add("$juzString $j")
                    page[j - 1] = hezb[i]
                    j++
                }
            }
            val hez = juzItems.toTypedArray<CharSequence>()
            return AlertDialog.Builder(activity).setTitle(R.string.juz)
                .setItems(hez) { _, which -> mViewPager.currentItem = pagesCount - page[which] }
                .create()
        }
    }

    class SelectBookmark(private val mViewPager: ViewPager, private val pagesCount: Int, private val setMark: MutableSet<String>?) : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            var k = 0
            val pageName = arrayOfNulls<CharSequence>(setMark!!.size)
            val pageNumber = arrayOfNulls<CharSequence>(setMark.size)
            setMark.forEach {
                pageName[k] = it
                pageNumber[k] = it.substring(it.lastIndexOf(" ") + 1, it.length)
                k++
            }
            return AlertDialog.Builder(activity).setTitle(R.string.bookmark)
                .setItems(pageName) { _, which -> mViewPager.currentItem = pagesCount - Integer.valueOf(pageNumber[which].toString()) - resources.getInteger(R.integer.pagesLeading) }
                .create()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (currentPageN == "1") return false
        when (item.itemId) {
            action_local -> {
                val edit: Editor = prefs.edit()
                edit.putInt("LastPage", mViewPager.currentItem)
                if (edit.commit()) {
                    var locale = Locale("ar")
                    if (item.title == "En") {
                        locale = Locale("en")
                    }
                    Locale.setDefault(locale)
                    val config = Configuration()
                    config.setLocale(locale)
                    baseContext.resources.updateConfiguration(config, resources.displayMetrics)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                return true
            }
            action_about -> {
                val newFragment: androidx.fragment.app.DialogFragment = AboutDeveloper()
                newFragment.show(supportFragmentManager, "about developer")
                return true
            }
            action_tafseer -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://quran.ksu.edu.sa/mobile.php#aya=${currentSurahIndex}_$currentHezbAyah")))
                return true
            }
            action_tafseer2 -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.altafsir.com/Tafasir.asp?tMadhNo=0&tTafsirNo=98&tSoraNo=$currentSurahIndex&tAyahNo=$currentHezbAyah&tDisplay=yes&UserProfile=0&LanguageId=1")))
                return true
            }
            action_bookmark -> {
                val nameFormat: String = infoString()
                if (item.title === getString(R.string.bookmarked)) {
                    setMark!!.remove(nameFormat)
                    bookmarkImg.visibility = View.GONE
                    prefBookmarkUpdate(setMark!!)
                } else {
                    setMark!!.add(nameFormat)
                    bookmarkImg.visibility = View.VISIBLE
                    prefBookmarkUpdate(setMark!!)
                }
                return true
            }
            change_image_size -> {
                prefs.edit().putInt("LastPage", mViewPager.currentItem)
                .putBoolean("FullScreen", !fullScreen).apply()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            invert_color -> {
                prefs.edit().putInt("LastPage", mViewPager.currentItem)
                    .putBoolean("InvertColor", !invertColor).apply()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun prefBookmarkUpdate(mark: MutableSet<String>) {
        val edit: Editor = prefs.edit()
        edit.putStringSet("Bookmarks", mark)
        if (edit.commit()) {
            snackbarFun(getString(R.string.bookmark_save_success))
        } else {
            snackbarFun(getString(R.string.bookmark_error))
        }
    }

    private fun infoString(): String = "$currentSurah, $currentJuz, $currentHezb, ${getString(R.string.page)} $currentPageN"

    companion object {
        lateinit var currentSurah: String
        lateinit var currentHezb: String
        lateinit var currentJuz: String
        lateinit var currentPageN: String
        var currentSurahIndex: Int = 1
        var currentHezbAyah: Int = 1
        var fullScreen: Boolean = false
        var invertColor: Boolean = false

        private var mFab: FloatingActionMenu? = null

        fun contains(arr: Array<Int>, item: Int?): Boolean {
            for (s in arr) {
                if (s == item)
                    return true
            }
            return false
        }
    }
}