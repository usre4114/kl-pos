package com.klpos.app

import android.app.AlertDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    private val PIN = "1234"
    private val balances = HashMap<String, Int>()
    private var lastCard: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPin()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        val uid = tag.id.joinToString("") { "%02X".format(it) }
        val hash = sha(uid)

        if (!balances.containsKey(hash)) {
            if (balances.size >= 4) {
                toast("Kart limiti dolu")
                return
            }
            balances[hash] = 200
        }

        lastCard = hash
        toast("Bakiye: ${balances[hash]} KL")
    }

    fun cek() {
        val c = lastCard ?: return
        if (balances[c]!! < 50) return
        balances[c] = balances[c]!! - 50
        toast("Yeni bakiye: ${balances[c]}")
    }

    fun aktar() {
        val keys = balances.keys.toList()
        if (keys.size < 2) return
        val from = lastCard ?: return
        val to = keys.first { it != from }

        if (balances[from]!! < 50) return
        balances[from] = balances[from]!! - 50
        balances[to] = balances[to]!! + 50

        toast("Aktarma OK")
    }

    private fun sha(s: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun toast(t: String) =
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show()

    private fun askPin() {
        val input = android.widget.EditText(this)
        AlertDialog.Builder(this)
            .setTitle("PIN")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                if (input.text.toString() != PIN) finish()
            }.show()
    }
}
