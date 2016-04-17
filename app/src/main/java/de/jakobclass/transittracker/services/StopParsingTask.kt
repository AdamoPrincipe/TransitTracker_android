package de.jakobclass.transittracker.services

import android.os.AsyncTask
import de.jakobclass.transittracker.models.Stop
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

interface StopParsingTaskDelegate {
    val stops: Map<String, Stop>

    fun addStops(stops: List<Stop>)
}

class StopParsingTask(val delegate: StopParsingTaskDelegate): AsyncTask<JSONObject, Void, List<Stop>>() {
    override fun doInBackground(vararg data: JSONObject?): List<Stop>? {
        var stops = mutableListOf<Stop>()
        val data = data.first()
        data?.let {
            val stopsData = data.getJSONArray("stops")
            if (stopsData.length() > 1) {
                // we skip the first element because it's something like a column descriptor:
                // ["x","y","viewmode","name","extId","puic","prodclass","lines"]
                for (i in 1..(stopsData.length() - 1)) {
                    val stopData = stopsData.getJSONArray(i)
                    val name = stopData.getString(3)
                    if (delegate.stops.containsKey(name)) {
                        continue
                    }
                    val stop = Stop(stopData)
                    stop?.let {
                        stops.add(stop)
                    }
                }
            }
        }
        return stops
    }

    override fun onPostExecute(stops: List<Stop>?) {
        delegate.addStops(stops ?: listOf<Stop>())
    }
}

fun Stop(data: JSONArray): Stop? {
    try {
        val x = data.getInt(0)
        val y = data.getInt(1)
        val name = data.getString(3)
        return Stop(LatLng(x = x, y = y), name)
    } catch (e: JSONException) {
        return null
    }
}