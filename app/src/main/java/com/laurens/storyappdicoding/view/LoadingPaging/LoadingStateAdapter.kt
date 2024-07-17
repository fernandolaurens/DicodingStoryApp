package com.laurens.storyappdicoding.view.LoadingPaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.core.view.isVisible
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.laurens.storyappdicoding.databinding.ItemLoadingBinding

class LoadingStateAdapter(private val onRetry: () -> Unit) : LoadStateAdapter<LoadingStateAdapter.PagingLoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PagingLoadStateViewHolder {
        val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PagingLoadStateViewHolder(binding, onRetry)
    }

    override fun onBindViewHolder(holder: PagingLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class PagingLoadStateViewHolder(private val binding: ItemLoadingBinding, onRetry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.BtnRetry.setOnClickListener { onRetry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.txtErrorMessage.text = loadState.error.localizedMessage
            }
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.BtnRetry.isVisible = loadState is LoadState.Error
            binding.txtErrorMessage.isVisible = loadState is LoadState.Error
        }
    }
}