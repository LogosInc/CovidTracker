package com.example.covidtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.covidtracker.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val BASE_URL = "https://covidtracking.com/api/v1/"
private const val TAG ="MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val covidService = retrofit.create(CovidService::class.java)
        // Fetch the national data
         covidService.getNationalData().enqueue(object : Callback<List<CovidData>>{
             override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>
             ) {
                 Log.i(TAG,"onResponse $response")
                 val nationalData = response.body()
                 if (nationalData == null){
                     Log.w(TAG, "Didn't receive a valid response body")
                     return
                 }
                 nationalDailyData = nationalData.reversed()
                 Log.i(TAG, "Update graph with national data")
                 updateDisplayWithData(nationalDailyData)
             }

             override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                 Log.e(TAG,"onFailure $t")
             }
         })
        // Fetch the state data
        covidService.getStateData().enqueue(object: Callback<List<CovidData>>{
            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>
            ) {
                Log.i(TAG,"onResponse $response")
                val stateData = response.body()
                if (stateData == null){
                    Log.w(TAG, "Didn't receive a valid response body")
                    return
                }
                perStateDailyData = stateData.reversed().groupBy { it.state }
                Log.i(TAG, "Update spinner with state names")
                // TODO: Update spinner with state names
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG,"onFailure $t")
            }
        } )
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        // Create a new SparkAdapter with the data
        val adapter = CovidSparkAdapter(dailyData)
        binding.sparkView.adapter = adapter
        // Update radio buttons to select the positive cases and max time by default
        binding.rbPositive.isChecked = true
        binding.rbMax.isChecked = true
        // Display metric for the most recent date
        updateInfoForDate(dailyData.last())
    }

    private fun updateInfoForDate(covidData: CovidData) {
        binding.tvMetricLabel.text = NumberFormat.getInstance().format(covidData.positiveIncrease)
        val outputDateFormat = SimpleDateFormat("MMM dd yyyy",  Locale.US)
        binding.tvDateLabel.text = outputDateFormat.format(covidData.dateChecked)
    }
}