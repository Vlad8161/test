package com.example.test.utils

import android.content.Context
import android.util.Log
import com.example.test.R
import org.json.JSONException
import org.json.JSONObject
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import retrofit2.HttpException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Created by vlad on 25.11.18.
 */

inline fun Context.handleError(throwable: Throwable, func: (msg: String) -> Unit) {
    val msg = when (throwable) {
        is HttpException -> throwable.response().errorBody()?.string()
                ?.also { Log.d("LOGI", "body: $it") }
                ?.let { parseErrorBody(it) }
                ?: getString(R.string.err_unknown)
        is IOException -> getString(R.string.err_network)
        else -> getString(R.string.err_unknown)
    }
    func(msg)
}

fun parseErrorBody(body: String): String? = try {
    JSONObject(body).getString("message")
} catch (e: JSONException) {
    try {
        val doc = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(InputSource(StringReader(body)))
        val xpath = "Error/Message"
        XPathFactory.newInstance()
                .newXPath()
                .evaluate(xpath, doc, XPathConstants.STRING) as String
    } catch (e: SAXException) {
        null
    }
}
