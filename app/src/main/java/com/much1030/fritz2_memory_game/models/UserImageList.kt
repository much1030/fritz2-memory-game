package com.much1030.fritz2_memory_game.models

import com.google.firebase.firestore.PropertyName

data class UserImageList(
    @PropertyName("images") val images: List<String>? = null
)
