package com.example.capstone.restaurant

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.example.capstone.*
import com.example.capstone.databinding.ActivityRestaurantWaitingBinding
import com.example.capstone.retrofit.API
import com.example.capstone.retrofit.IRetrofit
import com.example.capstone.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.properties.Delegates

class RestaurantWaitingActivity : AppCompatActivity(), ConfirmDialogInterface {
    private lateinit var binding: ActivityRestaurantWaitingBinding
    private lateinit var dialog1:CustomDialog
    private var searKeyword=""
    private var isSeatKeywordSelected=false
    private var numberOfPeople=2
    lateinit var userInfo: SharedPreferences
    lateinit var userId:String
    lateinit var resId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityRestaurantWaitingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userInfo = this.getSharedPreferences("userInfo", MODE_PRIVATE)
        userId = this.getSharedPreferences("userInfo", MODE_PRIVATE).getString("userId", "010-1234-5678").toString()
        Log.d("hy", userId)
        resId= intent.getStringExtra("resId").toString()
        binding.textView14.text=intent.getIntExtra("currWaiting", 0).toString()

        //todo 좌석 키워드 칩에 연결결

       var spinnerData=resources.getStringArray(R.array.spinner_array)
        var adapter=ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerData)
        binding.spinner.adapter=adapter
        binding.spinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(p2){
                    0->numberOfPeople=2
                    1->numberOfPeople=3
                    2->numberOfPeople=4
                    3->numberOfPeople=5
                    4->numberOfPeople=6
                    5->numberOfPeople=7
                    6->numberOfPeople=8
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val textData: String = binding.textView20.text.toString()
        val builder = SpannableStringBuilder(textData)
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.INUYellow)), 7, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textView20.text = builder

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                binding.seatKeyword1.id->searKeyword=binding.seatKeyword1.text.toString()
                binding.seatKeyword2.id->searKeyword=binding.seatKeyword2.text.toString()
                binding.seatKeyword3.id->searKeyword=binding.seatKeyword3.text.toString()
                binding.seatKeyword4.id->searKeyword=binding.seatKeyword4.text.toString()
                binding.seatKeyword5.id->searKeyword=binding.seatKeyword5.text.toString()
                binding.seatKeyword6.id->searKeyword=binding.seatKeyword6.text.toString()
                binding.seatKeyword7.id->searKeyword=binding.seatKeyword7.text.toString()
                binding.seatKeyword8.id->searKeyword=binding.seatKeyword8.text.toString()
            }
            isSeatKeywordSelected=true
            binding.checkBox.isChecked=false
        }
        binding.checkBox.setOnClickListener {
            if(binding.checkBox.isChecked) {
                searKeyword=""
                isSeatKeywordSelected=true
            }
            else {
                isSeatKeywordSelected=false
            }
        }
        binding.waitingButton.setOnClickListener {
            if(isSeatKeywordSelected){ //조건충족
                dialog1 = CustomDialog(this@RestaurantWaitingActivity, "인원: ${numberOfPeople}\n키워드:${searKeyword}\n대기를 신청하시겠습니까?", 0, 0)
                dialog1.isCancelable = false
                dialog1.show(this.supportFragmentManager, "ConfirmDialog")
            }
            else{ //조건 미충족
                dialog1 = CustomDialog(this@RestaurantWaitingActivity, "인원과 키워드를 선택해주세요.", 0, 1)
                dialog1.isCancelable = true
                dialog1.show(this.supportFragmentManager, "ConfirmDialog")
            }

        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onYesButtonClick(num: Int, theme: Int) {
        when(num){
            0->{
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val date = current.format(formatter)
                addWaiting(AddWaiting(userId , resId, numberOfPeople, date, searKeyword, false))
                dialog1.dismiss()
            }
        }
    }
    private fun addWaiting(addWaiting: AddWaiting){
        val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)
        val call = iRetrofit?.addWaiting(addWaiting = addWaiting) ?:return

        call.enqueue(object : Callback<AddWaiting> {

            override fun onResponse(call: Call<AddWaiting>, response: Response<AddWaiting>) {
                Log.d("hy", addWaiting.toString())
                Log.d("retrofit", "대기 신청 - 응답 성공 / t : ${response.raw()} ${response.body()}")
                val dialog = CustomDialog(this@RestaurantWaitingActivity, "대기 신청이 완료되었습니다.", 0, 1)
                dialog.isCancelable = true
                dialog.show(this@RestaurantWaitingActivity.supportFragmentManager, "ConfirmDialog")
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 500)
            }
            override fun onFailure(call: Call<AddWaiting>, t: Throwable) {
                Log.d("retrofit", "대기 신청 - 한식 응답 실패 / t: $t")
                val dialog = CustomDialog(this@RestaurantWaitingActivity, "잠시 후 다시 실행해주세요.", 0, 1)
                dialog.isCancelable = true
                dialog.show(this@RestaurantWaitingActivity.supportFragmentManager, "ConfirmDialog")
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 500)
            }
        })
    }
}