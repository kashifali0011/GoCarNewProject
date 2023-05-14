package com.towsal.towsal.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.LayoutItemPaymentMethodBinding
import com.towsal.towsal.network.serializer.payment.PaymentsResponseModel
import com.towsal.towsal.utils.CardBrand

class PaymentMethodsAdapter(
    val paymentMethodsList: ArrayList<PaymentsResponseModel>,
    val deleteCallback: (Int) -> Unit,
    val callback: (Int, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var defaultCardPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = LayoutItemPaymentMethodBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.layout_item_payment_method,
                    parent,
                    false
                )
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val model = paymentMethodsList[position]
        if (
            model.isDefault == 1
        )
            defaultCardPosition = position
        viewHolder.binding.rbDefaultCard.apply {
            isChecked = (
                    model
                        .isDefault == 1
                    )
            setOnClickListener {
                paymentMethodsList[defaultCardPosition].isDefault = 0
                notifyItemChanged(defaultCardPosition)
                if (model.isDefault != 1) {
                    callback(position, defaultCardPosition)
                }
            }

        }
        model.paymentBrand?.let {
            viewHolder.binding.ivCardType.setImageResource(getImageResource(it, viewHolder.binding))
        }

        viewHolder.binding.ivDelete.setOnClickListener {
            deleteCallback(position)
        }

        model.paymentMethodType?.let {
            model.paymentMethodType?.let {
                viewHolder.binding.tvCardNumber.text =
                    "Credit Card (${ model.details.cardNumber?.takeLast(4)})"
            }
        }


    }

    private fun getImageResource(
        paymentBrand: String,
        binding: LayoutItemPaymentMethodBinding
    ): Int {
        return when (paymentBrand.lowercase()) {
            CardBrand.VISA.value -> {
                binding.tvTypeName.text = "visa card"
                R.drawable.ic_visa
            }
            CardBrand.MASTER.value -> {
                binding.tvTypeName.text = "master card"
                R.drawable.ic_master
            }
            CardBrand.MADA.value -> {
                binding.tvTypeName.text = "mada card"
                R.drawable.ic_mada
            }
            CardBrand.AMEX.value -> {
                binding.tvTypeName.text = "amex card"
                R.drawable.ic_amex
            }
            else -> {
                binding.tvTypeName.text = "visa card"
                R.drawable.ic_visa
            }
        }
    }

    override fun getItemCount(): Int = paymentMethodsList.size

    class ViewHolder(
        val binding: LayoutItemPaymentMethodBinding
    ) : RecyclerView.ViewHolder(binding.root)

}

