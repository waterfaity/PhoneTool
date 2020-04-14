package com.waterfaity.phone

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.waterfaity.photo.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var share: SharedPreferences
    var isRunning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        share = getSharedPreferences("phone_history", Context.MODE_PRIVATE)
        frontNum.setText(share.getString("front_num", "13764"))
        startNum.setText(share.getString("current_phone", "0"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                ), 1001
            )
        }
        //13764 312271
//        NameTool.getData();
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    fun add(view: View) {
        isRunning = true
        share.edit().putString("front_num", frontNum.text.toString()).apply()
        val front = frontNum.text.toString();
        val start = startNum.text.toString().toInt()
        val forLength = endNum.text.toString().toInt()

        object : AsyncTask<Void, String, Int>() {

            override fun doInBackground(vararg params: Void?): Int {
                var num = 0;
                var index = 0
                for (i in start..(start + forLength)) {
                    index = i
                    if (!isRunning) return index
                    val generateName = "员工" + NameTool.generateName();
                    val endPhone = genePhoto(i, 11 - front.length);
                    val fullPhone = front + endPhone
                    if (fullPhone.length > 11) return index;

//                    val regex = "(\\d)\\1{2,}"
//                    val contains = fullPhone.contains(Regex(regex))
//                    if (!contains) {
                    num++
                    publishProgress(endPhone)
                    ContactAddUtils.addContact(
                        applicationContext,
                        ContactEntity(generateName, fullPhone)
                    )
//                    }

                }
                return index
            }

            override fun onProgressUpdate(vararg values: String?) {
                super.onProgressUpdate(*values)
                share.edit().putString("current_phone", values[0]).apply()
                content.setText(values[0])
            }

            override fun onPostExecute(result: Int) {
                super.onPostExecute(result)
                startNum.setText(result.toString())
            }
        }.execute()
    }

    private fun genePhoto(i: Int, length: Int): String {
        var photoNum = i.toString()
        val dLen = length - photoNum.length
        if (length - dLen > 0) {
            for (i in 0 until dLen) {
                photoNum = "0" + photoNum
            }
        }
        return photoNum

    }

    fun stop(view: View) {
        isRunning = false;
    }


}
