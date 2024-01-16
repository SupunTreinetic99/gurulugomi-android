package com.treinetic.whiteshark.util

import com.treinetic.whiteshark.models.FormattedDate
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Nuwan on 2/6/19.
 */
class DateFormatUtil {

    companion object {
        private val instance = DateFormatUtil()
        fun getInstance(): DateFormatUtil {
            return instance;
        }
    }


    fun getFormattedDate(dateString: String): FormattedDate {
        val pattern = "yyyy-MM-dd"
        dateString.split("")
        var formatter = SimpleDateFormat(pattern, Locale.ENGLISH)
        var dateOnj: Date = formatter.parse(dateString)

        var calendar = Calendar.getInstance()
        calendar.timeInMillis = dateOnj.time

        val year = calendar.get(Calendar.YEAR)
        val date = calendar.get(Calendar.DATE)

        val month = SimpleDateFormat("MMMM").format(calendar.time)

        return FormattedDate(year, month, date)


    }


}