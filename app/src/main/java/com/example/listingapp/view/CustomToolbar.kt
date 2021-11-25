package com.example.listingapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.listingapp.R
import com.example.listingapp.databinding.CustomToolbarBinding
import com.google.android.material.appbar.MaterialToolbar

class CustomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.toolbarStyle,
) : MaterialToolbar(context, attrs, defStyle) {
    private var binding: CustomToolbarBinding =
        CustomToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.custom_component_attributes,
            0,
            0
        )
        setContentInsetsAbsolute(0, 0)
        val title = a.getString(R.styleable.custom_component_attributes_title)
        val temperature = a.getString(R.styleable.custom_component_attributes_temperature)
        val city = a.getString(R.styleable.custom_component_attributes_city)
        val area = a.getString(R.styleable.custom_component_attributes_area)
        binding.pageTitle.text = title
        binding.vTemperature.text = temperature
        binding.vCity.text = city
        binding.vArea.text = area
        a.recycle()
    }


    fun setTemperature(text: String) {
        binding.vTemperature.text = "$text°"
    }

    fun setCity(text: String) {
        binding.vCity.text = text
    }

    fun setArea(text: String) {
        binding.vArea.text = text
    }

}


