package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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

class MainActivity: AppCompatActivity() {

        lateinit var nomeUsuario: EditText
        lateinit var btnConfirmar: Button
        lateinit var listaRepositories: RecyclerView
        lateinit var progress: ProgressBar
        lateinit var noInternet: LinearLayout
        lateinit var githubApi: GitHubService

        var previousUser = ""
        var hasInternet = true



        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)

                setupView()
                setupListeners()
                showUserName()

                setupRetrofit()
        }

        override fun onResume() {
                super.onResume()

                hasInternet = checkForInternet(this@MainActivity)

                listaRepositories.isVisible = hasInternet
                noInternet.isVisible = !hasInternet

                if (!hasInternet) {
                        return
                }

                if (nomeUsuario.text.isNotEmpty()) {
                        getAllReposByUserName()
                }
        }


        // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
        fun setupView() {
                nomeUsuario = findViewById<EditText>(R.id.et_nome_usuario)
                btnConfirmar = findViewById<Button>(R.id.btn_confirmar)
                listaRepositories = findViewById<RecyclerView>(R.id.rv_lista_repositories)
                progress = findViewById<ProgressBar>(R.id.pb_loading)
                noInternet = findViewById<LinearLayout>(R.id.ll_no_internet)
        }


        //metodo responsavel por configurar os listeners click da tela
        private fun setupListeners() {
                btnConfirmar.setOnClickListener {
                        if (!hasInternet) {
                                return@setOnClickListener
                        }

                        if (nomeUsuario.text.isEmpty()) {
                                Toast.makeText(this@MainActivity, "O nome de usuário não pode ser em branco...", Toast.LENGTH_LONG).show()
                                return@setOnClickListener
                        }

                        saveUserLocal(nomeUsuario.text.toString())
                        getAllReposByUserName()
                }

        }


        // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
        private fun saveUserLocal(nome: String) {
                val prefs = getPreferences(Context.MODE_PRIVATE) ?: return

                prefs.edit().apply {
                        putString(getString(R.string.nome_usuario), nome)
                        apply()
                }
        }


        private fun showUserName() {
                val prefs = getPreferences(Context.MODE_PRIVATE) ?: return

                prefs.getString(getString(R.string.nome_usuario), "")?.let {
                        nomeUsuario.setText(it)
                }
        }


        //Metodo responsavel por fazer a configuracao base do Retrofit
        fun setupRetrofit() {
                val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.github.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                githubApi = retrofit.create(GitHubService::class.java)
        }


        //Metodo responsavel por buscar todos os repositorios do usuario fornecido
        fun getAllReposByUserName() {
                progress.isVisible = true
                listaRepositories.isVisible = false

                githubApi.getAllRepositoriesByUser(nomeUsuario.text.toString()).enqueue(object: Callback<List<Repository>> {
                        override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                                progress.isVisible = false

                                if (!response.isSuccessful) {
                                        Toast.makeText(this@MainActivity, R.string.response_error, Toast.LENGTH_LONG).show()
                                        return
                                }


                                response.body()?.let {
                                        listaRepositories.isVisible = true
                                        setupAdapter(it)
                                }
                        }

                        override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                                listaRepositories.isVisible = true
                                progress.isVisible = false
                        }

                })
        }

        // Metodo responsavel por realizar a configuracao do adapter
        fun setupAdapter(list: List<Repository>) {
                listaRepositories.adapter = RepositoryAdapter(
                        list,
                        contentListener = {
                                openBrowser(it.htmlUrl)
                        },
                        shareListener = {
                                shareRepositoryLink(it.htmlUrl)
                        }
                )
        }


        // Metodo responsavel por compartilhar o link do repositorio selecionado
        fun shareRepositoryLink(urlRepository: String) {
                val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, urlRepository)
                        type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
        }

        // Metodo responsavel por abrir o browser com o link informado do repositorio
        fun openBrowser(urlRepository: String) {
                startActivity(
                        Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(urlRepository)
                        )
                )
        }



        fun checkForInternet(context: Context): Boolean {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val network = connectivityManager.activeNetwork ?: return false
                        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false


                        return when {
                                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                                else -> false
                        }

                } else {
                        @Suppress("DEPRECATION")
                        val networkInfo = connectivityManager.activeNetworkInfo ?: return false

                        @Suppress("DEPRECATION")
                        return networkInfo.isConnected
                 }
        }
}