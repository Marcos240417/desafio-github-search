package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import br.com.igorbag.githubsearch.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var sharedPref: SharedPreferences

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupView()
        setupListeners()
        showUserName()

        viewModel.repositories.observe(this) { repos ->
            setupAdapter(repos)
        }
    }

    private fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        listaRepositories.layoutManager = LinearLayoutManager(this)
        sharedPref = getSharedPreferences("github_prefs", MODE_PRIVATE)
    }

    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            saveUserLocal()
            val user = nomeUsuario.text.toString().trim()
            viewModel.getAllReposByUserName(user)
        }
    }

    private fun showUserName() {
        val user = sharedPref.getString("user", "")
        nomeUsuario.setText(user)
    }

    private fun saveUserLocal() {
        sharedPref.edit {
            putString("user", nomeUsuario.text.toString())
        }
    }

    private fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(list)
        adapter.carItemLister = { openBrowser(it.htmlUrl) }
        adapter.btnShareLister = { shareRepositoryLink(it.htmlUrl) }
        listaRepositories.adapter = adapter
    }

    private fun shareRepositoryLink(urlRepository: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "Compartilhar"))
    }

    private fun openBrowser(urlRepository: String) {
        startActivity(Intent(Intent.ACTION_VIEW, urlRepository.toUri()))
    }
}
