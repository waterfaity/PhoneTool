package com.waterfaity.photo

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                ), 1001
            )
        }
        //13764 312271

        NameTool.getData();


    }

    fun add(view: View) {
        var start = startNum.text.toString().toInt()
        var end = endNum.text.toString().toInt()

        var num = 0;
        for (i in start..end) {
            val generateName = "员工" + NameTool.generateName();
            var photo = "13764" + genePhoto(i, 6)

            var gegex = "(\\d)\\1{2,}"
            val contains = photo.contains(Regex(gegex))
            if (contains) {
//                Log.i("generateName ", "name:" + generateName + " \t: " + photo)
            } else {
                num++
                Log.i("generateName ", "name:" + generateName + " \t: " + photo)
                PhotoAddUtils.addContact(this, ContactEntity(generateName, photo))
//                PhotoAddUtils.deleteContact(this, photo)
            }

        }
        Log.i("generateName ", "total:" + num)
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


}
