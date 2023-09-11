package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(
        private val repositories: List<Repository>,
        var contentListener: (Repository) -> Unit = {},
        var shareListener: (Repository) -> Unit = {},
): RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {


        // Cria uma nova view
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
                return ViewHolder(view)
        }

        // Pega o conteudo da view e troca pela informacao de item de uma lista
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.bind(repositories[position], position)
        }

        // Pega a quantidade de repositorios da lista
        override fun getItemCount(): Int = repositories.size



        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
                var repositoryContent: ConstraintLayout
                var repositoryName: TextView
                var btnShare: ImageView

                init {
                        view.apply {
                                repositoryContent = findViewById(R.id.cl_card_content)
                                repositoryName = findViewById(R.id.tv_repositorio)
                                btnShare = findViewById(R.id.iv_share)
                        }
                }


                fun bind(repository: Repository, position: Int) = with(itemView) {
                        repositoryName.text = repository.name

                        btnShare.setOnClickListener {
                                contentListener(repository)
                        }
                        repositoryContent.setOnClickListener {
                                shareListener(repository)
                        }
                }
        }
}


