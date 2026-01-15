package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupView()
        setupListeners()
        setupRetrofit()
        showUserName()
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
            getAllReposByUserName()
        }
    }

    private fun saveUserLocal() {
        sharedPref.edit()
            .putString("user", nomeUsuario.text.toString())
            .apply()
    }

    private fun showUserName() {
        val user = sharedPref.getString("user", "")
        nomeUsuario.setText(user)
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    private fun getAllReposByUserName() {
        val user = nomeUsuario.text.toString().trim()
        if (user.isEmpty()) return

        githubApi.getAllRepositoriesByUser(user)
            .enqueue(object : Callback<List<Repository>> {

                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            setupAdapter(it)
                        }
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    private fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(list)

        adapter.carItemLister = {
            openBrowser(it.htmlUrl)
        }

        adapter.btnShareLister = {
            shareRepositoryLink(it.htmlUrl)
        }

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
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(urlRepository))
        )
    }
}
