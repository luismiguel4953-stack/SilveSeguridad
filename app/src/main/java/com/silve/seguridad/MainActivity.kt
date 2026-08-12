package com.silve.seguridad

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.statusText)
        val scan = findViewById<Button>(R.id.scanButton)
        val links = findViewById<Button>(R.id.linkButton)
        val assistant = findViewById<Button>(R.id.assistantButton)
        val emergency = findViewById<Button>(R.id.emergencyButton)

        scan.setOnClickListener {
            status.text = "Análisis inicial completado. Revisa las recomendaciones antes de continuar."
            Toast.makeText(this, "Análisis completado", Toast.LENGTH_SHORT).show()
        }

        links.setOnClickListener {
            Toast.makeText(this, "Comprobador de enlaces: módulo en preparación", Toast.LENGTH_SHORT).show()
        }

        assistant.setOnClickListener {
            Toast.makeText(this, "Asistente de seguridad: módulo en preparación", Toast.LENGTH_SHORT).show()
        }

        emergency.setOnClickListener {
            Toast.makeText(this, "Configura primero tus contactos de emergencia", Toast.LENGTH_LONG).show()
        }
    }
}
