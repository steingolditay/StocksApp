package com.steingolditay.app.buxassignment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.steingolditay.app.buxassignment.R
import com.steingolditay.app.buxassignment.model.Product
import java.util.*
import kotlin.collections.ArrayList

class ProductsAdapter(private val context: Context,
                      private val products: ArrayList<Product>?,
                      private val listener: ProductsAdapter.OnItemClickListener): RecyclerView.Adapter<ProductsAdapter.ViewHolder>(), Filterable {

    val fullProductList = products?.toMutableList()

    interface OnItemClickListener{
        fun onItemClick(product: Product)
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.productName)
        val id: TextView = itemView.findViewById(R.id.productId)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && products != null){
                    listener.onItemClick(products[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products!![position]
        holder.name.text = product.displayName
        holder.id.text = product.securityId
    }

    override fun getItemCount(): Int {
        return products?.size ?: 0
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<Product>()
                if (constraint == null || constraint.isEmpty()){
                    if (fullProductList != null){
                        filteredList.addAll(fullProductList.toTypedArray())
                    }
                }
                else {
                    val pattern = constraint.toString().toLowerCase(Locale.getDefault()).trim()
                    if (fullProductList != null){
                        for (product in fullProductList){
                            if (product.displayName.toLowerCase(Locale.getDefault()).trim().contains(pattern))
                                filteredList.add(product)
                        }

                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (products != null && results != null){
                    products.clear()
                    products.addAll(results.values as List<Product>)
                    notifyDataSetChanged()
                }
            }

        }
    }
}